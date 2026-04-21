/*
 * Copyright (c) 2026 Hexant, LLC
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package charlie.test.core;

import junit.framework.TestCase;
import java.util.Date;

/**
 * This class exercises await-signal logic.
 * @author Ron.Coleman
 */
public class AwaitSignalTest extends TestCase {
    final static int N = 5;

    final Object monitor = new Object();
    volatile boolean trucking = true;

    /**
     * Enters test case here.
     */
    public void test() throws Exception {
        for(int cycleno=0; cycleno < N; cycleno++) {
            System.out.println("process stared: " + new Date()+" ");
            System.out.flush();

            // Fork the main thread to simulate the signal process.
            new Thread(() -> {
                try {
                    Thread.sleep(2000);
                    signal();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();

            // Simulate the block for process to complete
            assert await(20000);

            System.out.println("process ended: " + new Date());
            System.out.flush();
        }
    }

    /**
     * Awaits signal.
     * @param timeout Time in millis to wait
     * @return True if got signal, false if timed out
     */
    public boolean await(long timeout) throws InterruptedException {
        trucking = true;

        long deadline = System.currentTimeMillis() + timeout;
        System.out.println("deadline: "+new Date(deadline));

        synchronized (monitor) {
            while (trucking) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    return false;              // timed out
                }
                System.out.println(new Date()+" >>> going to wait");
                monitor.wait(remaining);
                System.out.println(new Date()+" >>> woke up");
            }
            return true;                       // condition became true
        }
    }

    /**
     * Signals awaiting thread(s).
     */
    public void signal() {
        synchronized (monitor) {
            trucking = false;
            monitor.notifyAll();
            System.out.println(new Date()+" !!! notified");
        }
    }
}

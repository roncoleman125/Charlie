/*
 * Copyright (c) Ron Coleman
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package charlie.test.framework;

import charlie.actor.Arriver;
import charlie.actor.ClientAuthenticator;
import charlie.actor.Courier;
import charlie.card.Hid;
import charlie.plugin.IUi;
import charlie.server.GameServer;
import charlie.server.Ticket;
import junit.framework.TestCase;
import org.apache.log4j.Logger;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * This class is the base testing framework.
 * @author Ron.Coleman
 */
public abstract class Perfect extends TestCase {
    final Logger LOG = Logger.getLogger(this.getClass());
    final String CHARLIE_PROPS_PATH = System.getProperty("charlie.props","charlie.props");

    protected Courier courier = null;

    /**
     *
     * Logs diagnostics conveniently.
     * @param text Text of message.
     */
    public void info(String text) {
        LOG.info(this.getClass().getSimpleName()+" "+text);
    }

    /**
     * Logs error diagnostics conveniently.
     * @param text Text of message.
     */
    public void error(String text) {
        LOG.error(this.getClass().getSimpleName()+" "+text);
    }

    /**
     * Sleeps as a convenience method.
     * @param millis Sleep-time in milliseconds.
     */
    public void sleep(long millis) {
        try {
            Thread.sleep(millis);
        }
        catch(Exception ignored) {

        }
    }

    /**
     * Launches the game server and logs in.
     */
    public void go(IUi ui) throws Exception {
        // Authentication looks for these properties
        Properties props = System.getProperties();
        props.load(new FileInputStream(CHARLIE_PROPS_PATH));

        // Start server as a worker thread.
        new Thread(() -> {
            new GameServer().go();
        }).start();

        // Wait for server to start properly
        sleep(500);

        // Connect to game server securely.
        ClientAuthenticator authenticator = new ClientAuthenticator();

        // The ticket is needed for subsequent communication(s).
        Ticket ticket = authenticator.send("perfect","123");
        info("connecting to server");

        // Start the courier which communicates over sockets with the server.
        courier = new Courier(ui);

        courier.start();
        info("courier started");

        // Tell the game server we've arrived.
        new Arriver(ticket).send();
        info("we ARRIVED!");

        // Game server will be ready when it notifies us, see Courier.got(:Ready).
        synchronized (this) {
            info("waiting for server READY...");
            this.wait();
        }

        info("server READY !");
    }

    /**
     * Places main and side bets with courier
     * @param amt Main bet
     * @param sideAmt Side bet
     */
    public void bet(int amt, int sideAmt) {
        courier.bet(amt,sideAmt);
    }

    /**
     * Places a bet.
     * @param amt Main bet
     */
    public void bet(int amt) {
        courier.bet(amt,0);
    }

    /**
     * Requests stay.
     * @param hid Hand id
     */
    public void stay(Hid hid) {
        new Thread(() -> courier.stay(hid)).start();
    }

    /**
     * Requests hit.
     * @param hid Hand id
     */
    public void hit(Hid hid) {
        new Thread(() -> courier.hit(hid)).start();
    }

    /**
     * Requests double-down.
     * @param hid Hand id
     */
    public void doubleDown(Hid hid) {
        new Thread(() -> courier.dubble(hid)).start();
    }

    /**
     * Requests split.
     * @param hid Hand id
     */
    public void split(Hid hid) {
        new Thread(() -> courier.split(hid)).start();
    }

    /**
     * Not supported.
     */
    public void insure() { }

    final Object lock = new Object();
    boolean trucking = true;

    /**
     * Awaits signal.
     * @param timeout Time in millis to wait
     * @return True if got signal, false if timed out
     */
    public boolean await(long timeout) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeout;

        synchronized (lock) {
            while (trucking) {
                long remaining = deadline - System.currentTimeMillis();
                if (remaining <= 0) {
                    return false;              // timed out
                }
                lock.wait(remaining);
            }
            return true;                       // condition became true
        }
    }

    /**
     * Signals awaiting thread(s).
     */
    public void signal() {
        synchronized (lock) {
            trucking = false;
            lock.notifyAll();
        }
    }
}

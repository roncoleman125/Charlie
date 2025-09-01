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
package charlie.test;

import junit.framework.TestCase;
import org.apache.log4j.Logger;

/**
 * This class is the starting framework for testing Charlie.
 * @author Ron.Coleman
 */
public abstract class AbstractTestCase extends TestCase {
    public Logger LOG = Logger.getLogger(this.getClass());

    /**
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
}

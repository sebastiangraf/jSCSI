/**
 * 
 */
package org.jscsi.initiator.example;

import org.jscsi.exception.ConfigurationException;
import org.jscsi.exception.NoSuchSessionException;
import org.jscsi.exception.TaskExecutionException;
import org.jscsi.initiator.Configuration;
import org.jscsi.initiator.Initiator;

/**
 * Example 1, Just creating and closing a session.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class SimpleLoginLogout {

    public static void main(final String[] args) throws NoSuchSessionException, TaskExecutionException,
        ConfigurationException {
        // init of the target
        String target = "testing-xen2-disk1";
        Initiator initiator = new Initiator(Configuration.create());
        // creating session, performing login on target
        initiator.createSession(target);
        // closing the session
        initiator.closeSession(target);
    }
}

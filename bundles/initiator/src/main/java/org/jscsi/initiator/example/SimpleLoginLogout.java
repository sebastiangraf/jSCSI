/**
 * 
 */
package org.jscsi.initiator.example;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.jscsi.exception.TaskExecutionException;
import org.jscsi.initiator.Configuration;
import org.jscsi.initiator.Initiator;
import org.jscsi.parser.exception.NoSuchSessionException;
import org.xml.sax.SAXException;

/**
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class SimpleLoginLogout {

    public static void main(final String[] args) throws NoSuchSessionException,
            SAXException, ParserConfigurationException, IOException, TaskExecutionException {
        // init of the target
        String target = "testing-xen2-disk1";
        Initiator initiator = new Initiator(Configuration.create());
        // creating session, performing login on target
        initiator.createSession(target);
        // closing the session
        initiator.closeSession(target);

    }
}

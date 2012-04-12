/**
 * 
 */
package org.jscsi.exception;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Exception for handling exception occuring while configuration setup.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class ConfigurationException extends Exception {

    /**
     * Necessary for the exception.
     */
    private static final long serialVersionUID = -3506430654972972154L;

    /**
     * 
     * Constructor to handle {@link SAXException}s.
     * 
     * @param exc
     *            to be encapsulated
     */
    public ConfigurationException(final SAXException exc) {
        super(exc);
    }

    /**
     * 
     * Constructor to handle {@link ParserConfigurationException}s.
     * 
     * @param exc
     *            to be encapsulated
     */
    public ConfigurationException(final ParserConfigurationException exc) {
        super(exc);
    }

    /**
     * 
     * Constructor to handle {@link IOException}s.
     * 
     * @param exc
     *            to be encapsulated
     */
    public ConfigurationException(final IOException exc) {
        super(exc);
    }

}

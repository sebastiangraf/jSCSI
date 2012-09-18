/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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

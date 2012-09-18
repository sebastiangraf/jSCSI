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
package org.jscsi.exception;

/**
 * This is a base class for the exception handling at the parsing process of the
 * iSCSI protocol.
 * 
 * @author Volker Wildi
 */
public class InternetSCSIException extends Exception {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The Serial Version ID. */
    private static final long serialVersionUID = 1L;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructs a new exception with null as its detail message.
     */
    public InternetSCSIException() {

        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     * 
     * @param msg
     *            the detail message. The detail message is saved for later
     *            retrieval by the Throwable.getMessage() method.
     */
    public InternetSCSIException(final String msg) {

        super(msg);
    }

    /**
     * Constructs a new exception as a wrapper for a given exception.
     * 
     * @param e
     *            the exception to be wrapped
     */
    public InternetSCSIException(final Exception e) {

        super(e);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

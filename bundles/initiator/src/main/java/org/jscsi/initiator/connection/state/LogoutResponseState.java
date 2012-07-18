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
package org.jscsi.initiator.connection.state;

import java.io.IOException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.logout.LogoutResponse;
import org.jscsi.parser.logout.LogoutResponseParser;

/**
 * <h1>LogoutResponseState</h1>
 * <p/>
 * This state handles a Logout Response.
 * 
 * @author Volker Wildi
 */
final class LogoutResponseState extends AbstractState {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a <code>LogoutResponseState</code> instance, which
     * uses the given connection for transmission.
     * 
     * @param initConnection
     *            The context connection, which is used for the network
     *            transmission.
     */
    public LogoutResponseState(final Connection initConnection) {

        super(initConnection);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final void execute() throws InternetSCSIException {

        final ProtocolDataUnit protocolDataUnit = connection.receive();

        if (!(protocolDataUnit.getBasicHeaderSegment().getParser() instanceof LogoutResponseParser)) {
            throw new InternetSCSIException("This PDU type ("
                + protocolDataUnit.getBasicHeaderSegment().getParser() + ") is not expected. ");
        }

        final LogoutResponseParser parser =
            (LogoutResponseParser)protocolDataUnit.getBasicHeaderSegment().getParser();

        if (parser.getResponse() == LogoutResponse.CONNECTION_CLOSED_SUCCESSFULLY) {
            // exception rethrow
            try {
                // FIXME: Implement Connection close
                connection.getSession().close();
            } catch (IOException e) {
                throw new InternetSCSIException("Closing the connection throws this error: "
                    + e.getLocalizedMessage());
            }
        } else {
            throw new InternetSCSIException("The connection could not be closed successfully.");
        }

        // return false;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

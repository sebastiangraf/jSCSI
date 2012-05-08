/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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
package org.jscsi.initiator;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.exception.NoSuchSessionException;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.initiator.connection.Session;

/**
 * <h1>LinkFactory</h1>
 * <p>
 * Implements a Factory which enables the Initiator to switch between
 * multithreaded and singlethreaded modes.
 * </p>
 * 
 * @author Patrice Matthias Brend'amour
 */

public final class LinkFactory {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The logger interface. */
    private static final Log LOGGER = LogFactory.getLog(LinkFactory.class);

    /** The calling Initiator. */
    private final Initiator initiator;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default Constructor to create a Linkfactory. Creates everything
     * MultiThreaded and MultiConnectioned
     * 
     * @param initiat
     *            The calling Initiator
     */

    public LinkFactory(final Initiator initiat) {

        this.initiator = initiat;
    }

    /**
     * Method to create and return a new, empty <code>Session</code> object with
     * the configured layer of threading.
     * 
     * @param initConfiguration
     *            The configuration to use within this session.
     * @param initTargetName
     *            The name of the iSCSI Target.
     * @param inetAddress
     *            The <code>InetAddress</code> of the leading connection of this
     *            session.
     * @return AbsSession The SessionObject.
     */
    public final Session getSession(final Configuration initConfiguration,
            final String initTargetName, final InetSocketAddress inetAddress) {

        try {
            // Create a new Session
            final Session newSession = new Session(this, initConfiguration,
                    initTargetName, inetAddress,
                    Executors.newSingleThreadExecutor());
            return newSession;
        } catch (Exception e) {
            LOGGER.error("This exception is thrown: " + e);
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Method to create and return a new, empty <code>Connection</code> object
     * with the configured layer of threading.
     * 
     * @param session
     *            Reference to the <code>AbsSession</code> object, which
     *            contains this connection.
     * @param initConfiguration
     *            The configuration to use within this connection.
     * @param inetAddress
     *            The <code>InetSocketAddress</code> to which this connection
     *            should established.
     * @param initConnectionID
     *            The ID of this connection.
     * @return AbsConnection The Connection Object.
     */
    public final Connection getConnection(final Session session,
            final Configuration initConfiguration,
            final InetSocketAddress inetAddress, final short initConnectionID) {

        try {
            final Connection newConnection = new Connection(session,
                    initConfiguration, inetAddress, initConnectionID);
            return newConnection;
        } catch (Exception e) {
            LOGGER.error("This exception is thrown: " + e);
            e.printStackTrace();
            return null;
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Adds a dying <code>Session</code> instance to the Queue.
     * 
     * @param session
     *            The name of the session, which instance you want.
     */
    public final void closedSession(final Session session) {

        try {
            initiator.removeSession(session);
        } catch (NoSuchSessionException e) {
            // DO NOTHING
        }

    }
}

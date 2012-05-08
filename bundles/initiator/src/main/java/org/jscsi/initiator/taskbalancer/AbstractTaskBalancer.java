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
package org.jscsi.initiator.taskbalancer;

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.exception.NoSuchConnectionException;
import org.jscsi.initiator.connection.Connection;
import org.jscsi.initiator.connection.Session;

/**
 * <h1>AbstractLoadBalancer</h1>
 * <p/>
 * Each load balancer must extend this abstract class to support some basic
 * features.
 * 
 * @author Volker Wildi
 */
public abstract class AbstractTaskBalancer {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** This list contains all free connections of a iSCSI Session. */
    protected LinkedBlockingQueue<Connection> freeConnections;

    /** The Logger interface. */
    protected static final Log LOGGER = LogFactory
            .getLog(AbstractTaskBalancer.class);

    /** The Session. */
    protected Session session;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new <code>AbstractLoadBalancer</code> instance,
     * which distribute the work to these connections.
     * 
     * @param initConnections
     *            The list with all opened connections.
     */
    protected AbstractTaskBalancer(
            final LinkedBlockingQueue<Connection> initConnections) {

        freeConnections = initConnections;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    public abstract Connection getConnection() throws NoSuchConnectionException;

    public void releaseConnection(final Connection connection)
            throws NoSuchConnectionException {

        if (connection != null) {
            try {
                freeConnections.add(connection);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}
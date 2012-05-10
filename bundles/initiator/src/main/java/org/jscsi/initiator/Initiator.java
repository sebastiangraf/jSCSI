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
package org.jscsi.initiator;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jscsi.exception.NoSuchSessionException;
import org.jscsi.exception.TaskExecutionException;
import org.jscsi.initiator.connection.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Initiator</h1>
 * <p>
 * This class represents an initiator, which request messages to a target defined by the iSCSI Protocol
 * (RFC3720).
 * 
 * @author Volker Wildi, University of Konstanz
 * @author Sebastian Graf, University of Konstanz
 */
public final class Initiator {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The Logger interface. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Initiator.class);

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Stores all configuration parameters. */
    private final Configuration configuration;

    /** Stores all opened sessions. */
    private final Map<String, Session> sessions;

    /** Stores all configuration parameters. */
    private final LinkFactory factory;

    /**
     * Constructor to create an empty <code>Initiator</code> object with the
     * given configuration.
     * 
     * @param initConfiguration
     *            The user-defined configuration file.
     */
    public Initiator(final Configuration initConfiguration) {

        configuration = initConfiguration;
        sessions = new Hashtable<String, Session>(1);
        factory = new LinkFactory(this);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Creates a new session with the given target name, which is read from the
     * configuration file.
     * 
     * @param targetName
     *            The name of the iSCSI Target to connect.
     * @throws NoSuchSessionException
     *             if no session was found
     * 
     */
    public final void createSession(final String targetName) throws NoSuchSessionException {

        createSession(configuration.getTargetAddress(targetName), targetName);
    }

    /**
     * Creates a new session to a target with the given Internet address and
     * port. The target has the name <code>targetName</code>.
     * 
     * @param targetAddress
     *            The Internet address and Port of the target.
     * @param targetName
     *            Name of the target, to which a connection should be created.
     * @throws Exception
     *             if any error occurs.
     */
    public final void createSession(final InetSocketAddress targetAddress, final String targetName) {

        final Session session = factory.getSession(configuration, targetName, targetAddress);
        sessions.put(session.getTargetName(), session);
        LOGGER.info("Created the session with iSCSI Target '" + targetName + "' at "
            + targetAddress.getHostName() + " on port " + targetAddress.getPort() + ".");
    }

    /**
     * Closes all opened connections within this session to the given target.
     * 
     * @param targetName
     *            The name of the target, which connection should be closed.
     * @throws NoSuchSessionException
     *             if no session is accessible
     * @throws TaskExecutionException
     *             if logout fails.
     */
    public final void closeSession(final String targetName) throws NoSuchSessionException,
        TaskExecutionException {

        getSession(targetName).logout();
        // TODO Test the removal from the map.
        sessions.remove(targetName);

        LOGGER.info("Closed the session to the iSCSI Target '" + targetName + "'.");
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Invokes a read operation for the session <code>targetName</code> and
     * store the read bytes in the buffer <code>dst</code>. Start reading at the
     * logical block address and request <code>transferLength</code> blocks.
     * 
     * 
     * @param targetName
     *            The name of the session to invoke this read operation.
     * @param dst
     *            The buffer to store the read data.
     * @param logicalBlockAddress
     *            The logical block address of the beginning.
     * @param transferLength
     *            Number of bytes to read.
     * @throws Exception
     *             if any error occurs.
     * 
     * @return FutureObject for MultiThreadedReads
     * @throws TaskExecutionException
     *             if execution fails
     * @throws NoSuchSessionException
     *             if session is not found
     */
    public final Future<Void> multiThreadedRead(final String targetName, final ByteBuffer dst,
        final int logicalBlockAddress, final long transferLength) throws NoSuchSessionException,
        TaskExecutionException {

        final Future<Void> returnVal = getSession(targetName).read(dst, logicalBlockAddress, transferLength);
        return returnVal;
    }

    /**
     * Invokes a read operation for the session <code>targetName</code> and
     * store the read bytes in the buffer <code>dst</code>. Start reading at the
     * logical block address and request <code>transferLength</code> blocks.
     * 
     * 
     * @param targetName
     *            The name of the session to invoke this read operation.
     * @param dst
     *            The buffer to store the read data.
     * @param logicalBlockAddress
     *            The logical block address of the beginning.
     * @param transferLength
     *            Number of bytes to read.
     * @throws TaskExecutionException
     *             if execution fails
     * @throws NoSuchSessionException
     *             if session is not found
     */
    public final void read(final String targetName, final ByteBuffer dst, final int logicalBlockAddress,
        final long transferLength) throws NoSuchSessionException, TaskExecutionException {
        try {
            multiThreadedRead(targetName, dst, logicalBlockAddress, transferLength).get();
        } catch (final InterruptedException exc) {
            throw new TaskExecutionException(exc);
        } catch (final ExecutionException exc) {
            throw new TaskExecutionException(exc);
        }
    }

    /**
     * Invokes a write operation for the session <code>targetName</code> and
     * transmits the bytes in the buffer <code>dst</code>. Start writing at the
     * logical block address and transmit <code>transferLength</code> blocks.
     * 
     * 
     * @param targetName
     *            The name of the session to invoke this write operation.
     * @param src
     *            The buffer to transmit.
     * @param logicalBlockAddress
     *            The logical block address of the beginning.
     * @param transferLength
     *            Number of bytes to write.
     * @throws Exception
     *             if any error occurs.
     * @return FutureObject for the multi-threaded write operation
     * @throws TaskExecutionException
     *             if execution fails
     * @throws NoSuchSessionException
     *             if session is not found
     */
    public final Future<Void> multiThreadedWrite(final String targetName, final ByteBuffer src,
        final int logicalBlockAddress, final long transferLength) throws NoSuchSessionException,
        TaskExecutionException {

        return getSession(targetName).write(src, logicalBlockAddress, transferLength);
    }

    /**
     * Invokes a write operation for the session <code>targetName</code> and
     * transmits the bytes in the buffer <code>dst</code>. Start writing at the
     * logical block address and transmit <code>transferLength</code> blocks.
     * 
     * 
     * @param targetName
     *            The name of the session to invoke this write operation.
     * @param src
     *            The buffer to transmit.
     * @param logicalBlockAddress
     *            The logical block address of the beginning.
     * @param transferLength
     *            Number of bytes to write.
     * @throws TaskExecutionException
     *             if execution fails
     * @throws NoSuchSessionException
     *             if session is not found
     */
    public final void write(final String targetName, final ByteBuffer src, final int logicalBlockAddress,
        final long transferLength) throws NoSuchSessionException, TaskExecutionException {

        try {
            multiThreadedWrite(targetName, src, logicalBlockAddress, transferLength).get();
        } catch (final InterruptedException exc) {
            throw new TaskExecutionException(exc);
        } catch (final ExecutionException exc) {
            throw new TaskExecutionException(exc);
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the used block size (in bytes) of the iSCSI Target.
     * 
     * @param targetName
     *            The name of the session to invoke this capacity operation.
     * @return the used block size (in bytes) of the connected iSCSI Target.
     * @throws NoSuchSessionException
     *             if the session connected to the target is yet not open.
     */
    public final long getBlockSize(final String targetName) throws NoSuchSessionException {

        return getSession(targetName).getBlockSize();
    }

    /**
     * Returns the capacity (in blocks) of the iSCSI Target.
     * 
     * @param targetName
     *            The name of the session to invoke this capacity operation.
     * @return the capacity in blocks of the connected iSCSI Target.
     * @throws NoSuchSessionException
     *             if the session connected to the target is yet not open.
     */
    public final long getCapacity(final String targetName) throws NoSuchSessionException {

        return getSession(targetName).getCapacity();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the <code>Session</code> instance of the iSCSI Target with the
     * given name.
     * 
     * @param targetName
     *            The name of the session, which instance you want.
     * @return The requested <code>Session</code> instance.
     */
    private final Session getSession(final String targetName) throws NoSuchSessionException {

        final Session session = sessions.get(targetName);

        if (session != null) {
            return session;
        } else {
            throw new NoSuchSessionException("Session " + targetName + " not found!");
        }
    }

    /**
     * Removes the <code>Session</code> instances form the sessions queue.
     * 
     * @param sessionReq
     *            The Session to remove
     * @throws NoSuchSessionException
     *             if the Session does not exist in the Map
     */
    public final void removeSession(final Session sessionReq) throws NoSuchSessionException {

        final Session session = sessions.get(sessionReq.getTargetName());

        if (session != null) {
            sessions.remove(sessionReq.getTargetName());
        } else {
            throw new NoSuchSessionException("Session " + sessionReq.getTargetName() + " not found!");
        }
    }

    /**
     * TODO Search a better solution for this. How can we notify the
     * Application, that all Sessions are closed (and all Tasks are finished)
     */
    /**
     * is the Sessions Map empty?.
     * 
     * @return true if it is empty
     */
    public final Boolean noSessions() {

        return (sessions.size() == 0);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

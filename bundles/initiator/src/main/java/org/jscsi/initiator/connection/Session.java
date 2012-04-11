/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
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
/**
 * 
 */

package org.jscsi.initiator.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.initiator.Configuration;
import org.jscsi.initiator.LinkFactory;
import org.jscsi.initiator.connection.phase.IPhase;
import org.jscsi.initiator.connection.phase.SecurityNegotiationPhase;
import org.jscsi.initiator.connection.state.LoginRequestState;
import org.jscsi.initiator.taskbalancer.AbstractTaskBalancer;
import org.jscsi.initiator.taskbalancer.SimpleTaskBalancer;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.SettingsMap;
import org.jscsi.parser.exception.NoSuchConnectionException;
import org.jscsi.parser.login.LoginStage;
import org.jscsi.utils.SerialArithmeticNumber;

/**
 * <h1>AbsSession</h1>
 * <p/>
 * A session or Initiator Target Nexus is a directed communication from an iSCSI
 * Initiator to an iSCSI Target. Each session can contain several connections.
 * This allows a better usage of bandwidth and decreases latency times. The
 * Abstract Class is used to implement serveral single- and multithreaded
 * variants of Sessions
 * 
 * @author Volker Wildi, Patrice Matthias Brend'amour
 */
public final class Session {

    /** The unique name of the connected iSCSI Target. */
    protected final String targetName;

    /** The unique name of the connected iSCSI Target. */
    protected final InetSocketAddress inetSocketAddress;

    /** The maximum number of connections, which are allowed in this session. */
    private int maxConnections;

    /** The index of the next used connection ID. */
    protected short nextFreeConnectionID;

    /** The session is in this phase. */
    protected IPhase phase;

    /**
     * This instance contains the informations about the capacity of the
     * connected target.
     */
    protected final TargetCapacityInformations capacityInformations;

    /** A List object with all open connections. */
    protected final LinkedBlockingQueue<Connection> connections;

    /** The Command Sequence Number of this session. */
    protected final SerialArithmeticNumber commandSequenceNumber;

    /** The Maximum Command Sequence Number of this session. */
    protected final SerialArithmeticNumber maximumCommandSequenceNumber;

    /**
     * The initiator uses this Initiator Task Tag to relate data to the
     * appropriate command. And the target uses this tag to correlate the data
     * to the appropriate command that it received earlier.
     */
    protected final SerialArithmeticNumber initiatorTaskTag;

    /**
     * Flag to indicate, if the login phase of this session is successfully
     * completed. This flag is also used for the protection of a reseting of
     * <code>targetSessionIdentifyingHandle</code>.
     */
    protected boolean tsihChanged;

    /** The Target Session Identifying Handle. */
    protected short targetSessionIdentifyingHandle;

    /** The Logger interface. */
    protected static final Log LOGGER = LogFactory.getLog(Session.class);

    /** The <code>Configuration</code> instance for this session. */
    protected final Configuration configuration;

    /** The <code>LinkFactory</code> instance for this session. */
    protected final LinkFactory factory;

    /** Executor to work with all task to be commited. */
    private final ExecutorService executor;

    /** Contains all queues, which are till now not successfully finished. */
    // FIXME: Support me!
    private final ConcurrentHashMap<ITask, Connection> outstandingTasks;

    /**
     * Handles the load balancing of the task distribution to the opened
     * connections.
     */
    protected final AbstractTaskBalancer taskBalancer;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new, empty <code>AbsSession</code> object with a
     * maximum number of allowed connections to a given iSCSI Target. This is
     * the abstract definition for Session implementations
     * 
     * @param linkFactory
     *            The LinkFactory which called the Constructor
     * @param initConfiguration
     *            The configuration to use within this session.
     * @param initTargetName
     *            The name of the iSCSI Target.
     * @param inetAddress
     *            The <code>InetSocketAddress</code> of the leading connection
     *            of this session.
     * @param initExecutor
     *            The <code>ExecutorService</code> for the Connection Threads
     * @throws Exception
     *             if anything happens
     */

    public Session(final LinkFactory linkFactory,
            final Configuration initConfiguration, final String initTargetName,
            final InetSocketAddress inetAddress,
            final ExecutorService initExecutor) throws Exception {

        maxConnections = Integer.parseInt(initConfiguration.getSessionSetting(
                initTargetName, OperationalTextKey.MAX_CONNECTIONS));
        factory = linkFactory;
        configuration = initConfiguration;
        commandSequenceNumber = new SerialArithmeticNumber();
        maximumCommandSequenceNumber = new SerialArithmeticNumber(1);
        nextFreeConnectionID = 1;
        inetSocketAddress = inetAddress;
        initiatorTaskTag = new SerialArithmeticNumber(1);
        targetName = initTargetName;
        phase = new SecurityNegotiationPhase();
        capacityInformations = new TargetCapacityInformations();
        connections = new LinkedBlockingQueue<Connection>(maxConnections);
        executor = initExecutor;
        taskBalancer = new SimpleTaskBalancer(connections);
        outstandingTasks = new ConcurrentHashMap<ITask, Connection>();

        // Add the leading connection
        addNewConnection();

        /*
         * We have to check whether the MaxConnection setting in our
         * Configuration is correct. There might be a wrong setting for a target
         * e.g. the target only supports one connection but we think it can
         * handle two.
         */
        maxConnections = Integer.parseInt(configuration.getSessionSetting(
                targetName, OperationalTextKey.MAX_CONNECTIONS));
        int targetMaxC = connections.peek().getSettingAsInt(
                OperationalTextKey.MAX_CONNECTIONS);
        if (targetMaxC < maxConnections) {
            maxConnections = targetMaxC;
        }

        // Add more Connections
        // TODO Do something more intelligent here. Always adding the maximum
        // isn't
        // always a good idea
        addConnections(maxConnections - 1);

    }

    /**
     * Returns the Target Session Identifying Handle (TSID) of this
     * <code>Session</code> object.
     * 
     * @return The current Target Session Identifying Handle (TSIH)
     */
    public final short getTargetSessionIdentifyingHandle() {

        return targetSessionIdentifyingHandle;
    }

    /**
     * Sets the Target Session Identifying Handle (TSIH) to the given value.
     * This TSIH is specified at the Login Phase by the target in a new session.
     * So, it can only set one time.
     * 
     * @param tsih
     *            The new Target Session Identifying Handle.
     */
    public final void setTargetSessionIdentifyingHandle(final short tsih) {

        if (!tsihChanged) {
            targetSessionIdentifyingHandle = tsih;
            tsihChanged = true;
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the Command Sequence Number of this session.
     * 
     * @return The current Command Sequence Number.
     */
    public final int getCommandSequenceNumber() {

        return commandSequenceNumber.getValue();
    }

    /**
     * Sets the Maximum Command Sequence Number to a new value.
     * 
     * @param newMaximumCommandSequenceNumber
     *            The new Maximum Command Sequence Number.
     */
    public final void setMaximumCommandSequenceNumber(
            final int newMaximumCommandSequenceNumber) {

        maximumCommandSequenceNumber.setValue(newMaximumCommandSequenceNumber);
    }

    /**
     * Returns the Maximum Command Sequence Number of this session.
     * 
     * @return The current Maximum Command Sequence Number.
     */
    public final SerialArithmeticNumber getMaximumCommandSequenceNumber() {

        return maximumCommandSequenceNumber;
    }

    /**
     * Returns the Initiator Task Tag of this session.
     * 
     * @return The Initiator Task Tag.
     */
    public final int getInitiatorTaskTag() {

        return initiatorTaskTag.getValue();
    }

    /**
     * Increments the Initiator Task Tag as defined in RFC1982 where
     * <code>SERIAL_BITS = 32</code>.
     */
    public final void incrementInitiatorTaskTag() {

        initiatorTaskTag.increment();
    }

    /**
     * Has the iSCSI Target enough resources to accept more incoming PDU?
     * 
     * @return <code>true</code>, if the iSCSI Target has enough resources to
     *         accept more incoming PDUs. Else <code>false</code> and hold out
     *         for sending.
     */
    public final boolean hasTargetMoreResources() {

        return maximumCommandSequenceNumber.compareTo(commandSequenceNumber
                .getValue()) > 0;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the name of the iSCSI Target of this session.
     * 
     * @return The name of the iSCSI Target.
     */
    public final String getTargetName() {

        return targetName;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Adds a number of new connections to this session.
     * 
     * @param max
     *            The number of Connections to open.
     * @throws Exception
     *             if any error occurs.
     */
    public final void addConnections(final int max) throws Exception {

        if (connections.size() < maxConnections) {
            for (int i = 1; i < max; i++) {
                addNewConnection();
            }
        }
    }

    /**
     * Adds a new connection to this session with the next free connection ID
     * (if the maximum number is not reached).
     * 
     * @return The connection ID of the newly created connection.
     * @throws Exception
     *             if any error occurs.
     */
    protected final short addNewConnection() throws Exception {

        if (connections.size() < maxConnections) {

            final Connection connection = factory.getConnection(this,
                    configuration, inetSocketAddress, nextFreeConnectionID);
            connection.nextState(new LoginRequestState(connection,
                    LoginStage.FULL_FEATURE_PHASE));
            // login phase successful, so we can add a new connection
            connections.add(connection);

            // only needed on the leading login connection
            if (connections.size() == 1) {
                phase.getCapacity(this, capacityInformations);

                if (connection
                        .getSettingAsInt(OperationalTextKey.MAX_CONNECTIONS) > 1) {
                    phase.login(this);
                }
            }

            return nextFreeConnectionID++;
        } else {
            LOGGER.warn("Unused new connection -> ignored!");
            return nextFreeConnectionID;
        }

    }

    /**
     * Updates the MaxConnection setting, so that it grows/shrinks the
     * Connectionlist.
     * 
     * @param max
     *            The maximum number of concurrent <code>Connections</code> to a
     *            target.
     */
    public void updateMaxConnections(final int max) {

        try {
            Connection conn = taskBalancer.getConnection();
            int update = 0;
            int targetMaxC = connections.peek().getSettingAsInt(
                    OperationalTextKey.MAX_CONNECTIONS);
            if (targetMaxC <= max) {
                if (targetMaxC > maxConnections) {
                    update = targetMaxC - maxConnections;
                    maxConnections = targetMaxC;
                }
            } else {
                if (max >= maxConnections) {
                    update = max - maxConnections;
                    maxConnections = max;
                }
            }

            SettingsMap sm = new SettingsMap();
            sm.add(OperationalTextKey.MAX_CONNECTIONS,
                    String.valueOf(maxConnections));
            conn.update(sm);
            taskBalancer.releaseConnection(conn);

            if (update > 0) {
                addConnections(update);
            } else {
                for (int i = -1; i >= update; i--) {
                    taskBalancer.getConnection().close();
                }
            }

        } catch (Exception e) {
            // DO Nothing
        }

    }

    /**
     * Returns the next free <code>Connection</code> object of this
     * <code>Session</code> object.
     * 
     * @return The connection to use for the next task.
     * @throws NoSuchConnectionException
     *             If there is no such connection.
     */
    public final Connection getNextFreeConnection()
            throws NoSuchConnectionException {

        return taskBalancer.getConnection();
    }

    /**
     * Returns the connection with the given ID.
     * 
     * @param connectionID
     *            The ID of the connection to open.
     * @return The connection with the given ID. Else <code>null</code>.
     */
    public final Connection getConnection(final short connectionID) {

        for (Connection c : connections) {
            if (c.getConnectionID() == connectionID) {
                return c;
            }
        }
        // the given connection id is valid in this session.
        return null;

    }

    /**
     * Increments the Command Sequence Number as defined in RFC1982, where
     * <code>SERIAL_BITS = 32</code>.
     */
    public final void incrementCommandSequenceNumber() {

        commandSequenceNumber.increment();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Closes this session instances with all opened connections.
     * 
     * @throws IOException
     *             if an I/O error occurs.
     */
    public final void close() throws IOException {

        LOGGER.info("Closing was requested.");

        for (Connection c : connections) {
            c.close();
        }

        connections.clear();

        // stop session task thread
        factory.closedSession(this);
        executor.shutdown();
    }

    /**
     * Returns the used block size of the connected iSCSI Target.
     * 
     * @return The used block size in bytes.
     */
    public final long getBlockSize() {

        return capacityInformations.getBlockSize();
    }

    /**
     * Returns the capacity (in blocks) of the connected iSCSI Target.
     * 
     * @return The capacity in blocks.
     */
    public final long getCapacity() {

        return capacityInformations.getSize();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This method invokes the same called method of the current
     * <code>IPhase</code> instance.
     * 
     * @param caller
     *            The pointer to the calling instance of this method.
     * @throws Exception
     *             if any error occurs.
     */
    public final void login(final Object caller) throws Exception {

        executeTask(new LoginTask(caller, this));
    }

    /**
     * This method invokes the same called method of the current
     * <code>IPhase</code> instance.
     * 
     * @param caller
     *            The pointer to the calling instance of this method.
     * @throws Exception
     *             if any error occurs.
     */
    public final void logout(final Object caller) throws Exception {

        executeTask(new LogoutTask(caller, this));
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This method invokes the same called method of the current
     * <code>IPhase</code> instance.
     * 
     * @param caller
     *            The pointer to the calling instance of this method.
     * @param dst
     *            Store the read bytes to this buffer.
     * @param logicalBlockAddress
     *            The logical block address of the device to begin the read
     *            operation.
     * @param transferLength
     *            The number of bytes to read from the device.
     * @throws Exception
     *             if any error occurs.
     * @return The Future object.
     */
    public final Future<Void> read(final Object caller, final ByteBuffer dst,
            final int logicalBlockAddress, final long transferLength)
            throws Exception {

        return executeTask(new ReadTask(caller, this, dst, logicalBlockAddress,
                transferLength));
    }

    /**
     * This method invokes the same called method of the current
     * <code>IPhase</code> instance.
     * 
     * @param caller
     *            The pointer to the calling instance of this method.
     * @param src
     *            Write the remaining bytes to the device.
     * @param logicalBlockAddress
     *            The logical block address of the device to begin the write
     *            operation.
     * @param transferLength
     *            The number of bytes to write to the device.
     * @throws Exception
     *             if any error occurs.
     * @return The Future object.
     */
    public final Future<Void> write(final Object caller, final ByteBuffer src,
            final int logicalBlockAddress, final long transferLength)
            throws Exception {

        return executeTask(new WriteTask(caller, this, src,
                logicalBlockAddress, transferLength));
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the current <code>LoginStage</code> object.
     * 
     * @return The instance to the current <code>LoginStage</code>.
     */
    public final LoginStage getPhase() {

        return phase.getStage();
    }

    /**
     * This method sets the current <code>IPhase</code> instance to the given
     * value.
     * 
     * @param newPhase
     *            The new instance to switch to.
     */
    public final void setPhase(final IPhase newPhase) {

        phase = newPhase;
        LOGGER.trace("Switching to phase "
                + newPhase.getClass().getSimpleName());

    }

    /**
     * This methods appends the given task to the end of the taskQueue and set
     * the calling thread is sleep state.
     * 
     * @param task
     *            The task to append to the end of the taskQueue.
     * @throws InterruptedException
     *             if another thread interrupted the current thread before or
     *             while the current thread was waiting for a notification. The
     *             interrupted status of the current thread is cleared when this
     *             exception is thrown.
     * @throws ExecutionException
     */
    private final Future<Void> executeTask(final ITask task)
            throws InterruptedException, ExecutionException {

        try {
            if (task instanceof IOTask) {
                final Future<Void> returnVal = executor.submit((IOTask) task);
                return returnVal;
            } else {
                task.call();
                return null;
            }
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
        // LOGGER.info("Added a " + task + " to the TaskQueue");
    }

    /**
     * removes Task from outstandingTasks.
     * 
     * @param ftask
     *            The Task which was finished .
     */
    public final void finishedTask(final ITask ftask) {

        try {
            taskBalancer.releaseConnection(outstandingTasks.get(ftask));
        } catch (NoSuchConnectionException e) {
            e.printStackTrace();
        }
        outstandingTasks.remove(ftask);
        LOGGER.debug("Finished a " + ftask + " for the session " + targetName);
    }

    /**
     * restarts a Task from outstandingTasks.
     * 
     * @param task
     *            The failed Task.
     * @throws ExecutionException
     *             for failed restart of the task
     */
    public final void restartTask(final ITask task) throws ExecutionException {

        try {
            if (task != null) {
                if (task instanceof IOTask) {
                    executor.submit((IOTask) task);
                } else {
                    task.call();
                }
                taskBalancer.releaseConnection(outstandingTasks.get(task));
                outstandingTasks.remove(task);
            }
            LOGGER.debug("Restarted a Task out of the outstandingTasks Queue");
        } catch (Exception e) {
            throw new ExecutionException(e);
        }
    }

    /**
     * Adds a Task to the outstandingTasks Hashmap.
     * 
     * @param connection
     *            The Connection where the Task will be started
     * @param task
     *            The Task which was started.
     */
    public final void addOutstandingTask(final Connection connection,
            final ITask task) {

        outstandingTasks.put(task, connection);
        LOGGER.debug("Added a Task to the outstandingTasks Queue");
    }

    /**
     * Adds a Task to the outstandingTasks Hashmap.
     * 
     * @param connection
     *            The Connection which will be released
     * @throws NoSuchConnectionException
     *             if any errors occur
     */
    public final void releaseUsedConnection(final Connection connection)
            throws NoSuchConnectionException {

        taskBalancer.releaseConnection(connection);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

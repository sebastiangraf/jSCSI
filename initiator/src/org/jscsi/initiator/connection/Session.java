/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id: Session.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.initiator.connection;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.core.utils.SerialArithmeticNumber;
import org.jscsi.initiator.Configuration;
import org.jscsi.initiator.tasks.balancer.ITaskBalancer;
import org.jscsi.initiator.tasks.balancer.SimpleTaskBalancer;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.exception.NoSuchConnectionException;
import org.jscsi.parser.login.LoginStage;

/**
 * <h1>Session</h1>
 * <p/>
 * 
 * A session or Initiator Target Nexus is a directed communication from an iSCSI
 * Initiator to an iSCSI Target. Each session can contains of several
 * connections. This allows a better usage of bandwidth and decreases latency
 * times.
 * 
 * @author Volker Wildi
 * @see org.jscsi.Connection
 */
public final class Session implements Runnable {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The Logger interface. */
    private static final Log LOGGER = LogFactory.getLog(Session.class);

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The <code>Configuration</code> instance for this session. */
    private final Configuration configuration;

    /** The session is in this phase. */
    private IPhase phase;

    /** Connection with all open tasks of this session. */
    private final Queue<ITask> taskQueue;

    /** Contains all queues, which are till now not successfully finished. */
    // FIXME: Support me!
    // private final Queue<ITask> outstandingTasks;
    /** The unique name of the connected iSCSI Target. */
    private final String targetName;

    /** The Command Sequence Number of this session. */
    private final SerialArithmeticNumber commandSequenceNumber;

    /** The Maximum Command Sequence Number of this session. */
    private final SerialArithmeticNumber maximumCommandSequenceNumber;

    /** The maximum number of connections, which are allowed in this session. */
    private final int maxConnections;

    /** The index of the next used connection ID. */
    private short nextFreeConnectionID;

    /**
     * The initiator uses this Initiator Task Tag to relate data to the
     * appropriate command. And the target uses this tag to correlate the data
     * to the appropriate command that it received earlier.
     */
    private final SerialArithmeticNumber initiatorTaskTag;

    /**
     * This instance contains the informations about the capacity of the
     * connected target.
     */
    private final TargetCapacityInformations capacityInformations;

    /** A List object with all open connections. */
    private final List<Connection> connections;

    /**
     * Handles the load balancing of the task distribution to the opened
     * connections.
     */
    private final ITaskBalancer taskBalancer;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Flag to indicate, if the login phase of this session is successfully
     * completed. This flag is also used for the protection of a reseting of
     * <code>targetSessionIdentifyingHandle</code>.
     */
    private boolean tsihChanged;

    /** The Target Session Identifying Handle. */
    private short targetSessionIdentifyingHandle;

    /** Flag, which indicates to stop the thread. */
    private boolean stop;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new, empty <code>Session</code> object with a
     * maximum number of allowed connections to a given iSCSI Target.
     * 
     * @param initConfiguration
     *            The configuration to use within this session.
     * @param initTargetName
     *            The name of the iSCSI Target.
     * @param inetAddress
     *            The <code>InetAddress</code> of the leading connection of
     *            this session.
     * @param port
     *            The TCP port of the listening iSCSI Target to connect to.
     * @param initMaxConnections
     *            The number of maximum allowed connections.
     * @throws Exception
     *             if any error occurs.
     */
    public Session(final Configuration initConfiguration, final String initTargetName, final InetAddress inetAddress, final int port,
            final int initMaxConnections) throws Exception {

        if (initMaxConnections > Short.MAX_VALUE) {
            throw new IllegalArgumentException("The Maximum number cannot be greater than " + Short.MAX_VALUE);
        }

        configuration = initConfiguration;
        targetName = initTargetName;
        maxConnections = initMaxConnections;

        phase = new SecurityNegotiationPhase();
        taskQueue = new LinkedList<ITask>();
        // outstandingTasks = new LinkedList<ITask>();

        capacityInformations = new TargetCapacityInformations();

        commandSequenceNumber = new SerialArithmeticNumber();
        maximumCommandSequenceNumber = new SerialArithmeticNumber(1);
        nextFreeConnectionID = 1;
        initiatorTaskTag = new SerialArithmeticNumber(1);

        connections = new ArrayList<Connection>(maxConnections);
        taskBalancer = new SimpleTaskBalancer(connections);

        new Thread(this, "SessionTaskThread for iSCSI Target " + targetName).start();

        LOGGER.debug("SessionTaskThread started.");

        addNewConnection(inetAddress, port);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Adds a new connection to this session with the next free connection ID
     * (if the maximum number is not reached).
     * 
     * @param inetAddress
     *            The leading connection address of the used iSCSI endpoint.
     * @param port
     *            The TCP port used for the leading connection.
     * @return The connection ID of the newly created connection.
     * @throws Exception
     *             if any error occurs.
     */
    private final short addNewConnection(final InetAddress inetAddress, final int port) throws Exception {

        if (connections.size() > maxConnections) {
            throw new InternetSCSIException("There are no more connections available.");
        }

        final Connection connection = new Connection(this, configuration, inetAddress, port, nextFreeConnectionID);
        connection.execute();
        // login phase successful, so we can add a new connection
        synchronized (connections) {
            connections.add(connection);

            // only needed on the leading login connection
            if (connections.size() == 1) {
                phase.getCapacity(this, capacityInformations);

                if (connection.getSettingAsInt(OperationalTextKey.MAX_CONNECTIONS) > 1) {
                    phase.login(this);
                }
            }
        }

        return nextFreeConnectionID++;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the Target Session Identifying Handle (TSID) of this
     * <code>Session</code> object.
     * 
     * @return The current Target Session Identifying Handle (TSIH)
     */
    final short getTargetSessionIdentifyingHandle() {

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
    final void setTargetSessionIdentifyingHandle(final short tsih) {

        if (!tsihChanged) {
            targetSessionIdentifyingHandle = tsih;
            tsihChanged = true;
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Increments the Command Sequence Number as defined in RFC1982, where
     * <code>SERIAL_BITS = 32</code>.
     */
    final void incrementCommandSequenceNumber() {

        commandSequenceNumber.increment();
    }

    /**
     * Returns the Command Sequence Number of this session.
     * 
     * @return The current Command Sequence Number.
     */
    final int getCommandSequenceNumber() {

        return commandSequenceNumber.getValue();
    }

    /**
     * Sets the Maximum Command Sequence Number to a new value.
     * 
     * @param newMaximumCommandSequenceNumber
     *            The new Maximum Command Sequence Number.
     */
    final void setMaximumCommandSequenceNumber(final int newMaximumCommandSequenceNumber) {

        maximumCommandSequenceNumber.setValue(newMaximumCommandSequenceNumber);
    }

    /**
     * Returns the Maximum Command Sequence Number of this session.
     * 
     * @return The current Maximum Command Sequence Number.
     */
    final SerialArithmeticNumber getMaximumCommandSequenceNumber() {

        return maximumCommandSequenceNumber;
    }

    /**
     * Returns the Initiator Task Tag of this session.
     * 
     * @return The Initiator Task Tag.
     */
    final int getInitiatorTaskTag() {

        return initiatorTaskTag.getValue();
    }

    /**
     * Increments the Initiator Task Tag as defined in RFC1982 where
     * <code>SERIAL_BITS = 32</code>.
     */
    final void incrementInitiatorTaskTag() {

        initiatorTaskTag.increment();
    }

    /**
     * Has the iSCSI Target enough resources to accept more incoming PDU?
     * 
     * @return <code>true</code>, if the iSCSI Target has enough resources to
     *         accept more incoming PDUs. Else <code>false</code> and hold out
     *         for sending.
     */
    final boolean hasTargetMoreResources() {

        return maximumCommandSequenceNumber.compareTo(commandSequenceNumber.getValue()) > 0;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the next free <code>Connection</code> object of this
     * <code>Session</code> object.
     * 
     * @return The connection to use for the next task.
     * 
     * @throws NoSuchConnectionException
     *             If there is no such connection.
     */
    final Connection getNextFreeConnection() throws NoSuchConnectionException {

        synchronized (taskBalancer) {
            return taskBalancer.getConnection();
        }
    }

    /**
     * Returns the connection with the given ID.
     * 
     * @param connectionID
     *            The ID of the connection to open.
     * @return The connection with the given ID. Else <code>null</code>.
     */
    final Connection getConnection(final short connectionID) {

        synchronized (connections) {
            for (Connection c : connections) {
                if (c.getConnectionID() == connectionID) {
                    return c;
                }
            }
        }

        // the given connection id is valid in this session.
        return null;
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

        synchronized (connections) {
            for (Connection c : connections) {
                synchronized (c) {
                    c.close();
                }
            }

            connections.clear();
        }

        // stop session task thread
        stop = true;
    }

    /**
     * Returns the name of the iSCSI Target of this session.
     * 
     * @return The name of the iSCSI Target.
     */
    public final String getTargetName() {

        return targetName;
    }

    /**
     * Returns the used block size of the connected iSCSI Target.
     * 
     * @return The used block size in bytes.
     */
    public final long getBlockSize() {

        synchronized (capacityInformations) {
            return capacityInformations.getBlockSize();
        }
    }

    /**
     * Returns the capacity (in blocks) of the connected iSCSI Target.
     * 
     * @return The capacity in blocks.
     */
    public final long getCapacity() {

        synchronized (capacityInformations) {
            return capacityInformations.getSize();
        }
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
    public final synchronized void login(final Object caller) throws Exception {

        appendAndSleep(new LoginTask(caller, this));
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
    public final synchronized void logout(final Object caller) throws Exception {

        appendAndSleep(new LogoutTask(caller, this));
    }

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
     */
    public final void read(final Object caller, final ByteBuffer dst, final int logicalBlockAddress, final long transferLength) throws Exception {

        appendAndSleep(new ReadTask(caller, this, dst, logicalBlockAddress, transferLength));
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
     */
    public final void write(final Object caller, final ByteBuffer src, final int logicalBlockAddress, final long transferLength) throws Exception {

        appendAndSleep(new WriteTask(caller, this, src, logicalBlockAddress, transferLength));
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the current <code>LoginStage</code> object.
     * 
     * @return The instance to the current <code>LoginStage</code>.
     */
    public final LoginStage getPhase() {

        synchronized (phase) {
            return phase.getStage();
        }
    }

    /**
     * This method sets the current <code>IPhase</code> instance to the given
     * value.
     * 
     * @param newPhase
     *            The new instance to switch to.
     */
    final void setPhase(final IPhase newPhase) {

        synchronized (phase) {
            LOGGER.trace("Switching to phase " + newPhase.getClass().getSimpleName());

            phase = newPhase;
        }
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
     */
    private final void appendAndSleep(final ITask task) throws InterruptedException {

        synchronized (task) {
            // asynchronously execute queue
            synchronized (taskQueue) {
                taskQueue.add(task);
            }

            LOGGER.info("Added a " + task + " to the task queue of the session " + targetName);

            // synchronously block _this_ method call
            synchronized (task.getCaller()) {
                task.getCaller().wait();
            }
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final void run() {

        ITask head;
        while (!stop) {

            synchronized (taskQueue) {
                head = taskQueue.poll();
            }

            if (head != null) {
                try {
                    // synchronized (outstandingTasks) {
                    // outstandingTasks.add(head);
                    // }
                    head.execute();
                    final Object thread = head.getCaller();
                    // TODO: Sure?
                    // outstandingTasks.remove(head);

                    LOGGER.info("Polled a " + head.getClass().getSimpleName() + " form the task queue of the session " + targetName);

                    synchronized (thread) {
                        thread.notify();
                    }
                } catch (Exception e) {
                    LOGGER.error("This exception is thrown: " + e);
                    e.printStackTrace();

                    stop();
                }
            }

            Thread.yield();
        }

        LOGGER.debug("SessionTaskThread for session " + targetName + " is stopped.");
    }

    /**
     * Stops this caller instance.
     */
    public final void stop() {

        stop = true;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * <h1>ITask</h1>
     * <p/>
     * 
     * This interface defines all methods, which a task has to support.
     * 
     * @author Volker Wildi
     */
    private interface ITask {

        /**
         * This method is call, when this <code>ITask</code> instance is
         * polled from the head of the <code>taskQueue</code> to start a task.
         * 
         * @throws Exception
         *             if any error occurs.
         */
        public void execute() throws Exception;

        /**
         * Returns the instance to the calling thread of this task.
         * 
         * @return the instance of the calling object.
         */
        public Object getCaller();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    private abstract class AbstractTask implements ITask {

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /** The invoking caller of this task. */
        protected final Object caller;

        /** The <code>Session</code> instance of this task. */
        protected final Session session;

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /**
         * Constructor to create a new, empty <code>AbstractTask</code>
         * subclass instance, which is initialized with the given values.
         * 
         * @param initCaller
         *            The invoking caller of this task.
         * @param referenceSession
         *            The session, where this task is executed in.
         */
        AbstractTask(final Object initCaller, final Session referenceSession) {

            caller = initCaller;
            session = referenceSession;
        }

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /** {@inheritDoc} */
        public final Object getCaller() {

            return caller;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString() {

            return getClass().getSimpleName();
        }

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    private final class LoginTask extends AbstractTask {

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /**
         * Constructor to create a new, empty <code>LoginTask</code> instance,
         * which is initialized with the given values.
         * 
         * @param initCaller
         *            The invoking caller of this task.
         * @param referenceSession
         *            The session, where this task is executed in.
         */
        LoginTask(final Object initCaller, final Session referenceSession) {

            super(initCaller, referenceSession);
        }

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /** {@inheritDoc} */
        public final void execute() throws Exception {

            phase.login(session);
        }

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    private final class LogoutTask extends AbstractTask {

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /**
         * Constructor to create a new, empty <code>LogoutTask</code>
         * instance, which is initialized with the given values.
         * 
         * @param initCaller
         *            The invoking caller of this task.
         * @param referenceSession
         *            The session, where this task is executed in.
         */
        LogoutTask(final Object initCaller, final Session referenceSession) {

            super(initCaller, referenceSession);
        }

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /** {@inheritDoc} */
        public final void execute() throws Exception {

            phase.logoutSession(session);
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    private abstract class IOTask extends AbstractTask {

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /** The buffer to store/read the data of this IOTask. */
        protected final ByteBuffer buffer;

        /** The logical block address of the start of this task. */
        protected final int logicalBlockAddress;

        /** The length (in bytes) of this task. */
        protected final long length;

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        IOTask(final Object initCaller, final Session referenceSession, final ByteBuffer dst, final int initLogicalBlockAddress, final long initLength) {

            super(initCaller, referenceSession);
            buffer = dst;
            logicalBlockAddress = initLogicalBlockAddress;
            length = initLength;
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    private final class ReadTask extends IOTask {

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /**
         * Constructor to create a new, empty <code>ReadTask</code> instance,
         * which is initialized with the given values.
         * 
         * @param initCaller
         *            The invoking caller of this task.
         * @param referenceSession
         *            The session, where this task is executed in.
         */
        ReadTask(final Object initCaller, final Session referenceSession, final ByteBuffer dst, final int initLogicalBlockAddress, final long initLength) {

            super(initCaller, referenceSession, dst, initLogicalBlockAddress, initLength);
        }

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /** {@inheritDoc} */
        public final void execute() throws Exception {

            phase.read(session, buffer, logicalBlockAddress, length);
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    private final class WriteTask extends IOTask {

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /**
         * Constructor to create a new, empty <code>WriteTask</code> instance,
         * which is initialized with the given values.
         * 
         * @param initCaller
         *            The invoking caller of this task.
         * @param referenceSession
         *            The session, where this task is executed in.
         */
        WriteTask(final Object initCaller, final Session referenceSession, final ByteBuffer src, final int initLogicalBlockAddress, final long initLength) {

            super(initCaller, referenceSession, src, initLogicalBlockAddress, initLength);
        }

        // --------------------------------------------------------------------------
        // --------------------------------------------------------------------------

        /** {@inheritDoc} */
        public final void execute() throws Exception {

            phase.write(session, buffer, logicalBlockAddress, length);
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

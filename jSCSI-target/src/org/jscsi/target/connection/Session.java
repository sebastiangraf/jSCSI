package org.jscsi.target.connection;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.conf.operationalText.OperationalTextConfiguration;
import org.jscsi.target.conf.operationalText.OperationalTextException;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.parameter.connection.SessionType;
import org.jscsi.target.task.SessionTaskRouter;
import org.jscsi.connection.SerialArithmeticNumber;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.login.ISID;
import org.jscsi.parser.login.LoginRequestParser;
import org.jscsi.parser.snack.SNACKRequestParser;

/**
 * <h1>Session</h1>
 * <p/>
 * 
 * A session or Initiator Target Nexus is a directed communication from an iSCSI
 * Initiator to an iSCSI Target. Each session can contains of several
 * connections. This allows a better usage of bandwidth and decreases latency
 * times.
 * 
 * @author Marcus Specht
 * 
 */
public class Session {

	/** The Logger interface. */
	private static final Log LOGGER = LogFactory.getLog(Session.class);

	public static final String IDENTIFIER = "Session";

	public static final String TARGET_SESSION_IDENTIFYING_HANDLE = "TargetSessionIdentifyingHandle";

	public static final String INITIATOR_SESSION_ID = "InitiatorSessionID";

	public static final String INITIATOR_NAME = "InitiatorName";

	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/** The <code>Configuration</code> instance for this session. */
	private final OperationalTextConfiguration configuration;

	/** The Target Session Identifying Handle. */
	private short targetSessionIdentifyingHandle;

	/** The Initiator Session ID */
	private ISID initiatorSessionID;

	private String initiatorName;

	/** the session's type */
	private SessionType sessionType;

	/** The Command Sequence Number of this session. */
	private SerialArithmeticNumber expectedCommandSequenceNumber;

	/** The Maximum Command Sequence Number of this session. */
	private SerialArithmeticNumber maximumCommandSequenceNumber;

	/** connections are mapped to their receiving Queues */
	private final Map<Connection, SortedMap<Integer, ProtocolDataUnit>> connections;

	private final SortedMap<Integer, Connection> signalledPDUs;

	private final Queue<ProtocolDataUnit> receivedPDUs;
	
	private final SessionTaskRouter taskRouter;

	/**
	 * the LOCK is used to synchronize every request for receiving PDUs
	 */
	private final Lock LOCK = new ReentrantLock();

	/**
	 * the Condition is used to signal waiting Threads for received PDUs
	 */
	private final Condition somethingReceived = LOCK.newCondition();

	public Session(Connection connection, short tsih)
			throws OperationalTextException {
		configuration = OperationalTextConfiguration.create(this);
		taskRouter = new SessionTaskRouter(this);
		targetSessionIdentifyingHandle = tsih;
		connections = new ConcurrentHashMap<Connection, SortedMap<Integer, ProtocolDataUnit>>();
		signalledPDUs = new TreeMap<Integer, Connection>();
		receivedPDUs = new ConcurrentLinkedQueue<ProtocolDataUnit>();
		sessionType = SessionType.Unknown;
		addConnection(connection);
		// start TaskManger
	}

	/**
	 * Adds a Connection to this Session.
	 * 
	 * @param connection
	 */
	public void addConnection(Connection connection) {
		if ((!connections.containsKey(connection))
				&& (connection.setReferencedSession(this))) {
			connections.put(connection, connection.getReceivingBuffer());
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER
						.debug("Tried to add a Connection which has already a referenced Session!");
			}
		}

	}

	/**
	 * Checks based on a Session's ISID and TSIH if those parameters are equals
	 * this session's ones. If so, the source <code>Connection</code> should
	 * be added to this <code>Session</code> instance.
	 * 
	 * @param isid
	 *            a Connection's Initiator Session ID
	 * @param tsih
	 *            a Session's Target Session Identifying Handle
	 * @return true if parameters are equal, false else.
	 */
	final boolean checkAppropriateConnection(ISID isid, short tsih) {
		if (isid.equals(initiatorSessionID)
				&& (tsih == targetSessionIdentifyingHandle)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean containsConnection(Connection connection) {
		return connections.containsKey(connection);
	}

	/**
	 * An iterator over all existing <code>Connection</code>s within this
	 * Session
	 * 
	 * @return all existing <code>Connections</code>
	 */
	public Iterator<Connection> getConnections() {
		return connections.keySet().iterator();
	}

	/**
	 * Returns a <code>Connection</code> with specified connectionID. If not
	 * found, returns null.
	 * 
	 * @param connectionID
	 *            the Connection's ID
	 * @return an existing COnnection or null, if not found
	 */
	public Connection getConnection(short connectionID) {

		Iterator<Connection> testedCons = connections.keySet().iterator();
		while (testedCons.hasNext()) {
			Connection tested = testedCons.next();
			if (tested.getConnectionID() == connectionID) {
				return tested;
			}
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("No Connection found with follwing connection ID: "
					+ connectionID);
		}
		return null;
	}

	public final OperationalTextConfiguration getConfiguration() {
		return configuration;
	}
	
	public final SessionTaskRouter getTaskRouter(){
		return taskRouter;
	}

	/**
	 * Returns the Session's Initiators Session ID.
	 * 
	 * @return
	 */
	final ISID getInitiatorSessionID() {
		return initiatorSessionID;
	}

	/**
	 * Returns the Session's Initiator Name.
	 * 
	 * @return
	 */
	final String getInitiatorName() {
		return initiatorName;
	}

	/**
	 * Returns the Session's actual expected command sequence number.
	 * 
	 * @return SerialArithmeticNumber representing the session's expCmdSeqNum
	 */
	final SerialArithmeticNumber getExpectedCommandSequence() {
		return getExpectedCommandSequenceNumber(false);
	}

	/**
	 * Returns the Session's actual Expected Command Sequence Number and if
	 * true, increments the expCmdSeqNum before returning.
	 * 
	 * @param inkr
	 *            if true, increments before return, else only return
	 * @return SerialArithmeticNumber the Session's expCmdSeqNum
	 */
	private final SerialArithmeticNumber getExpectedCommandSequenceNumber(
			boolean incr) {
		synchronized (expectedCommandSequenceNumber) {
			if (incr == true) {
				expectedCommandSequenceNumber.increment();
			}
			return expectedCommandSequenceNumber;
		}
	}

	/**
	 * Returns the Session's actual Maximum Command Sequence Number.
	 * 
	 * @return SerialArithmeticNumber representing the session's maxCmdSeqNum
	 */
	final SerialArithmeticNumber getMaximumCommandSequence() {
		return incrMaximumCommandSequence(0);
	}

	/**
	 * Returns the number of received queued PDUs
	 * 
	 * @return
	 */
	final int getReceivingBufferSize() {
		return receivedPDUs.size();
	}

	public final SessionType getSessionType() {
		return sessionType;
	}

	/**
	 * Returns the Session's Target Identifying Handle.
	 * 
	 * @return
	 */
	final short getTargetSessionIdentifyingHandleD() {
		return targetSessionIdentifyingHandle;
	}

	/**
	 * Returns true if the Session already assigned an ExpectedCommandSequence
	 * number at Startup.
	 * 
	 * @return true if ExpStatSN is set, false else
	 */
	final boolean hasExpCmdSN() {
		if (expectedCommandSequenceNumber == null) {
			return false;
		}
		return true;
	}

	/**
	 * Increments the Session's ExpCmdSN if necessary because of the callingPDU.
	 * 
	 * @param callingPDU
	 *            increment is based on type and state of the callingPDU
	 */
	final void incrExpectedCommandSequenceNumber(ProtocolDataUnit callingPDU) {
		InitiatorMessageParser parser = (InitiatorMessageParser) callingPDU
				.getBasicHeaderSegment().getParser();
		int receivedCommandSequenceNumber = parser.getCommandSequenceNumber();
		if (getExpectedCommandSequence().compareTo(
				receivedCommandSequenceNumber) == 0) {
			// callingPDU is expected PDU
			if (!(callingPDU.getBasicHeaderSegment().isImmediateFlag())
					&& !(parser instanceof SNACKRequestParser)
					&& !(parser instanceof LoginRequestParser)) {
				// ExpCmdSN increment is necessary for callingPDU
				getExpectedCommandSequenceNumber(true);
			}
		}
	}

	/**
	 * Returns the Session's actual Maximum Command Sequence Number and if sumX
	 * greater 0, increments the maxCmdSeqNum x times before returning
	 * 
	 * @param sumX
	 *            increment maxCmdSeqNum x times before returning
	 * @return SerialArithmeticNumber representing the session's maxCmdSeqNum
	 */
	final SerialArithmeticNumber incrMaximumCommandSequence(int incrXTimes) {
		if (incrXTimes < 0) {
			incrXTimes = 0;
		}
		synchronized (maximumCommandSequenceNumber) {
			for (int i = 0; i < incrXTimes; i++) {
				maximumCommandSequenceNumber.increment();
			}
			return maximumCommandSequenceNumber;
		}

	}

	/**
	 * Retrieve and removes the next Received Protocol Data Unit. Method waits
	 * until a PDU was received.
	 * 
	 * @return the next received PDU
	 */
	public final ProtocolDataUnit pollReceivedPDU() {
		return peekOrPollReceivingBuffer("poll", 0);
	}

	/**
	 * Retrieve and removes the next Received Protocol Data Unit. Method waits
	 * <code>nanoSecs</code> for a received PDU.
	 * 
	 * @param nanoSecs
	 * @return the next received PDU or null if waiting time exceeded
	 */
	 public final ProtocolDataUnit pollReceivedPDU(long nanoSecs) {
		return peekOrPollReceivingBuffer("poll", nanoSecs);
	}

	/**
	 * Retrieve but do not remove the next Received Protocol Data Unit. Method
	 * waits until a PDU was received.
	 * 
	 * @return the next received PDU
	 */
	public final ProtocolDataUnit peekReceivedPDU() {
		return peekOrPollReceivingBuffer("peek", 0);
	}

	/**
	 * Retrieve but do not remove the next received Protocol Data Unit. Method
	 * waits <code>nanoSecs</code> for a received PDU.
	 * 
	 * @param nanoSecs
	 * @return the next received PDU or null if waiting time exceeded
	 */
	public final ProtocolDataUnit peekReceivedPDU(long nanoSecs) {
		return peekOrPollReceivingBuffer("peek", nanoSecs);
	}

	/**
	 * Peeks or polls the receiving PDU queue. In case nanoSecs > 0,
	 * 
	 * @param peekOrPoll
	 *            "peek" or "poll"
	 * @param nanoSecs
	 *            the nanoseconds this method waits for an incoming PDU (0 =
	 *            infinity)
	 * @return the next received PDU or null if waiting time exceeded
	 */
	private final ProtocolDataUnit peekOrPollReceivingBuffer(String peekOrPoll,
			long nanoSecs) {
		ProtocolDataUnit result = null;
		// lock this block for other threads
		LOCK.lock();
		try {
			// wait for an incoming PDU
			if (nanoSecs <= 0) {
				while (receivedPDUs.size() == 0)
					somethingReceived.await();
			} else {
				while (receivedPDUs.size() == 0)
					somethingReceived.awaitNanos(nanoSecs);
			}
		} catch (InterruptedException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER
						.debug("Synchronization error while awaiting an incoming PDU!");
			}
		}

		// peek or poll and in case await exceeded time limit -> return null
		if (peekOrPoll.equals("peek") && (getReceivingBufferSize() != 0)) {
			result = receivedPDUs.peek();
		}
		if (peekOrPoll.equals("poll") && (getReceivingBufferSize() != 0)) {
			result = receivedPDUs.poll();
		}
		// unlock this block
		LOCK.unlock();
		return result;
	}

	private final void setExpectedCommandSequenceNumber(
			ProtocolDataUnit firstPDU) {
		if (!hasExpCmdSN()) {
			if (firstPDU.getBasicHeaderSegment().getParser() instanceof InitiatorMessageParser) {
				InitiatorMessageParser parser = (InitiatorMessageParser) firstPDU
						.getBasicHeaderSegment().getParser();
				expectedCommandSequenceNumber = new SerialArithmeticNumber(
						parser.getCommandSequenceNumber());
			}
		}
	}

	/**
	 * Set this Session's Initiator Session ID, if not already set.
	 * 
	 * @param isid
	 *            InitiatorSessionID
	 * @return false if already set, true else.
	 */
	final boolean setInitiatorSessionID(ISID isid) {
		if (initiatorSessionID != null) {
			initiatorSessionID = isid;
			return true;
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Tried to set a session's ISID twice: old ISID is "
					+ getInitiatorSessionID() + ", new ISID would be " + isid);
		}
		return false;
	}

	final boolean setInitiatorName(String name) {
		if (initiatorName != null) {
			initiatorName = name;
			return true;
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER
					.debug("Tried to set a session's initiator name twice: old name is "
							+ getInitiatorName()
							+ ", new name would be "
							+ name);
		}
		return false;
	}

	/**
	 * 
	 * @param type
	 */
	public final void setSessionType(SessionType type) {
		if (sessionType.equals(SessionType.Unknown)) {
			sessionType = type;

		}
	}

	/*
	 * final void signalReceivedPDU(Connection connection) { // if the received
	 * PDU's CmdSN is equal ExpCmdSN, add to received // PDUs ProtocolDataUnit
	 * pdu = null; InitiatorMessageParser parser = null; SerialArithmeticNumber
	 * receivedCommandSequenceNumber = null; synchronized (connections) { // if
	 * the received PDU's CmdSN is equal ExpCmdSN, add to received // PDUs if
	 * (connection.getReceivingQueue().size() > 0) { pdu =
	 * connections.get(connection).peek();
	 * 
	 * if (receivedCommandSequenceNumber .equals(expectedCommandSequenceNumber)) {
	 * receivedPDUs.add(connections.get(connection).poll()); // only increment
	 * ExpCmdSN if non-immediate and no SNACK // Request if
	 * (!pdu.getBasicHeaderSegment().isImmediateFlag() && !(parser instanceof
	 * SNACKRequestParser)) { expectedCommandSequenceNumber.increment(); } //
	 * signal TaskRouter } else { }
	 * 
	 * if (receivedCommandSequenceNumber .equals(expectedCommandSequenceNumber)) {
	 * receivedPDUs.add(connections.get(connection).poll());
	 * expectedCommandSequenceNumber.increment(); // new maximum command
	 * sequence number // signal TaskRouter } else { } // search for the next
	 * buffered PDU in every connection for (Queue<ProtocolDataUnit> pdus :
	 * connections.values()) { if (pdus.size() <= 0) { continue; } // could be
	 * more than one following PDU per connection boolean testing = true; while
	 * (testing) { pdu = pdus.peek(); parser = (InitiatorMessageParser) pdu
	 * .getBasicHeaderSegment().getParser(); receivedCommandSequenceNumber = new
	 * SerialArithmeticNumber( parser.getCommandSequenceNumber()); if
	 * (receivedCommandSequenceNumber .equals(expectedCommandSequenceNumber)) {
	 * receivedPDUs .add(connections.get(connection).poll());
	 * expectedCommandSequenceNumber.increment(); // new maximum command
	 * sequence number // signal TaskRouter } else { // head of connection's
	 * queue isn't the next // received // PDU testing = false; } } } } } }
	 */

	/**
	 * Signaling a sequencing Gap within a connection.
	 * 
	 * @param connection
	 */
	final void signalCommandSequenceGap(Connection connection) {
		// a Connection is signaling a command sequencing gap,
		// which means the connection is receiving more and more PDUs,
		// but the Session will not process them, because ExpCmdSN doesn't
		// reaches the necessary value.
		// The Session can decide whether to clear the connection's receiving
		// PDU buffer and send a NOP-Response, signaling the initiator which
		// CmdSN is expected.
		// (clearing the buffer will only force initiator to send PDUs again, no
		// lost if initiator behaves correct),
		// or to completely drop the connection.
	}

	/**
	 * A connection is signaling a received PDU.
	 * 
	 * @param CmdSN
	 *            the received PDU's CommandSequenceNumber
	 * @param connection
	 *            the connection the PDU was received
	 */
	final void signalReceivedPDU(Integer CmdSN, Connection connection) {
		signalledPDUs.put(CmdSN, connection);
		ProtocolDataUnit receivedPDU = null;
		// the leading PDU will call on the Expected Command Sequence Number
		if (!hasExpCmdSN()) {
			setExpectedCommandSequenceNumber(connection.peekReceivedPDU());
		}
		// checks if Expected Command Sequence Number arrived
		while (getExpectedCommandSequence().compareTo(signalledPDUs.firstKey()) == 0) {
			synchronized (receivedPDUs) {
				// load expected PDU from any connection into the session
				receivedPDU = signalledPDUs.get(getExpectedCommandSequence())
						.pollReceivedPDU();
				receivedPDUs.add(receivedPDU);
				// remove from signaled PDUs
				signalledPDUs.remove(getExpectedCommandSequence());
				// increment ExpCmdSN, if necessary
				incrExpectedCommandSequenceNumber(receivedPDU);
				// MaxCmdSN Window
				// ///////////////////////////////
				// signal possible waiting Threads, e.g. TaskRouter
				somethingReceived.signal();
			}
			// signal TaskRouter
		}

	}

	/**
	 * Updating Session wide, PDU relevant parameters before sending PDU over
	 * specified Connection
	 * 
	 * @param connection
	 *            the sending Connection
	 * @param pdu
	 *            the sending PDU
	 */
	final void sendPDU(Connection connection, ProtocolDataUnit pdu) {
		synchronized (connections) {
			// update PDU parameter, ExpCmdSN + MaxCmdSN
			TargetMessageParser parser = (TargetMessageParser) pdu
					.getBasicHeaderSegment().getParser();
			parser
					.setExpectedCommandSequenceNumber(getExpectedCommandSequence()
							.getValue());
			parser.setMaximumCommandSequenceNumber(getMaximumCommandSequence()
					.getValue());
			// send PDU
			connection.sendPDU(this, pdu);
		}

	}
	
	public String getIdentifyingString(){
		StringBuffer result = new StringBuffer();
		result.append("InitiatorName = ");
		result.append(getInitiatorName());
		result.append("; ISID = ");
		result.append(getInitiatorSessionID());
		result.append("; TSIH = ");
		result.append(getTargetSessionIdentifyingHandleD());
		result.append(";");
		return result.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((initiatorName == null) ? 0 : initiatorName.hashCode());
		result = prime
				* result
				+ ((initiatorSessionID == null) ? 0 : initiatorSessionID
						.hashCode());
		result = prime * result + targetSessionIdentifyingHandle;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Session other = (Session) obj;
		if (initiatorName == null) {
			if (other.initiatorName != null)
				return false;
		} else if (!initiatorName.equals(other.initiatorName))
			return false;
		if (initiatorSessionID == null) {
			if (other.initiatorSessionID != null)
				return false;
		} else if (!initiatorSessionID.equals(other.initiatorSessionID))
			return false;
		if (targetSessionIdentifyingHandle != other.targetSessionIdentifyingHandle)
			return false;
		return true;
	}

	/**
	 * Logs a trace Message specific to this Session, if trace log is enabled
	 * within the logging environment.
	 * 
	 * @param logMessage
	 */
	private void logTrace(String logMessage) {
		if (LOGGER.isTraceEnabled()) {

			LOGGER.trace("InitiatorName = " + getInitiatorName() + "ISID = "
					+ getInitiatorName() + "TSIH ="
					+ getTargetSessionIdentifyingHandleD() + " LogMessage: "
					+ logMessage);

		}
	}

	/**
	 * Logs a debug Message specific to this Session, if debug log is enabled
	 * within the logging environment.
	 * 
	 * @param logMessage
	 */
	private void logDebug(String logMessage) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("InitiatorName = " + getInitiatorName() + "ISID = "
					+ getInitiatorName() + "TSIH ="
					+ getTargetSessionIdentifyingHandleD() + " LogMessage: "
					+ logMessage);

		}
	}

}

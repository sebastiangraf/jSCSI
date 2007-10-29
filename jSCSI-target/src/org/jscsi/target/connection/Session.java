package org.jscsi.target.connection;

import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.conf.OperationalTextConfiguration;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.parameter.connection.Phase;
import org.jscsi.target.parameter.connection.SessionType;
import org.jscsi.connection.SerialArithmeticNumber;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.parser.login.ISID;

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

	/** Session's Phase */
	private short SessionPhase;

	/** The Target Session Identifying Handle. */
	private short targetSessionIdentifyingHandle;

	/** The Initiator Session ID */
	private ISID initiatorSessionID;

	private String initiatorName;

	/** the session's phase */
	private Phase phase;

	/** the session's type */
	private SessionType sessionType;

	/** The Command Sequence Number of this session. */
	private SerialArithmeticNumber expectedCommandSequenceNumber;

	/** The Maximum Command Sequence Number of this session. */
	private SerialArithmeticNumber maximumCommandSequenceNumber;

	/** connections are mapped to their receiving Queues */
	private final Map<Connection, Queue<ProtocolDataUnit>> connections;

	private final Queue<ProtocolDataUnit> receivedPDUs;

	public Session(Connection connection, short tsih) {
		configuration = OperationalTextConfiguration.create(this);
		targetSessionIdentifyingHandle = tsih;
		connections = new ConcurrentHashMap<Connection, Queue<ProtocolDataUnit>>();
		receivedPDUs = new ConcurrentLinkedQueue<ProtocolDataUnit>();
		addConnection(connection);
		// start TaskManger
	}

	/**
	 * Adds a Connection to this Session.
	 * 
	 * @param connection
	 */
	public void addConnection(Connection connection) {
		if (connection.setReferencedSession(this)) {
			connections.put(connection, connection.getReceivingQueue());
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
		return connections.keySet().contains(connection);
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
		return incrExpectedCommandSequence(false);
	}

	/**
	 * Returns the Session's actual Maximum Command Sequence Number.
	 * 
	 * @return SerialArithmeticNumber representing the session's maxCmdSeqNum
	 */
	final SerialArithmeticNumber getMaximumCommandSequence() {
		return incrMaximumCommandSequence(0);
	}

	public final Phase getSessionPhase() {
		return phase;
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
	 * Returns the Session's actual Expected Command Sequence Number and if
	 * true, increments the expCmdSeqNum before returning.
	 * 
	 * @param inkr
	 *            if true, increments before return, else only return
	 * @return SerialArithmeticNumber the Session's expCmdSeqNum
	 */
	final SerialArithmeticNumber incrExpectedCommandSequence(boolean incr) {
		// because of write and/or read request
		synchronized (expectedCommandSequenceNumber) {
			if (incr == true) {
				expectedCommandSequenceNumber.increment();
			}
			return expectedCommandSequenceNumber;
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

	final ProtocolDataUnit receivePDU(Connection connection) {
		return connections.get(connection).poll();
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
					.debug("Tried to set a session's initiatorn name twice: old name is "
							+ getInitiatorName()
							+ ", new name would be "
							+ name);
		}
		return false;
	}

	public final void setSessionPhase(Phase phase) {
		phase = phase;
	}

	public final void setSessionType(SessionType type) {
		sessionType = type;
	}

	final void signalReceivedPDU(Connection connection) {
		synchronized (connections) {
			// if the received PDU's CmdSN is equal ExpCmdSN, add to received
			// PDUs
			InitiatorMessageParser parser = (InitiatorMessageParser) connections
					.get(connection).peek().getBasicHeaderSegment().getParser();
			SerialArithmeticNumber receivedCommandSequenceNumber = new SerialArithmeticNumber(
					parser.getCommandSequenceNumber());
			if (receivedCommandSequenceNumber
					.equals(expectedCommandSequenceNumber)) {
				receivedPDUs.add(connections.get(connection).poll());
				expectedCommandSequenceNumber.increment();
				// signal TaskRouter
			} else {
			}
			// search for the next buffered PDU in every connection
			for (Queue<ProtocolDataUnit> pdus : connections.values()) {
				if (pdus.size() <= 0) {
					continue;
				}
				// could be more than one following PDU per connection
				boolean testing = true;
				while (testing) {
					parser = (InitiatorMessageParser) pdus.peek()
							.getBasicHeaderSegment().getParser();
					receivedCommandSequenceNumber = new SerialArithmeticNumber(
							parser.getCommandSequenceNumber());
					if (receivedCommandSequenceNumber
							.equals(expectedCommandSequenceNumber)) {
						receivedPDUs.add(connections.get(connection).poll());
						expectedCommandSequenceNumber.increment();
						// signal TaskRouter
					} else {
						// head of connection's queue isn't the next received
						// PDU
						testing = false;
					}
				}
			}
		}
	}

	final void sendPDU(Connection connection, ProtocolDataUnit pdu) {

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

}

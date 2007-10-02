package connection;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.connection.SerialArithmeticNumber;
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

	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/** The <code>Configuration</code> instance for this session. */
	// private final Configuration configuration;
	/** The session is in this phase. */
	// private IPhase phase;
	/** The Target Session Identifying Handle. */
	private short targetSessionIdentifyingHandle;

	/** The Initiator Session ID */
	private ISID initiatorSessionID;

	private String initiatorName;

	/** The Command Sequence Number of this session. */
	private final SerialArithmeticNumber expectedCommandSequenceNumber;

	/** The Maximum Command Sequence Number of this session. */
	private final SerialArithmeticNumber maximumCommandSequenceNumber;

	/** A List object with all open connections and there connectionIDs. */
	private final Map<Short, Connection> connections;

	public Session(Connection connection, short tsih) {
		expectedCommandSequenceNumber = new SerialArithmeticNumber(1);
		maximumCommandSequenceNumber = new SerialArithmeticNumber(2);
		targetSessionIdentifyingHandle = tsih;
		connections = new ConcurrentHashMap<Short, Connection>();
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
			connections.put(connection.getConnectionID(), connection);
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER
						.debug("Tried to add a Connection which has already a referenced Session!");
			}
		}

	}
	
	/**
	 * An iterator over all existing <code>Connection</code>s within
	 * this Session
	 * @return all existing <code>Connections</code>
	 */
	public Iterator<Connection> getConnections(){
		return connections.values().iterator();
	}
	
	/**
	 * Returns a <code>Connection</code> with specified
	 * connectionID. If not found, returns null.
	 * @param connectionID the Connection's  ID
	 * @return an existing COnnection or null, if not found
	 */
	public Connection getConnection(short connectionID){
		
		if(connections.keySet().contains(connectionID)){
			return connections.get(connectionID);
		} else {
			if(LOGGER.isDebugEnabled()){
				LOGGER.debug("No Connection found with follwing connection ID: " + connectionID);
			}
			return null;
		}
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

	/**
	 * Returns the Session's Initiator Name.
	 * 
	 * @return
	 */
	final String getInitiatorName() {
		return initiatorName;
	}
	
	final boolean setInitiatorName(String name) {
		if (initiatorName != null) {
			initiatorName = name;
			return true;
		}
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Tried to set a session's initiatorName twice: old name is "
					+ getInitiatorName() + ", new name would be " + name);
		}
		return false;
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
	 * Returns the Session's actual expected command sequence number.
	 * 
	 * @return SerialArithmeticNumber representing the session's expCmdSeqNum
	 */
	final SerialArithmeticNumber getExpectedCommandSequence() {
		return incrExpectedCommandSequence(false);
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
	 * Returns the Session's actual Maximum Command Sequence Number.
	 * 
	 * @return SerialArithmeticNumber representing the session's maxCmdSeqNum
	 */
	final SerialArithmeticNumber getMaximumCommandSequence() {
		return incrMaximumCommandSequence(0);
	}

}

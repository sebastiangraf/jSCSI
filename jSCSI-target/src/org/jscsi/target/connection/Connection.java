package org.jscsi.target.connection;

import java.nio.channels.SocketChannel;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.connection.SerialArithmeticNumber;
import org.jscsi.parser.InitiatorMessageParser;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.TargetMessageParser;
import org.jscsi.target.conf.operationalText.OperationalTextConfiguration;
import org.jscsi.target.conf.operationalText.OperationalTextException;
import org.jscsi.target.parameter.connection.Phase;
import org.jscsi.target.task.ConnectionTaskRouter;

/**
 * <h1>Connection</h1>
 * <p/> This class represents a connection, which is used in the iSCSI Standard
 * (RFC3720). Such a connection is directed from the initiator to the targetTest.
 * One or more Connections between an initiator and a targetTest represent a
 * session.
 * 
 * @author Marcus Specht
 * 
 */
public class Connection {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(Connection.class);

	public static final String CONNECTION_ID = "ConnectionID";

	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * The <code>Session</code> instance, which contains this
	 * <code>Connection</code> instance.
	 */
	private Session referenceSession;

	private final OperationalTextConfiguration configuration;

	/** the connection's ConnectionPhase */
	private Phase ConnectionPhase;

	/**
	 * The ID of this connection. This must be unique within a
	 * <code>Session</code>.
	 */
	private short connectionID;

	/** if the Connection already got a connectionID */
	boolean hasConnectionID;

	/**
	 * The Status Sequence Number, which is used from the initiator to control
	 * status sequencing and other.
	 */
	private SerialArithmeticNumber statusSequenceNumber;

	/**
	 * The sending queue filled with <code>ProtocolDataUnit</code>s and their
	 * corresponding StatusSeguenceNumbers.
	 */
	private final SortedMap<Integer, ProtocolDataUnit> sendingBuffer;

	/**
	 * The receiving Buffer filled with <code>ProtocolDataUnit</code>s and
	 * their corresponding Command Sequence Numbers
	 */
	private final SortedMap<Integer, ProtocolDataUnit> receivingBuffer;

	/**
	 * the ReceivingLock is used to synchronize every request for receiving PDUs
	 */
	private final Lock ReceivingLock = new ReentrantLock();

	/**
	 * the SendingLock is used to synchronize every request for sending PDUs
	 */
	private final Lock SendingLock = new ReentrantLock();

	/**
	 * the Condition is used to signal waiting Threads for reveived PDUs
	 */
	private final Condition somethingReceived = ReceivingLock.newCondition();
	
	private final ConnectionTaskRouter taskRouter;

	/**
	 * this Connection's NetWorker
	 */
	private final NetWorker netWorker;

	public Connection(SocketChannel sChannel) throws OperationalTextException {
		//create a new Configuration for this Connection 
		configuration = OperationalTextConfiguration.create(this);
		//a new TaskRouter holding every active Task Object restricted to this Connection
		taskRouter = new ConnectionTaskRouter(this);
		// when java 1.6 available for mac, change TreeMap to
		// ConcurrentSkipListMap, should be done in the Session and NetWorker too
		sendingBuffer = new TreeMap<Integer, ProtocolDataUnit>();
		receivingBuffer = new TreeMap<Integer, ProtocolDataUnit>();
		//Let's say we start Status sequencing with 1
		statusSequenceNumber = new SerialArithmeticNumber(1);
		//following variables are not yet available, but must be set later
		referenceSession = null;
		ConnectionPhase = Phase.Unknown;
		connectionID = -1;
		hasConnectionID = false;
		netWorker = new NetWorker(sChannel, this);
		netWorker.startListening();

	}

	/**
	 * Return the Connection's CID.
	 * 
	 * @return ConnectionID
	 */
	final short getConnectionID() {
		return connectionID;
	}

	/**
	 * Returns the Connections Configuration
	 * 
	 * @return
	 */
	public final OperationalTextConfiguration getConfiguration() {
		return configuration;
	}

	public final String getIdentifyingString(boolean recursiv){
		StringBuffer result = new StringBuffer();
		result.append("ConnectionID = ");
		result.append(getConnectionID());
		result.append(";");
		if(recursiv == true){
			result.append(" Referenced Session: ");
			result.append(getReferencedSession().getIdentifyingString());
		}
		return result.toString();
	}
	
	/**
	 * Returns the lowest CmdSN the receiving Buffer contains
	 * 
	 * @return
	 */
	final Integer getNextReceivedCommandSequenceNumber() {
		return receivingBuffer.firstKey();
	}

	/**
	 * Get the Connection's referenced Session.
	 * 
	 * @return referenced Session
	 */
	public final Session getReferencedSession() {
		return referenceSession;
	}

	/**
	 * Returns the Connection's StatusSequenceNumber.
	 * 
	 * @return StatSN
	 */
	final SerialArithmeticNumber getStatusSequenceNumber() {
		return getStatusSequenceNumber(false);
	}

	/**
	 * Get the Connection's actual Status Sequence Number
	 * 
	 * @param inkr
	 *            if true, increments StatusSequenceNumber before returning
	 * @return
	 */
	private final SerialArithmeticNumber getStatusSequenceNumber(boolean inkr) {
		synchronized (statusSequenceNumber) {
			if (inkr) {
				statusSequenceNumber.increment();
				return statusSequenceNumber;
			} else {
				return statusSequenceNumber;
			}
		}
	}
	
	public final ConnectionTaskRouter getTaskRouter(){
		return taskRouter;
	}

	/**
	 * Returns theConnection's current ConnectionPhase.
	 * 
	 * @return Connetion's ConnectionPhase
	 */
	public final Phase getPhase() {
		return ConnectionPhase;
	}

	/**
	 * Returns the Connection's sending queue.
	 * 
	 * @return
	 */
	final Map<Integer, ProtocolDataUnit> getSendingBuffer() {
		return sendingBuffer;
	}

	/**
	 * Returns the Connections receiving Queue
	 * 
	 * @return
	 */
	final SortedMap<Integer, ProtocolDataUnit> getReceivingBuffer() {
		return receivingBuffer;
	}

	/**
	 * Set the Connetion'S CID if not already set.
	 * 
	 * @param cid
	 *            ConnectionID
	 * @return false means CID already set, true else.
	 */
	final boolean setConnectionID(short cid) {
		if (hasConnectionID == false) {
			connectionID = cid;
			hasConnectionID = true;
			logTrace("Started new Connection");
			return true;
		} else {
			logDebug("Tried to reset Connection ID: old CID is "
					+ this.connectionID + " new CID should be " + cid);
			return false;
		}
	}

	public final void setPhase(Phase phase) {
		logTrace("Switched Phase from " + this.ConnectionPhase.getValue() + "to "
				+ phase.getValue());
		this.ConnectionPhase = phase;
	}

	/**
	 * Set the Connection's referenced Session if not already set
	 * 
	 * @param ion
	 * @return false means Session already set, true else.
	 */
	final boolean setReferencedSession(Session session) {
		if (referenceSession == null) {
			referenceSession = session;
			logTrace("Refernced Session: TSIH="
					+ referenceSession.getTargetSessionIdentifyingHandleD());
			return true;
		} else
			logDebug("Tried to reset referenced Session");
		return false;
	}
	
	/**
	 * Signals the Connection a received PDU. The Connection
	 * is updating parameters because of the receivedPDU's parameter.
	 * The Session will be signaled.  
	 * @param receivedPDU
	 */
	final void signalReceivedPDU(ProtocolDataUnit receivedPDU) {
		InitiatorMessageParser parser = (InitiatorMessageParser) receivedPDU
				.getBasicHeaderSegment().getParser();
		// add received PDU and it's CmdSN to Buffer
		receivingBuffer.put(parser.getCommandSequenceNumber(), receivedPDU);
		// clean the sending buffer from successfully sended PDU signaled
		// through
		// the initiator's ExpStatSN
		updateAndCleanSendedBuffer(parser.getExpectedStatusSequenceNumber());
		// signal Session
		getReferencedSession().signalReceivedPDU(
				parser.getCommandSequenceNumber(), this);
	}

	/**
	 * Sends the PDU, if the calling entity is the referencedSession. If not
	 * the PDU will be forwarded to the Session, so the Session takes control.
	 * 
	 * @param caller
	 *            if not referenced Session, will be forwarded to Session
	 * @param pdu
	 *            sending PDU
	 */
	final void sendPDU(Object caller, ProtocolDataUnit pdu) {
		if (caller instanceof Session) {
			// is the Session the Connection's referenced Session
			if (((Session) caller).equals(referenceSession)) {
				TargetMessageParser parser = (TargetMessageParser) pdu
						.getBasicHeaderSegment().getParser();
				Integer newStatSN;
				// if status sequence numbering isn't done for every T-to-I PDU,
				// make exception here
				SendingLock.lock();
				newStatSN = getStatusSequenceNumber(true).getValue();
				parser.setStatusSequenceNumber(newStatSN);
				sendingBuffer.put(newStatSN, pdu);
				netWorker.signalSendingPDU(newStatSN);
				SendingLock.unlock();
				return;
			}
		}
		getReferencedSession().sendPDU(this, pdu);
	}

	// /**
	// * Sends the Protocol Data Unit.
	// *
	// * @param pdu
	// */
	// final void enqueueSendingQueue(ProtocolDataUnit pdu) {
	// sendingQueue.add(pdu);
	// // inform the NetWorker he has work to do
	// netWorker.somethingToSend();
	// }

	// /**
	// * Sends all ProtocolDataUnits.
	// *
	// * @param pdus
	// */
	// final void enqueueSendingQueue(Collection<? extends ProtocolDataUnit>
	// pdus) {
	// sendingQueue.addAll(pdus);
	// // inform the NetWorker he has work to do
	// netWorker.somethingToSend();
	// }

	/**
	 * Returns the number of received queued PDUs
	 * 
	 * @return
	 */
	final int getReceivingBufferSize() {
		return receivingBuffer.size();
	}

	/**
	 * Retrieve and removes the next Received Protocol Data Unit. Method waits
	 * until a PDU was received.
	 * 
	 * @return the next received PDU
	 */
	final ProtocolDataUnit pollReceivedPDU() {
		return peekOrPollReceivingBuffer("poll", 0);
	}

	/**
	 * Retrieve and removes the next Received Protocol Data Unit. Method waits
	 * <code>nanoSecs</code> for a received PDU.
	 * 
	 * @param nanoSecs
	 * @return the next received PDU or null if waiting time exceeded
	 */
	final ProtocolDataUnit pollReceivedPDU(long nanoSecs) {
		return peekOrPollReceivingBuffer("poll", nanoSecs);
	}

	/**
	 * Retrieve but do not remove the next Received Protocol Data Unit. Method
	 * waits until a PDU was received.
	 * 
	 * @return the next received PDU
	 */
	final ProtocolDataUnit peekReceivedPDU() {
		return peekOrPollReceivingBuffer("peek", 0);
	}

	/**
	 * Retrieve but do not remove the next received Protocol Data Unit. Method
	 * waits <code>nanoSecs</code> for a received PDU.
	 * 
	 * @param nanoSecs
	 * @return the next received PDU or null if waiting time exceeded
	 */
	final ProtocolDataUnit peekReceivedPDU(long nanoSecs) {
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
		ReceivingLock.lock();
		try {
			// wait for an incoming PDU
			if (nanoSecs <= 0) {
				while (getReceivingBufferSize() == 0)
					somethingReceived.await();
			} else {
				while (getReceivingBufferSize() == 0)
					somethingReceived.awaitNanos(nanoSecs);
			}
		} catch (InterruptedException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER
						.debug("Synchronization error while awaiting an incoming PDU!");
			}
		}

		// peek or poll and in case await exceeded time limit -> return null
		if (peekOrPoll.equals("peek") && (receivingBuffer.size() != 0)) {
			result = receivingBuffer.get(receivingBuffer.firstKey());
		}
		if (peekOrPoll.equals("poll") && (receivingBuffer.size() != 0)) {
			result = receivingBuffer.get(receivingBuffer.firstKey());
			receivingBuffer.remove(receivingBuffer.firstKey());
		}
		// unlock this block
		ReceivingLock.unlock();
		return result;
	}

	final void removeFirstReceivedPDUfromBuffer() {
		removeReceivedPDUfromBuffer(receivingBuffer.firstKey());
	}

	final void removeReceivedPDUfromBuffer(int cmdSN) {
		ReceivingLock.lock();
		receivingBuffer.remove(cmdSN);
		ReceivingLock.unlock();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + connectionID;
		result = prime
				* result

				+ ((getReferencedSession().getInitiatorName() == null) ? 0
						: getReferencedSession().getInitiatorName().hashCode());

		result = prime * result
				+ getReferencedSession().getTargetSessionIdentifyingHandleD();
		return result;
	}

	/**
	 * Cleaning the Connection's sended PDU buffer. A targetTest buffers any sending
	 * PDU until a received ExpStatSN is signaling the successful delivery.
	 * 
	 * @param expectedStatusSequenceNumber
	 *            the initiator's ExpStatSN
	 */
	final void updateAndCleanSendedBuffer(int expectedStatusSequenceNumber) {
		int firstStatSN = sendingBuffer.firstKey();
		while (firstStatSN < expectedStatusSequenceNumber) {
			sendingBuffer.remove(firstStatSN);
			firstStatSN = sendingBuffer.firstKey();
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Connection other = (Connection) obj;
		if (connectionID != other.connectionID)
			return false;
		if (getReferencedSession().getInitiatorName() == null) {
			if (other.getReferencedSession().getInitiatorName() != null)
				return false;
		} else if (!getReferencedSession().getInitiatorName().equals(
				other.getReferencedSession().getInitiatorName()))
			return false;
		if (getReferencedSession().getTargetSessionIdentifyingHandleD() != other
				.getReferencedSession().getTargetSessionIdentifyingHandleD())
			return false;
		return true;
	}

	/**
	 * Logs a trace Message specific to this Connection, if trace log is enabled
	 * within the logging environment.
	 * 
	 * @param logMessage
	 */
	private void logTrace(String logMessage) {
		if (LOGGER.isTraceEnabled()) {
			if (hasConnectionID) {
				LOGGER.trace("CID=" + getConnectionID() + " Message: "
						+ logMessage);
			}
		}
	}

	/**
	 * Logs a debug Message specific to this Connection, if debug log is enabled
	 * within the logging environment.
	 * 
	 * @param logMessage
	 */
	private void logDebug(String logMessage) {
		if (LOGGER.isDebugEnabled()) {
			if (hasConnectionID) {
				LOGGER.trace("CID=" + getConnectionID() + " Message: "
						+ logMessage);
			}
		}
	}

}

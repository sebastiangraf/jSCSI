package org.jscsi.target.connection;

import java.nio.channels.SocketChannel;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.connection.Connection;
import org.jscsi.connection.SerialArithmeticNumber;
import org.jscsi.target.connection.Session;
import org.jscsi.parser.ProtocolDataUnit;

/**
 * <h1>Connection</h1>
 * <p/> This class represents a connection, which is used in the iSCSI Standard
 * (RFC3720). Such a connection is directed from the initiator to the target.
 * One or more Connections between an initiator and a target represent a
 * session.
 * 
 * @author Marcus Specht
 * 
 */
public class Connection {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(Connection.class);

	/** connectionIDs null value */
	private final short NO_ID = -1;
	// --------------------------------------------------------------------------
	// --------------------------------------------------------------------------

	/**
	 * The <code>Session</code> instance, which contains this
	 * <code>Connection</code> instance.
	 */
	private Session referenceSession;

	/**
	 * The ID of this connection. This must be unique within a
	 * <code>Session</code>.
	 */
	private short connectionID;

	/**
	 * The Status Sequence Number, which is used from the initiator to control
	 * status sequencing and other.
	 */
	private SerialArithmeticNumber statusSequenceNumber;

	/**
	 * The sending queue filled with <code>ProtocolDataUnit</code>s, which
	 * have to be sent.
	 */
	private final Queue<ProtocolDataUnit> sendingQueue;

	/**
	 * The receiving queue filled with <code>ProtocolDataUnit</code>s, which
	 * are received.
	 */
	private final Queue<ProtocolDataUnit> receivingQueue;

	/**
	 * the LOCK is used to synchronize every request for receiving PDUs
	 */
	private final Lock LOCK = new ReentrantLock();

	/**
	 * the Condition is used to signal waiting Threads for reveived PDUs
	 */
	private final Condition somethingReceived = LOCK.newCondition();

	/**
	 * this Connection's NetWorker
	 */
	private final NetWorker netWorker;

	public Connection(SocketChannel sChannel) {
		sendingQueue = new ConcurrentLinkedQueue<ProtocolDataUnit>();
		receivingQueue = new ConcurrentLinkedQueue<ProtocolDataUnit>();
		connectionID = NO_ID;
		statusSequenceNumber = new SerialArithmeticNumber(1);
		netWorker = new NetWorker(sChannel, this);
		netWorker.startListening();
	}

	/**
	 * Assign a Session to this Connection instance. This is allowed only once.
	 * 
	 * @param session
	 * @return false if this connection already has a Session referenced, true
	 *         else
	 */
	final boolean assignSession(Session session) {
		if (referenceSession == null) {
			referenceSession = session;
			return true;
		}
		return false;

	}

	/**
	 * Set the Connetion'S CID if not already set.
	 * 
	 * @param cid
	 *            ConnectionID
	 * @return false means CID already set, true else.
	 */
	final boolean setConnectionID(short cid) {
		// NO_ID means that the Connection has not yet a ConnectionID
		if (connectionID == NO_ID) {
			connectionID = cid;
			return true;
		}
		return false;
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
			return true;
		} else
			return false;
	}

	/**
	 * Returns the Connection's sending queue.
	 * 
	 * @return
	 */
	final Queue<ProtocolDataUnit> getSendingQueue() {
		return sendingQueue;
	}

	/**
	 * Returns the Connections receiving Queue
	 * 
	 * @return
	 */
	final Queue<ProtocolDataUnit> getReceivingQueue() {
		return receivingQueue;
	}

	/**
	 * Sends the Protocol Data Unit.
	 * 
	 * @param pdu
	 */
	final void enqueueSendingQueue(ProtocolDataUnit pdu) {
		sendingQueue.add(pdu);
		// inform the NetWorker he has work to do
		netWorker.somethingToSend();
	}

	/**
	 * Sends all ProtocolDataUnits.
	 * 
	 * @param pdus
	 */
	final void enqueueSendingQueue(Collection<? extends ProtocolDataUnit> pdus) {
		sendingQueue.addAll(pdus);
		// inform the NetWorker he has work to do
		netWorker.somethingToSend();
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
	 * Returns the number of received queued PDUs
	 * @return
	 */
	final int getReceivingQueueSize(){		
		return receivingQueue.size();
	}
	
	/**
	 * Retrieve and removes the next Received Protocol Data Unit. Method waits
	 * until a PDU was received.
	 * 
	 * @return the next received PDU
	 */
	final ProtocolDataUnit pollReceivedPDU() {
		return peekOrPollReceivingQueue("poll", 0);
	}

	/**
	 * Retrieve and removes the next Received Protocol Data Unit. Method waits
	 * <code>nanoSecs</code> for a received PDU.
	 * 
	 * @param nanoSecs
	 * @return the next received PDU or null if waiting time exceeded
	 */
	final ProtocolDataUnit pollReceivedPDU(long nanoSecs) {
		return peekOrPollReceivingQueue("poll", nanoSecs);
	}

	/**
	 * Retrieve but do not remove the next Received Protocol Data Unit. Method
	 * waits until a PDU was received.
	 * 
	 * @return the next received PDU
	 */
	final ProtocolDataUnit peekReceivedPDU() {
		return peekOrPollReceivingQueue("peek", 0);
	}

	/**
	 * Retrieve but do not remove the next received Protocol Data Unit. Method
	 * waits <code>nanoSecs</code> for a received PDU.
	 * 
	 * @param nanoSecs
	 * @return the next received PDU or null if waiting time exceeded
	 */
	final ProtocolDataUnit peekReceivedPDU(long nanoSecs) {
		return peekOrPollReceivingQueue("peek", nanoSecs);
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
	private final ProtocolDataUnit peekOrPollReceivingQueue(String peekOrPoll,
			long nanoSecs) {
		ProtocolDataUnit result = null;
		// lock this block for other threads
		LOCK.lock();
		try {
			// wait for an incoming PDU
			if (nanoSecs <= 0) {
				while (receivingQueue.size() == 0)
					somethingReceived.await();
			} else {
				while (receivingQueue.size() == 0)
					somethingReceived.awaitNanos(nanoSecs);
			}
		} catch (InterruptedException e) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER
						.debug("Synchronization error while awaiting an incoming PDU!");
			}
		}

		// peek or poll and in case await exceeded time limit -> return null
		if (peekOrPoll.equals("peek") && (receivingQueue.size() != 0)) {
			result = receivingQueue.peek();
		}
		if (peekOrPoll.equals("poll") && (receivingQueue.size() != 0)) {
			result = receivingQueue.poll();
		}
		// unlock this block
		LOCK.unlock();
		return result;
	}

	/**
	 * Get the Connection's actual Status Sequence Number
	 * 
	 * @param inkr
	 *            if true, increments StatusSequenceNumber before returning
	 * @return
	 */
	final SerialArithmeticNumber getStatusSequenceNumber(boolean inkr) {
		synchronized (statusSequenceNumber) {
			if (inkr) {
				statusSequenceNumber.increment();
				return statusSequenceNumber;
			} else {
				return statusSequenceNumber;
			}
		}
	}

	/**
	 * If Threads are waiting to get a received ProtocolDataUnit, this method is
	 * called to signal any received PDU.
	 */
	final void somethingReceived() {
		somethingReceived.signalAll();
	}


}

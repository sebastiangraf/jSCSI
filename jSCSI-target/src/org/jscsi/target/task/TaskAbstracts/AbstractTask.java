package org.jscsi.target.task.TaskAbstracts;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.connection.SerialArithmeticNumber;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;

/**
 * A Task represents the command processing entity started by an initial
 * request, i.e. an initial Protocol Data Unit.
 * 
 * @author Marcus Specht
 *
 */
public abstract class AbstractTask extends AbstractOperation implements MutableTask {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(AbstractTask.class);

	/** The Task's referenced Connection */
	private final Connection refConnection;

	/** The Task's initiator task tag */
	private SerialArithmeticNumber initiatorTaskTag;

	/** The Task's target transfer tag, if used */
	private SerialArithmeticNumber targetTransferTag;

	/** The Task's current processing State */
	private State currentState;

	/** The Task's PDU receiving Queue */
	private final Queue<ProtocolDataUnit> receivedPDUs;

	/** only purpose is to use conditions */
	private final Lock lock = new ReentrantLock();

	/** condition signals the arrival of new PDUs */
	private final Condition receivedPDU = lock.newCondition();

	/**
	 * Creating a new task within a target environment, the task needs to have
	 * a Connection allegiance and an initialPDU.
	 * @param refConnection
	 * @param initialPDU
	 */
	public AbstractTask(Connection refConnection, ProtocolDataUnit initialPDU) {
		receivedPDUs = new ConcurrentLinkedQueue<ProtocolDataUnit>();
		receivedPDUs.add(initialPDU);
		this.refConnection = refConnection;
		initiatorTaskTag = new SerialArithmeticNumber(initialPDU
				.getBasicHeaderSegment().getInitiatorTaskTag());
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("Started new Task: ITT = "
					+ initialPDU.getBasicHeaderSegment().getInitiatorTaskTag());
		}
	}
	
	/**
	 * Returns the referenced Connection.
	 * 
	 * @return referenced Connection
	 */
	public Connection getReferencedConnection() {
		return refConnection;
	}

	/**
	 * Waits specified nanoWaitingTime for a received ProtocolDataUnit.
	 * 
	 * @param nanoWaitingTime
	 *            waiting time in nanoseconds
	 * @return the received PDU or null, if no PDU was received
	 */
	public ProtocolDataUnit receivePDU(long nanoWaitingTime) {
		ProtocolDataUnit result = null;
		lock.lock();
		if (receivedPDUs.size() == 0) {
			try {
				if (nanoWaitingTime != 0) {
					receivedPDU.awaitNanos(nanoWaitingTime);
				} else {
					receivedPDU.await();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (receivedPDUs.size() != 0) {
			result = receivedPDUs.poll();
		}
		lock.unlock();
		return result;
	}

	/**
	 * Sets the Tasks ITT, is only allowed once because an ITT never changes
	 * while a Task is running.
	 * 
	 * @throws OperationException
	 */
	public void setITT(SerialArithmeticNumber itt) throws OperationException {
		if (initiatorTaskTag == null) {
			initiatorTaskTag = itt;
		} else {
			throw new OperationException(
					"A Task's initiator task tag can only be set once");
		}

	}

	/**
	 * Sets the current processing State
	 * 
	 * @param current
	 *            State
	 */
	public void setState(State state) {
		currentState = state;

	}

	/**
	 * If the Task and State processing needs a location to store an identifying
	 * TargetTransferTag, here it is. The TTT is not used identifying an
	 * appropriate Task for incoming PDUs.
	 * 
	 * @throws OperationException
	 */
	public void setTTT(SerialArithmeticNumber ttt){
		targetTransferTag = ttt;
	}

	/**
	 * Assigns an appropriate PDU to this Task.
	 */
	public void assignPDU(ProtocolDataUnit pdu) {
		receivedPDUs.add(pdu);
		// signals the receivePDU method that a PDU arrived.
		receivedPDU.signal();

	}

	/**
	 * Returns the Task's Initiator Transfer Tag. The Initiator Transfer Tag is
	 * unique within an I-T-Nexus and is used to identify appropriate existing
	 * Tasks.
	 * 
	 * @return the Task's ITT.
	 */
	public SerialArithmeticNumber getITT() {
		return initiatorTaskTag;
	}

	/**
	 * The TargetTransferTag can be used within a target environment to place
	 * information in a response PDU. The initiator guarantees to use the TTT
	 * for every following PDUs.
	 * 
	 * @return the task's TTT, if used.
	 */
	public SerialArithmeticNumber getTTT() {
		return targetTransferTag;
	}

	/**
	 * Returns the current processing State
	 * @return current state
	 */
	public State getState() {
		return currentState;
	}



	



}

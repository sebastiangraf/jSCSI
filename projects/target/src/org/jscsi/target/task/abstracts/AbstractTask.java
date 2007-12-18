package org.jscsi.target.task.abstracts;

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
import org.jscsi.target.task.TargetTaskLibrary;
import org.jscsi.target.util.Singleton;

import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

/**
 * A Task represents the command processing entity started by an initial
 * request, i.e. an initial Protocol Data Unit.
 * 
 * @author Marcus Specht
 * 
 */
public abstract class AbstractTask extends AbstractSuspendableOperation implements
		MutableTask {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(AbstractTask.class);

	/** The Task's referenced Connection */
	private Connection refConnection;

	/** The Task's initiator task tag */
	private SerialArithmeticNumber initiatorTaskTag;

	/** The Task's targetTest transfer tag, if used */
	private SerialArithmeticNumber targetTransferTag;

	/** The Task's current processing State */
	private State currentState;

	private Thread currentStateThread;

	private boolean defined;

	private final TargetTaskLibrary library;

	/** The Task's PDU receiving Queue */
	private Queue<ProtocolDataUnit> receivedPDUs;

	/** the number of signaled/unprocessed PDUs * */
	private int signaledPDUs;

	/** control received PDU enqueue * */
	private final Lock enqueueLock = new ReentrantLock();

	/** only purpose is to use conditions signaling a receivedPDU */
	private final Lock dequeLock = new ReentrantLock();

	/** condition signals the arrival of new PDUs */
	private final Condition receivedPDU = dequeLock.newCondition();

	public AbstractTask() throws OperationException {
		defined = false;
		signaledPDUs = 0;
		try {
			library = Singleton.getInstance(TargetTaskLibrary.class);
		} catch (ClassNotFoundException e) {
			throw new OperationException("Couldn't load TargetTaskLibrary!");
		}
	}

	/**
	 * Creating a new task within a targetTest environment, the task needs to
	 * have a Connection allegiance and an initialPDU.
	 * 
	 * @param refConnection
	 * @param initialPDU
	 */
	/*
	 * public AbstractTask(Connection refConnection, ProtocolDataUnit
	 * initialPDU) { receivedPDUs = new ConcurrentLinkedQueue<ProtocolDataUnit>();
	 * receivedPDUs.add(initialPDU); this.refConnection = refConnection;
	 * initiatorTaskTag = new SerialArithmeticNumber(initialPDU
	 * .getBasicHeaderSegment().getInitiatorTaskTag()); signaledPDUs = 0; if
	 * (LOGGER.isTraceEnabled()) { LOGGER.trace("Started new Task: ITT = " +
	 * initialPDU.getBasicHeaderSegment().getInitiatorTaskTag()); } }
	 */

	public final void define(String initialStateClassName) throws OperationException {
		if (!defined) {
			setState(initialStateClassName);
			defined = true;
		} else {
			throw new OperationException("Task is already defined!");

		}

	}


	public void execute() throws OperationException {
		if (defined && (refConnection != null) && (receivedPDUs != null)) {
			super.execute();
			currentStateThread.start();
		} else {
			throw new OperationException(
					"Cannot execute Task: not referenced to an existing Connection");
		}
	}

	public void execute(Connection refConnection, ProtocolDataUnit initialPDU)
			throws OperationException {
		if (defined) {
			receivedPDUs = new ConcurrentLinkedQueue<ProtocolDataUnit>();
			receivedPDUs.add(initialPDU);
			this.refConnection = refConnection;
			initiatorTaskTag = new SerialArithmeticNumber(initialPDU
					.getBasicHeaderSegment().getInitiatorTaskTag());
			// FIXME TargetTransferTag could be set for any use-case,
			// e.g as unique task identifier within the target
			signaledPDUs = 0;
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Started new Task: ITT = "
						+ initialPDU.getBasicHeaderSegment()
								.getInitiatorTaskTag());
			}

		} else {
			throw new OperationException(
					"Task is not defined, cannot be executed");
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
		dequeLock.lock();
		if (signaledPDUs <= 0) {
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
			signaledPDUs--;
		}
		dequeLock.unlock();
		return result;
	}
	
	/**
	 * Suspends the task, i.e. stops forwarding received PDUs
	 * until the Task  is restarted. 
	 */
	@Override
	public void suspend() throws OperationException {
		// TODO Auto-generated method stub
		super.suspend();
	}
	
	/**
	 * Aborts the Task, i.e. destroys the active State and
	 * finishes the Task. 
	 */
	@Override
	public void abort() throws OperationException {
		// TODO Auto-generated method stub
		super.abort();
	}
	
	/**
	 * Finishes the Task.
	 */
	@Override
	protected void finish() throws OperationException {
		// TODO Auto-generated method stub
		super.finish();
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
	 * Sets the current processing State. Creates Thread and starts.
	 * 
	 * @param current
	 *            State
	 * @throws OperationException
	 * @throws OperationException
	 */
	public void setState(State state) throws OperationException {
		synchronized (currentState) {
			if (currentState != null) {
				currentState.abort();
			}
			try {
				state.define(this);
			} catch (OperationException e) {
				if (!state.getReferencedTask().equals(this)) {
					throw new OperationException(e.getMessage());
				}
			}
			currentState = state;
			currentStateThread = new Thread((Runnable) state);
		}

	}

	public final void setState(String stateClassName) throws OperationException {
		State newCurrentState = library.createState(stateClassName);
		if (newCurrentState != null) {
			setState(newCurrentState);
		}

	}

	/**
	 * If the Task and State processing needs a location to store an identifying
	 * TargetTransferTag, here it is. The TTT is not used identifying an
	 * appropriate Task for incoming PDUs.
	 * 
	 * @throws OperationException
	 */
	public void setTTT(SerialArithmeticNumber ttt) {
		targetTransferTag = ttt;
	}

	/**
	 * Assigns an appropriate PDU to this Task.
	 */
	public void assignPDU(ProtocolDataUnit pdu) {
		enqueueLock.lock();
		receivedPDUs.add(pdu);
		enqueueLock.unlock();
	}

	/**
	 * Assigns an appropriate PDU to this Task and tells the Task to process it.
	 * 
	 * @param pdu
	 */
	public void processPDU(ProtocolDataUnit pdu) {
		enqueueLock.lock();
		assignPDU(pdu);
		signalReceivedPDU();
		enqueueLock.unlock();
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
	 * The TargetTransferTag can be used within a targetTest environment to
	 * place information in a response PDU. The initiator guarantees to use the
	 * TTT for every following PDUs.
	 * 
	 * @return the task's TTT, if used.
	 */
	public SerialArithmeticNumber getTTT() {
		return targetTransferTag;
	}

	/**
	 * Returns the current processing State
	 * 
	 * @return current state
	 */
	public State getState() {
		return currentState;
	}
	
	/**
	 * Returns the Thread Object of the current Thread.
	 * @return
	 */
	public Thread getStateThread(){
		return currentStateThread;
	}

	/**
	 * Tells the Task to process a receivedPDU.
	 */
	public void signalReceivedPDU() {
		enqueueLock.lock();
		// signals the receivePDU method that a PDU arrived.
		if (receivedPDUs.size() > signaledPDUs) {
			signaledPDUs++;
			receivedPDU.signal();
		}
		enqueueLock.unlock();
	}

}

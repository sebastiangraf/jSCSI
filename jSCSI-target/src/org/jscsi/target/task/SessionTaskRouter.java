package org.jscsi.target.task;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.connection.Session;
import org.jscsi.target.task.TaskAbstracts.Task;
import org.jscsi.target.util.Singleton;

/**
 * A Session's TaskRouter will hold every active Task within a Session and all it's Connections.
 * The Task Router will decide the order the tasks process the received PDUs.
 * @author Marcus Specht
 *
 */
public class SessionTaskRouter extends Thread {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(Connection.class);
	
	/** The single TaskDescriptorLoader is used to create appropriate Tasks **/
	private TaskDescriptorLoader taskDescriptorLoader;
	
	/** the Session this TaskRouter is working for **/
	private final Session refSession;

	/** every active Task mapped by their ITTs **/
	private final Map<Integer, Task> activeTasks;
	
	
	private final LinkedList<Integer> signaledPDUs;

	/** the position in the signaled PDUs, where un-immediate PDUs start **/
	private int immediateListPosition;
	
	/**
	 * the SendingLock is used to synchronize every request for sending PDUs
	 */
	private final Lock signaledPDULock = new ReentrantLock();
	
	private final Lock awaitSignaledPDULock = new ReentrantLock();

	/**
	 * the Condition is used to signal waiting Threads for reveived PDUs
	 */
	private final Condition PDUReceived = awaitSignaledPDULock.newCondition();

	
	
	public SessionTaskRouter(Session refSesssion) {
		try {
			taskDescriptorLoader = Singleton
					.getInstance(TaskDescriptorLoader.class);
		} catch (ClassNotFoundException e) {

		}
		this.refSession = refSesssion;
		signaledPDUs = new LinkedList<Integer>();
		immediateListPosition = 0;
		activeTasks = new ConcurrentHashMap<Integer, Task>();
		// no active Tasks at Startup
	}

	private void createTask(ProtocolDataUnit initialPDU,
			Connection callingConnection) {
		// create a new Task that matches the intialPDUs
		Task newTask = null;
		try {
			newTask = taskDescriptorLoader.createTask(initialPDU,
					callingConnection);
		} catch (Exception e) {
			// here the target should start a Task sending an unsupported method
			// status response
			logDebug("Unsupported PDU arrived, target should send appropriate status response");
		}
		// forward initialPDU to the Task and add to Session's active Tasks
		newTask.assignPDU(initialPDU);
		addTask(newTask);

	}

	public void addTask(Task newTask) {
		if (!activeTasks.containsKey(newTask.getITT().getValue())) {
			activeTasks.put(newTask.getITT().getValue(), newTask);
		} else {
			logDebug("Tried to add a new Task with an already existing initiator task tag: ITT = " + newTask.getITT());
		}

	}

	public Task getTask(int initiatorTaskTag) {
		return activeTasks.get(initiatorTaskTag);
	}
	
	/**
	 * Enqueue the signaled PDU. Immediate will enqueue the signal at the head of the queue.
	 * @param immediate
	 * @param initiatorTaskTag
	 */
	private void queueSignaledPDU(boolean immediate, int initiatorTaskTag) {
		signaledPDULock.lock();
		if (immediate) {
			signaledPDUs.add(immediateListPosition, initiatorTaskTag);
			immediateListPosition++;
		} else {
			signaledPDUs.addLast(initiatorTaskTag);
		}
		//signal a received PDU
		PDUReceived.signal();
		signaledPDULock.unlock();
	}

	private int dequeueSignaledPDUs() {
		int result;
		//await a received PDU
		awaitSignaledPDULock.lock();
		while(signaledPDUs.size() <= 0){
			try {
				PDUReceived.await();
			} catch (InterruptedException e) {
				logDebug("Snychronization error while waiting for a received PDU");
			}
		}
		awaitSignaledPDULock.unlock();
		//dequeue the received PDU
		signaledPDULock.lock();
		result = signaledPDUs.remove();
		if(immediateListPosition > 0){
			immediateListPosition--;
		}
		signaledPDULock.unlock();
		return result;
	}
	
	/**
	 * Signals the Session's TaskRouter a new received PDU.
	 * The TaskRouter will poll the PDU, assign it to the appropriate active Task or
	 * will create a new one. This Task will be signaled processing the PDU later,
	 * when the TaskRouter decides to. 
	 * @param initiatorTaskTag the received PDU's ITT
	 * @param callingConnection the connection the PDU was received
	 */
	public void signalPDU(int initiatorTaskTag, Connection callingConnection) {
		if (activeTasks.containsKey(initiatorTaskTag)) {
			activeTasks.get(initiatorTaskTag).assignPDU(
					refSession.pollReceivedPDU());
		} else {
			createTask(refSession.pollReceivedPDU(), callingConnection);
		}
		queueSignaledPDU(false, initiatorTaskTag);
	}
	
	/**
	 * Signals the Session's TaskRouter a new received PDU with immediate flag set.
	 * The TaskRouter will poll the PDU, assign it to the appropriate active Task or
	 * will create a new one. This task will be signaled processing the PDU later,
	 * when the TaskRouter decides to. Immediate tasks will be processed before 
	 * normal/un-immediate  tasks.
	 * @param initiatorTaskTag
	 * @param callingConnection
	 */
	public void signalImmediatePDU(int initiatorTaskTag,
			Connection callingConnection) {
		if (activeTasks.containsKey(initiatorTaskTag)) {
			activeTasks.get(initiatorTaskTag).assignPDU(
					refSession.pollReceivedPDU());
		} else {
			createTask(refSession.pollReceivedPDU(), callingConnection);
		}
		queueSignaledPDU(true, initiatorTaskTag);
	}
	
	/**
	 * Signals Tasks to process received PDUs,
	 * in an order the signals are queued.
	 */
	public void run() {
		int initiatorTaskTag;
		while(!isInterrupted()){
			initiatorTaskTag = dequeueSignaledPDUs();
			activeTasks.get(initiatorTaskTag).signalReceivedPDU();
		}
	}

	/**
	 * Logs a trace Message specific to the session, if trace log is enabled
	 * within the logging environment.
	 * 
	 * @param logMessage
	 */
	private void logTrace(String logMessage) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(" Message: " + logMessage);

		}
	}

	/**
	 * Logs a debug Message specific to the session, if debug log is enabled
	 * within the logging environment.
	 * 
	 * @param logMessage
	 */
	private void logDebug(String logMessage) {
		if (LOGGER.isDebugEnabled()) {

			LOGGER.trace(" Message: " + logMessage);
		}
	}

}
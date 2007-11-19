package org.jscsi.target.task;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.connection.Session;

public class TargetTaskRouter {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(TargetTaskRouter.class);

	/** A map holding every active Session and their TaskRouter * */
	private final Map<Session, SessionTaskRouter> activeSessions;

	/** A queue holding every unprocessed signal * */
	private final Queue<TTRSignaledPDU> signaledPDUs;

	/** only to use Conditions signaling an enqueued TTRSignaledPDU * */
	private final Lock awaitSignaledPDULock = new ReentrantLock();

	/** signaling an enqueued TTRSignaledPDU * */
	private final Condition PDUReceived = awaitSignaledPDULock.newCondition();

	private final TargetTaskRouterWorker worker;

	public TargetTaskRouter() {
		activeSessions = new ConcurrentHashMap<Session, SessionTaskRouter>();
		signaledPDUs = new ConcurrentLinkedQueue<TTRSignaledPDU>();
		worker = new TargetTaskRouterWorker(this);
		logTrace("Started TargetTaskRouter");
	}

	public void assignSession(Session newSession) {
		if (!activeSessions.containsKey(newSession)) {
			activeSessions.put(newSession, newSession.getTaskRouter());
		} else {
			logDebug("Tried to assign a Session that is yet managed by the TargetTaskRouter: "
					+ newSession.getIdentifyingString());
		}
	}

	public void removeSession(Session session) {
		if (activeSessions.containsKey(session)) {
			activeSessions.remove(session);
		} else {
			logDebug("Tried to remove a Session that wasn't assigned to the TargetTaskRouter");
		}
	}

	public Set<Session> getActiveSessions() {
		return activeSessions.keySet();
	}

	public int numberOfSignaledPDUs() {
		return signaledPDUs.size();
	}

	/**
	 * Retrieves but does not removes a TTRSignaledPDU.
	 * 
	 * @return
	 */
	public TTRSignaledPDU peekSignaledPDU() {
		return signaledPDUs.peek();
	}

	/**
	 * Retrieves and removes a TTRSignaledPDU
	 * 
	 * @return
	 */
	public TTRSignaledPDU pollSignaledPDU() {
		return signaledPDUs.poll();
	}

	public int numberOfActiveSessions() {
		return getActiveSessions().size();
	}

	/**
	 * Returns a String containing some information about the router's current
	 * state. Absolutely human readable.
	 * 
	 * @return knowledge is power
	 */
	public String getStateDescribingString() {
		StringBuffer result = new StringBuffer();
		result.append("TargetTaskRouter is ");
		if (worker.isAlive()) {
			result.append("alive");
			if (worker.isInterrupted()) {
				result
						.append(" (I'm sorry to say, but the router is interrupted)");
			}
			result.append(": ");
			result.append("Number of active Sessions = ");
			result.append(numberOfActiveSessions());

		} else {
			result
					.append("dead (the lazy router stopped working, don't believe it).");
			result
					.append("Dr. Strangelove: 'No sane man would trigger the doomsday machine. It's triggered automatically'.");
		}
		return result.toString();
	}

	/**
	 * Signals the Session's TaskRouter a new received PDU. The TaskRouter will
	 * poll the PDU, assign it to the appropriate active Task or will create a
	 * new one. This Task will be signaled processing the PDU later, when the
	 * TaskRouter decides to.
	 * 
	 * @param initiatorTaskTag
	 *            the received PDU's ITT
	 * @param callingConnection
	 *            the connection the PDU was received
	 */
	public void signalPDU(Session callingSession, Connection refConnection,
			int initiatorTaskTag) {

		TTRSignaledPDU signal = new TTRSignaledPDU(callingSession,
				refConnection, initiatorTaskTag, false);
		signaledPDUs.add(signal);
		PDUReceived.signal();
	}

	/**
	 * Signals the Session's TaskRouter a new received PDU with immediate flag
	 * set. The TaskRouter will poll the PDU, assign it to the appropriate
	 * active Task or will create a new one. This task will be signaled
	 * processing the PDU later, when the TaskRouter decides to. Immediate tasks
	 * will be processed before normal/un-immediate tasks.
	 * 
	 * @param initiatorTaskTag
	 * @param callingConnection
	 */
	public void signalImmediatePDU(Session callingSession,
			Connection refConnection, int initiatorTaskTag) {
		TTRSignaledPDU signal = new TTRSignaledPDU(callingSession,
				refConnection, initiatorTaskTag, true);
		signaledPDUs.add(signal);
		PDUReceived.signal();
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

	private class TargetTaskRouterWorker extends Thread {

		TargetTaskRouter refTaskRouter;

		public TargetTaskRouterWorker(TargetTaskRouter refTaskRouter) {
			this.refTaskRouter = refTaskRouter;
		}

		@Override
		public void run() {
			int initiatorTaskTag;
			while (!isInterrupted()) {
				//if there is no signaled PDU, wait for one
				if (numberOfSignaledPDUs() <= 0) {
					awaitSignaledPDULock.lock();
					try {
						PDUReceived.await();
					} catch (InterruptedException e) {
						logDebug("Synchronization error occured waiting for a received PDU");
					}
					awaitSignaledPDULock.unlock();
				}
				TTRSignaledPDU signal = pollSignaledPDU();
				//if sigal is null, loop
				if (signal == null) {
					logDebug("Polled a TTRSignaledPDU that was a null Object");
					continue;
				}
				//forward necessary parameter to SessionTaskRouters
				if (signal.isImmediate()) {
					activeSessions.get(signal.getCallingSession())
							.signalImmediatePDU(signal.getInitiatorTaskTag(),
									signal.getReferencedConnection());
				} else {
					activeSessions.get(signal.getCallingSession()).signalPDU(
							signal.getInitiatorTaskTag(),
							signal.getReferencedConnection());
				}
			}
		}

	}

	/**
	 * A TargetTaskRouter signaled PDU is carrying every necessary information
	 * the Target environment needs to locate the correct Task.
	 * 
	 * @author Marcus Specht
	 * 
	 */
	private class TTRSignaledPDU {

		/** The Session that signalled an incoming PDU * */
		private final Session callingSession;

		/** The Session's Connection the PDU was received * */
		private final Connection referencedConnection;

		/** The InitiatorTaskTag the PDU is carrying * */
		private final int initiatorTaskTag;

		/** if the intiator wants to process the Task immediatily* */
		private final boolean immediate;

		public TTRSignaledPDU(Session callingSession,
				Connection referencedConnection, int initiatorTaskTag,
				boolean immediate) {
			this.callingSession = callingSession;
			this.referencedConnection = referencedConnection;
			this.initiatorTaskTag = initiatorTaskTag;
			this.immediate = immediate;
		}

		public Session getCallingSession() {
			return callingSession;
		}

		public Connection getReferencedConnection() {
			return referencedConnection;
		}

		public int getInitiatorTaskTag() {
			return initiatorTaskTag;
		}

		public boolean isImmediate() {
			return immediate;
		}

	}

}

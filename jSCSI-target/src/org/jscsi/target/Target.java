package org.jscsi.target;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.login.ISID;
import org.jscsi.target.conf.operationalText.OperationalTextConfiguration;
import org.jscsi.target.conf.operationalText.OperationalTextException;
import org.jscsi.target.conf.target.TargetConfiguration;
import org.jscsi.target.connection.Session;
import org.jscsi.target.connection.TargetSocketRouter;
import org.jscsi.target.task.TargetTaskRouter;
import org.jscsi.target.util.Singleton;

public class Target {
	
	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(TargetTaskRouter.class);
	
	private final Lock workingLOCK = new ReentrantLock();

	private final Condition shutdownCondition = workingLOCK.newCondition();
	
	/** all active Sessions and their targetTest session identifying handles */
	private final Map<Short, Session> sessions;
	
	private String targetName;
	
	private int targetPortalGroupTag;
	
	private TargetSocketRouter sessionRouter;
	
	private TargetTaskRouter taskRouter;
	
	private OperationalTextConfiguration operationalTextConfiguration;
	
	private TargetConfiguration configuration;
	
	
	public Target(){
		sessions = new ConcurrentHashMap<Short, Session>();
	}
	
	/**
	 * Return the SessionMap, which is a ConcurrentHashMap, e.g. should be
	 * thread safe.
	 * 
	 * @return the SessionMap
	 */
	public Map<Short, Session> getSessionMap() {
		return sessions;
	}

	/**
	 * Get the Session by the targetTest session identifying handle.
	 * 
	 * @param targetSessionIdentifyingHandle
	 *            the session's TSIH
	 * @return a session object or null if no such session
	 */
	public Session getSession(short targetSessionIdentifyingHandle) {
		return getSessionMap().get(targetSessionIdentifyingHandle);
	}

	/**
	 * Get the Session with the initiator session id and initiator name.
	 * 
	 * @param initiatorSessionID
	 *            the sessions ISID
	 * @param initiatorName
	 *            the sessions connected initiator name
	 * @return a session object or null if no such session
	 */
	public Session getSession(ISID initiatorSessionID, String initiatorName) {
		Iterator<Session> sessions = getSessions();
		Session result = null;
		// find matching Session
		while (sessions.hasNext()) {
			Session checkedSession = sessions.next();
			if (checkedSession.getInitiatorSessionID().equals(
					initiatorSessionID)
					&& checkedSession.getInitiatorName().equals(initiatorName)) {
				result = checkedSession;
				break;
			}
		}
		// returns null if no session matched
		return result;
	}
	
	public void awaitShutdown(){
		awaitShutdown(0);
	}
	
	public void awaitShutdown(int seconds) {
		boolean stop = false;
		workingLOCK.lock();
		
		while (!stop) {
			try {
				if(seconds <= 0){
					shutdownCondition.await();
				} else {
					shutdownCondition.await(seconds, TimeUnit.SECONDS);
				}
				stop = true;
			} catch (InterruptedException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER
							.debug("Synchronisation problem while awaiting shutdown");
				}
			}
		}
		workingLOCK.unlock();
	}

	public final void shutdown() {
		shutdownCondition.signal();
	}

	/**
	 * Returns an Iterator over all existing Sessions
	 * 
	 * @return all existing Sessions
	 */
	public Iterator<Session> getSessions() {
		return getSessionMap().values().iterator();
	}
	
	public void startWorking(){
		
	}
	
	public void stopWorking(){
		
	}
	
	public void initialize() throws TargetException, OperationalTextException{
		try {
			operationalTextConfiguration = OperationalTextConfiguration.createGlobalConfig();
			configuration = Singleton.getInstance(TargetConfiguration.class);
			taskRouter = Singleton.getInstance(TargetTaskRouter.class);
			sessionRouter = Singleton.getInstance(TargetSocketRouter.class);
			//operationalTextConfiguration.createGlobal();
		} catch (ClassNotFoundException e) {
			throw new TargetException("Error initializing targetTest. Error-message: " + e.getMessage());
		}
	}
	
	public void loadTargetConfiguration(){
		
	}
	
	/**
	 * Logs a trace Message, if trace log is enabled
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
	 * Logs a debug Message , if debug log is enabled
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

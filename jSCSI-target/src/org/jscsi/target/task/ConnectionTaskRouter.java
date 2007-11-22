package org.jscsi.target.task;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.task.abstracts.Task;

public class ConnectionTaskRouter {
	
	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(ConnectionTaskRouter.class);

	/** every active Task mapped by their ITTs **/
	private final Map<Integer, Task> activeTasks;
	
	private final Connection refConnection;
	
	public ConnectionTaskRouter(Connection refConnection){
		activeTasks = new ConcurrentHashMap<Integer, Task>();
		this.refConnection = refConnection;
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

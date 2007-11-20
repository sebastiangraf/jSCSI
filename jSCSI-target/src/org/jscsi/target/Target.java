package org.jscsi.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.conf.operationalText.OperationalTextConfiguration;
import org.jscsi.target.conf.target.TargetConfiguration;
import org.jscsi.target.connection.TargetSocketRouter;
import org.jscsi.target.task.TargetTaskRouter;
import org.jscsi.target.util.Singleton;

public class Target {
	
	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(TargetTaskRouter.class);
	
	private String targetName;
	
	private int targetPortalGroupTag;
	
	private TargetSocketRouter sessionRouter;
	
	private TargetTaskRouter taskRouter;
	
	private OperationalTextConfiguration operationalTextConfiguration;
	
	private TargetConfiguration configuration;
	
	
	public Target(){
		
	}
	
	public void startWorking(){
		
	}
	
	public void stopWorking(){
		
	}
	
	public void initialize() throws TargetException{
		try {
			sessionRouter = Singleton.getInstance(TargetSocketRouter.class);
			taskRouter = Singleton.getInstance(TargetTaskRouter.class);
			//operationalTextConfiguration.createGlobal();
			configuration = Singleton.getInstance(TargetConfiguration.class);
		} catch (ClassNotFoundException e) {
			throw new TargetException("Error initializing target. Error-message: " + e.getMessage());
		}
		
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

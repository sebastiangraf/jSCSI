package org.jscsi.target.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class TaskException extends Exception {
	
	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(TaskException.class);
	
	public TaskException() {
		super();
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Undefined Exception");
		}
	}

	public TaskException(String message) {
		super(message);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(message);
		}
	}

}

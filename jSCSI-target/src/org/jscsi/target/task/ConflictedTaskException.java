package org.jscsi.target.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ConflictedTaskException extends Exception {
	
	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(ConflictedTaskException.class);
	
	public ConflictedTaskException() {
		super();
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Undefined Exception");
		}
	}

	public ConflictedTaskException(String message) {
		super(message);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(message);
		}
	}

}
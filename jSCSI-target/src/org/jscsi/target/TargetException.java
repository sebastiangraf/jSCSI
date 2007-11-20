package org.jscsi.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.conf.operationalText.OperationalTextException;

public class TargetException extends Exception {
	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(TargetException.class);
	
	public TargetException() {
		// TODO Auto-generated constructor stub
		super("Undefined Exception");
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Undefined Exception");
		}
	}

	public TargetException(String message) {
		super(message);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(message);
		}
	}


}

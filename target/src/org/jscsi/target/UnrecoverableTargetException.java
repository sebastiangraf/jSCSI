package org.jscsi.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * The UnrecoverableTargetException should be the only (actually the second) Exception actually thrown out of the Target Environment.
 * This Exception should be used in any case the target environment cannot guarantee further processing in a correct way. 
 * @author marcus specht
 *
 */
public class UnrecoverableTargetException extends Exception {
	
	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(TargetException.class);
	
	public UnrecoverableTargetException() {
		// TODO Auto-generated constructor stub
		super("Undefined Exception");
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Undefined Exception");
		}
	}

	public UnrecoverableTargetException(String message) {
		super(message);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(message);
		}
	}
	
}

package org.jscsi.target;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * The TargetException should be the only Exception actually thrown out of the Target Environment.
 * In case the Exception cannot guarantee further correct processing, the UnrecoverableTargetException should be used,
 * so that the user can decide wether to ignore the Exception of should restart.
 * 
 * Filtering all Exceptions presented to the user should avoid confusion. 
 * Exceptions don't get lost since every unwanted errors or exceptions should be logged anyway.
 * 
 * @author marcus specht
 *
 */
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

package org.jscsi.target.conf;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Every exception thrown working with iSCSI operational-text-keys, -values, -pairs.
 * @author Marcus Specht
 *
 */
@SuppressWarnings("serial")
public class OperationalTextException extends Exception {
	
	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(OperationalTextException.class);
	
	public OperationalTextException() {
		super("Undefined Exception");
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug("Undefined Exception");
		}
	}

	public OperationalTextException(String message) {
		super(message);
		if(LOGGER.isDebugEnabled()){
			LOGGER.debug(message);
		}
	}

}

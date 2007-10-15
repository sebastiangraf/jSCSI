package org.jscsi.target.conf;

/**
 * Every exception thrown working with iSCSI operational-text-keys, -values, -pairs.
 * @author Marcus Specht
 *
 */
public class OperationalTextException extends Exception {

	public OperationalTextException() {
		super("Undefined Exception");
	}

	public OperationalTextException(String message) {
		super(message);
	}

}

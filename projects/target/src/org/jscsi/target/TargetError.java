package org.jscsi.target;

/**
 * <b>ATTENTION</b> - Don't forget, you cannot catch an error.
 * <br>
 * <p>
 * As alternative to Errors use the <b>UnrecoverableTargetException</b>,
 * which should be used to signal the user an unrecoverable error occurred.
 * No correct further processing can be guaranteed.
 * </p><br>
 * <p>
 * The TargetError should be the only Error actually thrown out of the Target Environment.
 * Filtering Errors presented to the user should avoid confusion. 
 * Exceptions don't get lost since every unwanted errors or exceptions should be logged anyway.
 * </p>
 * @author marcus specht
 *
 */
public class TargetError extends Error {

	public TargetError() {
		super("Undefined Error");
	}

	public TargetError(String message) {
		super("message");
	}

	
	
}

package org.jscsi.target.task.abstracts;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * An AbstractOperation builds the basis for all working task processors within
 * a targetTest environment. It provides several methods that can be especially
 * used for task management functions.
 * 
 * @author Marcus Specht
 * 
 */
public abstract class AbstractOperation implements Operation {

	/** The logger interface. */
	protected static final Log LOGGER = LogFactory
			.getLog(AbstractOperation.class);

	/** tag to signal the operation has to abort **/
	private boolean aborted = false;

	/** tag to signal that the operation finished **/
	private boolean finished = false;

	/** tag to signal the Operation was executed **/
	protected boolean executed = false;

	

	public AbstractOperation() {
		aborted = false;
		finished = false;
		executed = false;
	}

	

	/**
	 * Tags the working AbstractOperation as aborted. Abortion cannot be
	 * guaranteed, must be checked by the running operation.
	 * @throws OperationException 
	 */
	public void abort() throws OperationException {
		if (executed() && !finished()) {
			aborted = true;
			executed = false;
			return;
		}
		String reason = "";
		if (aborted()) {
			reason = "already aborted";
		}
		if (finished()) {
			reason = "already finished";
		}
		if (!executed()) {
			reason = "wasn't executed";
		}
		throw new OperationException("Cannot abort Operation: " + reason);

	}

	/**
	 * Checks whether the operation was aborted or not.
	 * 
	 * @return true if aborted, false else.
	 */
	public boolean aborted() {
		return aborted;
	}

	/**
	 * Executes the Operation if suspended.
	 */
	public void execute() throws OperationException {
		if ((!aborted()) && (!finished())) {
			executed = true;
			return;
		}
		String reason = "";
		if (aborted()) {
			reason = "was aborted";
		}
		if (finished()) {
			reason = "has finished";
		}
		if (executed()) {
			reason = "already executed";
		}
		throw new OperationException("Operation cannot be executed: " + reason);
	}

	public boolean executed() {
		return executed;
	}

	/**
	 * Called if Operation finished, i.e. that the Operation stopped itself
	 * successful or not.
	 * @throws OperationException 
	 * 
	 */
	protected void finish() throws OperationException {
		if (executed()) {
			finished = true;
			return;
		}
		throw new OperationException("Operation wasn't executed");
	}

	public boolean finished() {
		return finished;
	}



	

}

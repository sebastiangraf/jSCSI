package org.jscsi.target.task.abstracts;

public interface SuspendableOperation extends Operation {
	
	/**
	 * Tags the working Operation as suspended.
	 * Suspension cannot be guaranteed, must be checked
	 * and processed by the running operation. 
	 * @throws OperationException 
	 */
	public void suspend() throws OperationException;

	/**
	 * Checks whether the AbstractOperation was suspended or not.
	 * @return true if suspended, false else.
	 */
	public boolean suspended();
	
	/**
	 * Waits specified nanoSeconds for a restart.
	 * O is equal to infinity.
	 * @param nanosTimeout waiting time, 0 is equal infinity
	 * @return true if restarted, false else.
	 */
	public boolean awaitRestart(long nanosTimeout);
	
}

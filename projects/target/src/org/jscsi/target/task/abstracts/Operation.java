package org.jscsi.target.task.abstracts;

/**
 * An Operation builds the basis for all working task processors
 * within a targetTest environment. It provides several methods that can
 * be especially used for task management functions.
 * @author Marcus Specht
 *
 */
public interface Operation {
	
	/**
	 * Tags the working AbstractOperation as suspended.
	 * Suspension cannot be guaranteed, must be checked
	 * by the running operation. 
	 */
	public void supend();

	/**
	 * Checks whether the AbstractOperation was suspended or not.
	 * @return true if suspended, false else.
	 */
	public boolean suspended();

	/**
	 * Tags the working AbstractOperation as aborted.
	 * Abortion cannot be guaranteed, must be checked
	 * by the running operation.
	 */
	public void abort();
	
	/**
	 * Checks whether the operation was aborted or not.
	 * @return true if aborted, false else.
	 */
	public boolean aborted();
	
	/**
	 * Tags the operation as restarted.
	 * Restarting cannot be guaranteed, must be checked
	 * by the running operation.
	 * @throws OperationException
	 */
	public void restart() throws OperationException;
	
	/**
	 * Checks whether the operation was restarted or not.
	 * @return true if restarted, false else.
	 */
	public boolean restarted();

	/**
	 * Waits specified nanoSeconds for a restart.
	 * O is equal to infinity.
	 * @param nanosTimeout waiting time, 0 is equal infinity
	 * @return true if restarted, false else.
	 */
	public boolean awaitRestart(long nanosTimeout);
	
	/**
	 * Finish the Operation.
	 * @return
	 */
	public void finish();
	
	/**
	 * Checks whether the Operation finished or not.
	 * @return true if finished, false else.
	 */
	public boolean finished();

	
}

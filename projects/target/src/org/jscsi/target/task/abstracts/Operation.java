package org.jscsi.target.task.abstracts;

/**
 * An Operation builds the basis for all working task processors
 * within a target environment. It provides several methods that can
 * be especially used for task management functions.
 * @author Marcus Specht
 *
 */
public interface Operation {
	
	

	/**
	 * Tags the working AbstractOperation as aborted.
	 * Abortion cannot be guaranteed, must be checked
	 * and processed by the running operation.
	 * @throws OperationException 
	 */
	public void abort() throws OperationException;
	
	/**
	 * Checks whether the operation was aborted or not.
	 * @return true if aborted, false else.
	 */
	public boolean aborted();
	
	/**
	 * Checks whether the operation was restarted or not.
	 * @return true if restarted, false else.
	 */
	public boolean executed();
	
	
	public void execute() throws OperationException;
	
	/**
	 * Checks whether the Operation finished or not.
	 * @return true if finished, false else.
	 */
	public boolean finished();

	
}

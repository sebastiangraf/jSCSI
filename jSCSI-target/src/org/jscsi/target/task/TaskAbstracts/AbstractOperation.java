package org.jscsi.target.task.TaskAbstracts;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * An AbstractOperation builds the basis for all working task processors
 * within a target environment. It provides several methods that can
 * be especially used for task management functions.
 * @author Marcus Specht
 *
 */
public abstract class AbstractOperation implements Operation {
	
	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(AbstractOperation.class);
	
	/** tag to signal the operation should suspend */
	private boolean suspended = false;;

	/** tag to signal the operation should restart */
	private boolean restarted = false;

	/** tag to signal the operation has to abort */
	private boolean aborted = false;

	/** tag to signal that the operation finished * */
	private boolean finished = false;

	/** only purpose is to use conditions */
	private final Lock lock = new ReentrantLock();

	/** condition is used to wait for a restart */
	private final Condition restart = lock.newCondition();

	/**
	 * Tags the working AbstractOperation as suspended.
	 * Suspension cannot be guaranteed, must be checked
	 * by the running operation. 
	 */
	public void supend() {
		suspended = true;
		restarted = false;
	}

	/**
	 * Checks whether the AbstractOperation was suspended or not.
	 * @return true if suspended, false else.
	 */
	public boolean suspended() {
		return suspended;
	}

	/**
	 * Tags the working AbstractOperation as suspended.
	 * Suspension cannot be guaranteed, must be checked
	 * by the running operation.
	 */
	public void abort() {
		aborted = true;
	}
	
	/**
	 * Checks whether the operation was aborted or not.
	 * @return true if aborted, false else.
	 */
	public boolean aborted(){
		return aborted;
	}

	/**
	 * Tags the operation as restarted.
	 * Restarting cannot be guaranteed, must be checked
	 * by the running operation.
	 * @throws OperationException
	 */
	public void restart() throws OperationException{
		if (suspended) {
			suspended = false;
			restarted = true;
			restart.signal();
		} else
			throw new OperationException(
					"Task wasn't suspended, restart not necessary");
	}
	
	/**
	 * Checks whether the operation was restarted or not.
	 * @return true if restarted, false else.
	 */
	public boolean restarted(){
		return restarted;
	}

	/**
	 * Waits specified nanoSeconds for a restart.
	 * O is equal to infinity.
	 * @param nanosTimeout waiting time, 0 is equal infinity
	 * @return true if restarted, false else.
	 */
	public final boolean awaitRestart(long nanosTimeout) {
		lock.lock();
		if (!restarted()){
			try {
				if (nanosTimeout != 0) {
					restart.awaitNanos(nanosTimeout);
				} else {
					restart.await();
				}
			} catch (InterruptedException e) {
				if(LOGGER.isDebugEnabled()){
					LOGGER.debug("Synchronization Error while waiting for an Operation restart!");
				}
			}
		}
		lock.unlock();
		return restarted;
	}

	public void tagFinished(){
		finished = true;
	}
	
	public boolean finished() {
		return finished;
	}


}

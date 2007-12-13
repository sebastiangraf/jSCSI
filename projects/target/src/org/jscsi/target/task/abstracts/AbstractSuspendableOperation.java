package org.jscsi.target.task.abstracts;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AbstractSuspendableOperation extends AbstractOperation implements SuspendableOperation{
	
	/** tag to signal the operation should suspend **/
	private boolean suspended = false;
	
	/** only purpose is to use conditions */
	private final Lock lock = new ReentrantLock();

	/** condition is used to wait for a restart */
	private final Condition restart = lock.newCondition();
	
	public AbstractSuspendableOperation(){
		suspended = false;
	}
	
	/**
	 * Tags the working AbstractOperation as suspended. Suspension cannot be
	 * guaranteed, must be checked by the running operation.
	 * @throws OperationException 
	 */
	public void suspend() throws OperationException {
		if (!aborted() && !finished()) {
			suspended = true;
			executed = false;
			return;
		}
		String reason = "";
		if (aborted()) {
			reason = "was aborted";
		}
		if (finished()) {
			reason = "has finished";
		}
		throw new OperationException("Cannot suspend Operation: " + reason);

	}
	
	/**
	 * Executes or restarts the Operation if suspended.
	 */
	public void execute() throws OperationException {
		if ((!aborted()) && (!suspended()) && (!finished())) {
			executed = true;
			return;
		}
		//if suspended
		if ((!executed()) && (!aborted()) && (suspended()) && (!finished())) {
			suspended = false;
			executed = true;
			restart.signal();
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
	
	/**
	 * Waits specified nanoSeconds for a restart. O is equal to infinity.
	 * 
	 * @param nanosTimeout
	 *            waiting time, 0 is equal infinity
	 * @return true if executed, false else.
	 */
	public final boolean awaitRestart(long nanosTimeout) {
		lock.lock();
		if (!executed() && suspended) {
			try {
				if (nanosTimeout <= 0) {
					restart.awaitNanos(nanosTimeout);
				} else {
					restart.await();
				}
			} catch (InterruptedException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER
							.debug("Synchronization Error while waiting for an Operation restart!");
				}
			}
		}
		lock.unlock();
		return executed;
	}
	
	
	
	/**
	 * Checks whether the AbstractOperation was suspended or not.
	 * 
	 * @return true if suspended, false else.
	 */
	public boolean suspended() {
		return suspended;
	}
	
}

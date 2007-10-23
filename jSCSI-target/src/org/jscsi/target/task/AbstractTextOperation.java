package org.jscsi.target.task;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.conf.OperationalTextKey;

public abstract class AbstractTextOperation extends AbstractOperation implements
		TextOperation, Runnable {

	/** The logger interface. */
	private static final Log LOGGER = LogFactory
			.getLog(AbstractTextOperation.class);
	
	/** the referenced State*/
	private final State refState;
	
	/** set of supported Keys */
	private final Set<String> supportedKeys;
	
	/** queue for incoming OperationalTextKeys and their values*/
	private final Queue<OperationalTextKey> incomingTextParameters = new ConcurrentLinkedQueue<OperationalTextKey>();

	/** only purpose is to use conditions */
	private final Lock lock = new ReentrantLock();

	/** condition is used to wait for a receivedTextParameter */
	private final Condition receivedTextParameter = lock.newCondition();

	public AbstractTextOperation(State refState, Set<String> supportedKeys) {
		this.refState = refState;
		this.supportedKeys = supportedKeys;
	}

	public AbstractTextOperation(State refState, String supportedKey) {
		this.refState = refState;
		this.supportedKeys = new HashSet<String>();
		this.supportedKeys.add(supportedKey);
	}

	/**
	 * Waits specified nanoSeconds for a received OperationalTextParameter.
	 * O is equal to infinity.
	 * @param nanosTimeout waiting time, 0 is equal infinity
	 * @return received TextParameter or null, if waiting time exceeded.
	 */
	public OperationalTextKey receiveTextParameter(long nanoWaitingTime) {
		lock.lock();
		if (incomingTextParameters.isEmpty()) {
			try {
				if (nanoWaitingTime != 0) {
					receivedTextParameter.awaitNanos(nanoWaitingTime);
				} else {
					receivedTextParameter.await();
				}
			} catch (InterruptedException e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER
							.debug("Synchronization Error while waiting for a received TextParameter!");
				}
			}
		}
		OperationalTextKey result = incomingTextParameters.poll();
		lock.unlock();
		return result;
	}

	/**
	 * Assign a new OperationalTextKey to this TextOperation.
	 * @param key assigned OperationalTextKey
	 * @throws OperationException 
	 */
	public void assignParameter(OperationalTextKey key) throws OperationException {
		if(supportsKey(key)){
			incomingTextParameters.add(key);
		} else{
			throw new OperationException("The assigned TextParameter isn't supported: " + key.getKey());
		}
	}
	
	/**
	 * Checks if an OperationalTextKey is supported.
	 * @param key checked key
	 * @return true if supported, false else.
	 */
	public boolean supportsKey(OperationalTextKey key) {
		if (supportedKeys.contains(key.getKey())) {
			return true;
		}
		return false;
	}

	public State getReferencedState() {
		return refState;
	}

}

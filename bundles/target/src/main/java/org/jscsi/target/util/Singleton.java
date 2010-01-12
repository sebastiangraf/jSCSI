package org.jscsi.target.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class represents a Singleton pattern for all classes needed to be
 * Singleton.
 * 
 * @author Marcus Specht
 * 
 */
public final class Singleton {
	
	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(Singleton.class);
	
	/** used for synchronization */
	private final static Lock LOCK = new ReentrantLock();
	
	/** already instanced classes */
	private final static Map<String, Object> Singletons = new HashMap<String, Object>();;

	private Singleton() {
	}

	/**
	 * Returns the single instance of a class.
	 * @param <T> 
	 * @param classInstance wanted class 
	 * @return single object instance
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getInstance(Class<T> classInstance)
			throws ClassNotFoundException {
		T result = null;
		String className = classInstance.getName();
		LOCK.lock();
		if (Singletons.containsKey(className)) {
			result = (T) Singletons.get(className);
		} else {
			try {
				result = (T) Class.forName(className).newInstance();
			} catch (Exception e) {
				throw new ClassNotFoundException("Couldn't create instance: "
						+ className);
			}
			Singletons.put(className, result);
		}
		LOCK.unlock();
		return result;
	}
	
	/**
	 * Set the single class instance.
	 * @param classInstance
	 */
	public static void setInstance(Object classInstance){
		LOCK.lock();
		String className = classInstance.getClass().getName();
		Singletons.put(className, classInstance);
		LOCK.unlock();
		logTrace("Set single Instance for " + classInstance.getClass().getName());
		
	}
	
	/**
	 * Checks whether a class was already instanced or not.
	 * @param <T> Classes type parameter.
	 * @param classInstance checked class
	 * @return true if already instanced, false else
	 */
	public static <T> boolean hasInstance(Class<T> classInstance) {
		if (Singletons.containsKey(classInstance.getName())) {
			return true;
		}
		return false;

	}
	
	/**
	 * Logs a trace Message, if trace log is enabled
	 * within the logging environment.
	 * 
	 * @param logMessage
	 */
	private static void logTrace(String logMessage) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(" Message: " + logMessage);

		}
	}

	/**
	 * Logs a debug Message , if debug log is enabled
	 * within the logging environment.
	 * 
	 * @param logMessage
	 */
	private static void logDebug(String logMessage) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.trace(" Message: " + logMessage);
		}
	}
}
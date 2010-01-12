package org.jscsi.target;

import org.apache.commons.logging.LogFactory;
import org.jscsi.target.task.TargetTaskLibrary;
import org.jscsi.target.util.Singleton;

/**
 * The TargetObject will represent an extended standard java Object.class, offering
 * functionality useful (let's hope so) for jSCSI Target Development.   
 * 
 * @author marcus specht
 *
 */
public class TargetObject extends Object{
	
	
	/**
	 * Logs a trace Message for the logForClass argument class, if trace log is enabled within the logging
	 * environment.
	 * @param logMessage
	 */
	protected static void logTrace(Class logForClass, String logMessage) {
		if (LogFactory.getLog(logForClass).isTraceEnabled()) {
			LogFactory.getLog(logForClass).trace(" Message: " + logMessage);
		}
	}

	/**
	 * Logs a debug Message for the logForClass argument class, if debug log is enabled within the logging
	 * environment.
	 * 
	 * @param logMessage
	 */
	protected static void logDebug(Class logForClass, String logMessage) {
		if (LogFactory.getLog(logForClass).isDebugEnabled()) {
			LogFactory.getLog(logForClass).trace(" Message: " + logMessage);

		}
	}
	
	/**
	 * Returns the single instance representation of the requested Class.
	 * @param <T> The instance type of the requested class
	 * @param classObject the class request
	 * @return the single instance of the requested class
	 */
	protected static <T> T getSingleton(Class<T> classObject){
		if (!Singleton.hasInstance(classObject)) {
			try {
				Singleton.setInstance(classObject.newInstance());
			} catch (InstantiationException e) {
				logDebug(TargetObject.class, "Wasn't able to create a class instance storing it as Singleton: " + e.getMessage()); 
				
			} catch (IllegalAccessException e) {
				logDebug(TargetObject.class, "Wasn't able to create a class instance storing it as Singleton: " + e.getMessage());
			}
		}
		T classInstance = null;
		try {
			classInstance = Singleton.getInstance(classObject);
		} catch (ClassNotFoundException e) {
			throw new Error("Couldn't load instance of "
					+ TargetTaskLibrary.class);
		}
		return classInstance;
	}
	
}

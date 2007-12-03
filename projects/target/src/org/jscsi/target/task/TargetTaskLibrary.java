package org.jscsi.target.task;

import java.io.File;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.conf.target.TargetConfiguration;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.parameter.pdu.Opcode;
import org.jscsi.target.task.abstracts.State;
import org.jscsi.target.task.abstracts.Task;
import org.jscsi.target.task.abstracts.TaskDescriptor;
import org.jscsi.target.util.CreativeClassLoader;
import org.jscsi.target.util.Singleton;

public class TargetTaskLibrary {

	/** The Log interface. */
	private static final Log LOGGER = LogFactory
			.getLog(TargetTaskLoader.class);
	
	private static final Map<String, TaskDescriptor> loadedTaskDescriptors = new ConcurrentHashMap<String, TaskDescriptor>();

	private static final Map<String, Task> loadedTasks = new ConcurrentHashMap<String, Task>();

	private static final Map<String, State> loadedStates = new ConcurrentHashMap<String, State>();

	private static final CreativeClassLoader classLoader = CreativeClassLoader
			.getInstance();

	private TargetTaskLibrary() {

	}
	
	private TargetTaskLibrary(TargetConfiguration conf){
		
	}
	
	public final TaskDescriptor getTaskDescriptor(String name) {
		return loadedTaskDescriptors.get(name);

	}
	
	public final Set<TaskDescriptor> getTaskDescriptors(byte opcode){
		Set<TaskDescriptor> result = new HashSet<TaskDescriptor>();
		for(TaskDescriptor taskD : loadedTaskDescriptors.values()){
			if(taskD.getSupportedOpcode().equals(opcode)){
				result.add(taskD);
			}
		}
		return result;
	}
	
	/**
	 * Creates a TaskObject
	 * 
	 * @param initialPDU
	 * @param callingConnection
	 * @return
	 * @throws Exception
	 */
	public Task createTask(ProtocolDataUnit initialPDU,
			Connection callingConnection) throws Exception {
		byte opcode = initialPDU.getBasicHeaderSegment().getOpCode().value();
		for (TaskDescriptor matchingDescriptor : getTaskDescriptors(opcode)) {
			if (matchingDescriptor != null) {
				if (matchingDescriptor.check(callingConnection, initialPDU)) {
					return matchingDescriptor.createTask();
				}
			}
		}
		logDebug("Unsupported Opcode arrived: Opcode = " + opcode);
		throw new Exception("Couldn't find a matching Task: Opcode = " + opcode);

	}

	public final Task getTask(String name) {
		return loadedTasks.get(name);

	}

	public final State getState(String name) {
		return loadedStates.get(name);

	}

	

	public void putAll(Map<? extends String, ? extends State> m) {
		loadedStates.putAll(m);
	}

	public TaskDescriptor put(String key, TaskDescriptor value) {
		return loadedTaskDescriptors.put(key, value);
	}

	public Task put(String key, Task value) {
		return loadedTasks.put(key, value);
	}

	public static TargetTaskLibrary getInstance() {
		if (!Singleton.hasInstance(TargetTaskLibrary.class)) {
			Singleton.setInstance(new TargetTaskLibrary());
		}
		TargetTaskLibrary instance = null;
		try {
			instance = Singleton.getInstance(TargetTaskLibrary.class);
		} catch (ClassNotFoundException e) {
			throw new Error("Couldn't load instance of "
					+ TargetTaskLibrary.class);
		}
		return instance;
	}
	
	public void loadFrom(TargetConfiguration conf){
		Set<Class<?>> loadedClasses = null;
		for(File file : conf.getTaskDescriptorDirectories()){
			classLoader.loadAllClasses(loadedClasses, file, true, TaskDescriptor.class);
			for(Class<?> loadedDescriptor : loadedClasses){
				try {
					loadedTaskDescriptors.put(loadedDescriptor.getName(), (TaskDescriptor) loadedDescriptor.newInstance());
				} catch (InstantiationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
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
	 * Logs a debug Message, if debug log is enabled
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

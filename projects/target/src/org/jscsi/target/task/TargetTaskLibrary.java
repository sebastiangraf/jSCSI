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
import org.jscsi.target.task.abstracts.AbstractTaskDescriptor;
import org.jscsi.target.task.abstracts.State;
import org.jscsi.target.task.abstracts.Task;
import org.jscsi.target.task.abstracts.TaskDescriptor;
import org.jscsi.target.util.CreativeClassLoader;
import org.jscsi.target.util.Singleton;

public class TargetTaskLibrary {

	/** The Log interface. */
	private static final Log LOGGER = LogFactory.getLog(TargetTaskLoader.class);

	private static final Map<Byte, Set<TaskDescriptor>> loadedTaskDescriptors = new ConcurrentHashMap<Byte, Set<TaskDescriptor>>();

	private static final Map<String, Task> loadedTasks = new ConcurrentHashMap<String, Task>();

	private static final Map<String, State> loadedStates = new ConcurrentHashMap<String, State>();

	private static final CreativeClassLoader classLoader = CreativeClassLoader
			.getInstance();

	private TargetTaskLibrary() {

	}

	private TargetTaskLibrary(TargetConfiguration conf) {
		loadFrom(conf);
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

	public final TaskDescriptor getTaskDescriptor(String name) {
		for (Set<TaskDescriptor> oneSet : loadedTaskDescriptors.values()) {
			for (TaskDescriptor taskD : oneSet) {
				if (taskD.getClass().getName().equals(name)) {
					return taskD;
				}
			}
		}
		logTrace("Couldn't find TaskDescriptor: " + name);
		return null;
	}

	public final Set<TaskDescriptor> getTaskDescriptors(byte opcode) {
		return loadedTaskDescriptors.get(opcode);
	}

	

	public final Task getTask(String name) {
		return loadedTasks.get(name);

	}

	public final State getState(String name) {
		return loadedStates.get(name);

	}

	public int getNumberOfSupportedOpcodes(){
		return loadedTaskDescriptors.keySet().size();
	}
	
	public int getNumberOfAvailableTasks(){
		return loadedTasks.keySet().size();
	}
	
	public int getNumberOfAvailableTaskDescriptors(){
		return loadedTaskDescriptors.keySet().size();
	}
	
	public int getNumberOfAvailableStates(){
		return loadedStates.keySet().size();
	}
	
	public String getInfo(){
		StringBuffer result = new StringBuffer();
		result.append("TaskLibrary is supporting ");
		result.append(getNumberOfSupportedOpcodes());
		result.append(" different Opcodes (");
		for(Byte opcode : loadedTaskDescriptors.keySet()){
			result.append(opcode + ", ");
		}
		result.delete(result.length() - 2, result.length());
		result.append(") ");
		result.append("and loaded: ");
		result.append(getNumberOfAvailableTaskDescriptors() + " TaskDescriptors;");
		result.append(getNumberOfAvailableTasks() + " Tasks;");
		result.append(getNumberOfAvailableStates() + " States;");
		return result.toString();
	}


	public void addTaskDescriptor(TaskDescriptor descriptor)
			throws ConflictedTaskException {
		byte opcode = descriptor.getSupportedOpcode().value();
		// check if a descriptor yet exists, that has identical parameter
		if (loadedTaskDescriptors.containsKey(opcode)) {
			for (TaskDescriptor equalOpcode : loadedTaskDescriptors.get(opcode)) {
				if (equalOpcode.compare((AbstractTaskDescriptor) descriptor)) {
					throw new ConflictedTaskException(
							"Tried to load a TaskDescriptor that would conflict with an already existing one: "
									+ descriptor.getClass().getName()
									+ " and "
									+ equalOpcode.getClass().getName());
				}
			}
		}
		// no collision, add TaskDescriptor to library
		if (loadedTaskDescriptors.get(opcode) != null) {
			loadedTaskDescriptors.get(opcode).add(descriptor);
		}
		Set<TaskDescriptor> newSet = new HashSet<TaskDescriptor>();
		newSet.add(descriptor);
		loadedTaskDescriptors.put(opcode, newSet);
		
	}
	
	public TaskDescriptor addTaskDescriptor(Class<?> taskDescriptor){
		TaskDescriptor result = null;
		try {
			result = (TaskDescriptor) taskDescriptor.newInstance();
			addTaskDescriptor(result);
			logTrace("Added new TaskDescriptor: " + result.getInfo());
		} catch (Exception e) {
			//if Object is no TaskDescriptor, return null;
		}
		return result;
	}
	
	public Task addTask(Class<?> task){
		Task result = null;
		try {
			result = (Task) task.newInstance(); 
			addTask(result);
			logTrace("Added new Task: " + result);
		} catch (Exception e) {
			//if Object is no Task, return null;
		}
		return result;
	}

	public Task addTask(Task value) {
		if(!loadedTasks.containsKey(value.getClass().getName())){
			return loadedTasks.put(value.getClass().getName(), value);
		}
		logTrace("Tried to add aan already existing Task: " + value.getClass().getName());
		return null;
	}
	
	public State addState(Class<?> state){
		State result = null;
		try {
			result = (State) state.newInstance();
			addState(result);
			logTrace("Added new State: " + result);
		} catch (Exception e) {
			//if Object is no State, return null;
		}
		return result;
	}
	
	public State addState(State value) {
		if(!loadedStates.containsKey(value.getClass().getName())){
			return loadedStates.put(value.getClass().getName(), value);
		}
		logTrace("Tried to add aan already existing Task: " + value.getClass().getName());
		return null;
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

	public void loadFrom(TargetConfiguration conf) {
		Set<Class<?>> loadedClasses = null;
		for (File file : conf.getTaskDescriptorDirectories()) {
			loadedClasses = classLoader.loadAllClasses(null, file, true,
					TaskDescriptor.class);
			for (Class<?> loadedDescriptor : loadedClasses) {
				addTaskDescriptor(loadedDescriptor);
			}
			loadedClasses = classLoader.loadAllClasses(null, file, true,
					Task.class);
			for (Class<?> loadedTask : loadedClasses) {
				addTask(loadedTask);
			}
			loadedClasses = classLoader.loadAllClasses(null, file, true,
					State.class);
			for (Class<?> loadedState : loadedClasses) {
				addState(loadedState);
			}
		}
		logTrace(getInfo());
	}
	

	/**
	 * Logs a trace Message, if trace log is enabled within the logging
	 * environment.
	 * 
	 * @param logMessage
	 */
	private static void logTrace(String logMessage) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace(" Message: " + logMessage);
		}
	}

	/**
	 * Logs a debug Message, if debug log is enabled within the logging
	 * environment.
	 * 
	 * @param logMessage
	 */
	private static void logDebug(String logMessage) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.trace(" Message: " + logMessage);

		}
	}

}

package org.jscsi.target.task;

import java.io.File;
import java.net.URL;
import java.util.Collection;
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
import org.jscsi.target.task.abstracts.AbstractState;
import org.jscsi.target.task.abstracts.AbstractTask;
import org.jscsi.target.task.abstracts.AbstractTaskDescriptor;
import org.jscsi.target.task.abstracts.AbstractTextOperation;
import org.jscsi.target.task.abstracts.AbstractTextOperationDescriptor;
import org.jscsi.target.task.abstracts.AbstractTextOperationState;
import org.jscsi.target.task.abstracts.State;
import org.jscsi.target.task.abstracts.Task;
import org.jscsi.target.task.abstracts.TaskDescriptor;
import org.jscsi.target.task.abstracts.TextOperation;
import org.jscsi.target.task.abstracts.TextOperationDescriptor;
import org.jscsi.target.task.abstracts.TextOperationState;
import org.jscsi.target.util.CreativeClassLoader;
import org.jscsi.target.util.Singleton;

public class TargetTaskLibrary {

	/** The Log interface. */
	private static final Log LOGGER = LogFactory.getLog(TargetTaskLoader.class);
	
	/** A Set of TaskDescriptors mapped by their opcodes **/
	private static final Map<String, Class<? extends AbstractTaskDescriptor>> loadedTaskDescriptors = new ConcurrentHashMap<String, Class<? extends AbstractTaskDescriptor>>();
	
	private static final Map<String, Class<? extends AbstractTextOperationDescriptor>> loadedTextOperationDescriptors = new ConcurrentHashMap<String, Class<? extends AbstractTextOperationDescriptor>>();

	private static final Map<String, Class<? extends AbstractTextOperation>> loadedTextOperations = new ConcurrentHashMap<String, Class<? extends AbstractTextOperation>>();
	
	private static final Map<String, Class<? extends AbstractTextOperationState>> loadedTextOperationState = new ConcurrentHashMap<String, Class<? extends AbstractTextOperationState>>();
	
	private static final Map<String, Class<? extends AbstractTask>> loadedTasks = new ConcurrentHashMap<String, Class<? extends AbstractTask>>();

	private static final Map<String, Class<? extends AbstractState>> loadedStates = new ConcurrentHashMap<String, Class<? extends AbstractState>>();

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
	 *//*
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
	}*/
	
	public final Task createTask(String name){
		Task newTask = null;
		try {
			newTask = getTask(name).newInstance();
		} catch (Exception e){
			
		}
		return newTask;
	}
	
	public final State createState(String name){
		State newState = null;
		try {
			newState = getState(name).newInstance();
		} catch (Exception e){
			
		}
		return newState;
	}
	

	public final Class<? extends TaskDescriptor> getTaskDescriptor(String name) {
		return loadedTaskDescriptors.get(name);

	}

	public final Class<? extends AbstractTask> getTask(String name) {
		return loadedTasks.get(name);

	}

	public final Class<? extends AbstractState> getState(String name) {
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
		/*for(Byte opcode : loadedTaskDescriptors.keySet()){
			result.append(opcode + ", ");
		}*/
		result.delete(result.length() - 2, result.length());
		result.append(") ");
		result.append("and loaded: ");
		result.append(getNumberOfAvailableTaskDescriptors() + " TaskDescriptors;");
		result.append(getNumberOfAvailableTasks() + " Tasks;");
		result.append(getNumberOfAvailableStates() + " States;");
		return result.toString();
	}


	/*public void addTaskDescriptor(TaskDescriptor descriptor)
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
		
	}*/
	
	public Class<? extends AbstractTaskDescriptor> addTaskDescriptor(Class<? extends AbstractTaskDescriptor> newTaskDescriptor){
		return addObject(newTaskDescriptor, loadedTaskDescriptors, AbstractTaskDescriptor.class);
	}
	

	public Class<? extends AbstractTask> addTask(Class<? extends AbstractTask> newTaskClass) {
		return addObject(newTaskClass, loadedTasks, AbstractTask.class);
	}
	
	public static <T> Class<T> addObject(Class<? extends T> object, Map<String, Class<? extends T>> map, Class<T> type){
		boolean valid = true;
		//already added Task ? 
		if(!map.containsKey(object.getClass().getName())){
			try {
				// can create instance ?
				object.newInstance();
			} catch (Exception e){
				valid = false;
			}
			
		} else {
			valid = false;
			logTrace("Tried to add an already existing entity: " + object.getName());
		}if(valid){
			logTrace("Added new entity: " + object.getName());
			return (Class<T>) map.put(object.getName(), object);
		}
		return null;
	}
	
	public Class<? extends AbstractState> addState(Class<? extends AbstractState> newState){
		return addObject(newState, loadedStates, AbstractState.class);
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
		for (File file : conf.getTaskDescriptorDirectories()) {
			Set<Class<? extends AbstractTaskDescriptor>>  loadedTaskDescriptor = classLoader.loadAllClassesHavingSuperclass(null, file, true,
					AbstractTaskDescriptor.class);
			for (Class<? extends AbstractTaskDescriptor> loadedDescriptor : loadedTaskDescriptor) {
				addTaskDescriptor(loadedDescriptor);
			}
			Set<Class<? extends AbstractTask>> loadedClasses = classLoader.loadAllClassesHavingSuperclass(null, file, true,
					AbstractTask.class);
			for (Class<? extends AbstractTask> loadedTask : loadedClasses) {
				addTask(loadedTask);
			}
			Set<Class<? extends AbstractState>> loadedStates = classLoader.loadAllClassesHavingSuperclass(null, file, true,
					AbstractState.class);
			for (Class<? extends AbstractState> loadedState : loadedStates) {
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

package org.jscsi.target.task;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.conf.target.TargetConfiguration;
import org.jscsi.target.task.abstracts.AbstractState;
import org.jscsi.target.task.abstracts.AbstractTask;
import org.jscsi.target.task.abstracts.AbstractTaskDescriptor;
import org.jscsi.target.task.abstracts.AbstractTextOperation;
import org.jscsi.target.task.abstracts.AbstractTextOperationDescriptor;
import org.jscsi.target.task.abstracts.AbstractTextOperationState;
import org.jscsi.target.task.abstracts.State;
import org.jscsi.target.task.abstracts.Task;
import org.jscsi.target.task.abstracts.TaskDescriptor;
import org.jscsi.target.util.CreativeClassLoader;
import org.jscsi.target.util.Singleton;

public class TargetTaskLibrary {

	/** The Log interface. */
	private static final Log LOGGER = LogFactory.getLog(TargetTaskLoader.class);

	/** A Set of TaskDescriptors mapped by their opcodes * */
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
	 */
	/*
	 * public Task createTask(ProtocolDataUnit initialPDU, Connection
	 * callingConnection) throws Exception { byte opcode =
	 * initialPDU.getBasicHeaderSegment().getOpCode().value(); for
	 * (TaskDescriptor matchingDescriptor : getTaskDescriptors(opcode)) { if
	 * (matchingDescriptor != null) { if
	 * (matchingDescriptor.check(callingConnection, initialPDU)) { return
	 * matchingDescriptor.createTask(); } } } logDebug("Unsupported Opcode
	 * arrived: Opcode = " + opcode); throw new Exception("Couldn't find a
	 * matching Task: Opcode = " + opcode); }
	 * 
	 * public final TaskDescriptor getTaskDescriptor(String name) { for (Set<TaskDescriptor>
	 * oneSet : loadedTaskDescriptors.values()) { for (TaskDescriptor taskD :
	 * oneSet) { if (taskD.getClass().getName().equals(name)) { return taskD; } } }
	 * logTrace("Couldn't find TaskDescriptor: " + name); return null; }
	 * 
	 * public final Set<TaskDescriptor> getTaskDescriptors(byte opcode) {
	 * return loadedTaskDescriptors.get(opcode); }
	 */

	public final Task createTask(String name) {
		Task newTask = null;
		try {
			newTask = getTask(name).newInstance();
		} catch (Exception e) {

		}
		return newTask;
	}

	public final State createState(String name) {
		State newState = null;
		try {
			newState = getState(name).newInstance();
		} catch (Exception e) {

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

	public int getNumberOfSupportedOpcodes() {
		return loadedTaskDescriptors.keySet().size();
	}

	public int getNumberOfAvailableTasks() {
		return loadedTasks.keySet().size();
	}

	public int getNumberOfAvailableTaskDescriptors() {
		return loadedTaskDescriptors.keySet().size();
	}

	public int getNumberOfAvailableStates() {
		return loadedStates.keySet().size();
	}

	public String getInfo() {
		StringBuffer result = new StringBuffer();
		result.append("TaskLibrary is supporting ");
		result.append(getNumberOfSupportedOpcodes());
		result.append(" different Opcodes (");
		for(Class<? extends AbstractTaskDescriptor> desc : loadedTaskDescriptors.values()){
			boolean error = false;
			TaskDescriptor descInst = null;
			try {
				descInst = desc.newInstance();
			} catch (InstantiationException e) {
				error = true;
			} catch (IllegalAccessException e) {
				error = true;
			}
			if(!error){
				result.append(descInst.getSupportedOpcode().value() + ", ");
			}
		}
		result.delete(result.length() - 2, result.length());
		result.append(") ");
		result.append("and loaded: ");
		result.append(getNumberOfAvailableTaskDescriptors()
				+ " TaskDescriptors;");
		result.append(getNumberOfAvailableTasks() + " Tasks;");
		result.append(getNumberOfAvailableStates() + " States;");
		return result.toString();
	}

	/*
	 * public void addTaskDescriptor(TaskDescriptor descriptor) throws
	 * ConflictedTaskException { byte opcode =
	 * descriptor.getSupportedOpcode().value(); // check if a descriptor yet
	 * exists, that has identical parameter if
	 * (loadedTaskDescriptors.containsKey(opcode)) { for (TaskDescriptor
	 * equalOpcode : loadedTaskDescriptors.get(opcode)) { if
	 * (equalOpcode.compare((AbstractTaskDescriptor) descriptor)) { throw new
	 * ConflictedTaskException( "Tried to load a TaskDescriptor that would
	 * conflict with an already existing one: " +
	 * descriptor.getClass().getName() + " and " +
	 * equalOpcode.getClass().getName()); } } } // no collision, add
	 * TaskDescriptor to library if (loadedTaskDescriptors.get(opcode) != null) {
	 * loadedTaskDescriptors.get(opcode).add(descriptor); } Set<TaskDescriptor>
	 * newSet = new HashSet<TaskDescriptor>(); newSet.add(descriptor);
	 * loadedTaskDescriptors.put(opcode, newSet); }
	 */

	public Class<? extends AbstractTaskDescriptor> addTaskDescriptor(
			Class<? extends AbstractTaskDescriptor> newTaskDescriptor) {
		return addObject(newTaskDescriptor, loadedTaskDescriptors,
				AbstractTaskDescriptor.class);
	}

	public Class<? extends AbstractTask> addTask(
			Class<? extends AbstractTask> newTaskClass) {
		return addObject(newTaskClass, loadedTasks, AbstractTask.class);
	}

	/**
	 * Adds a new instance from given object as value to the given map, using
	 * the object's name as key. type is used to restrict map's value and object
	 * to a given superclass.
	 * 
	 * @param <T>
	 *            Both given object and map's value must have T as superclass
	 * @param object
	 *            the object that will be added to the map. object's name will
	 *            be used as maps key
	 * @param map
	 *            object's name and object will be added to the map
	 * @param type
	 *            Both given object and map's value must have T as superclass
	 * @return
	 */
	public static <T> Class<T> addObject(Class<? extends T> object,
			Map<String, Class<? extends T>> map, Class<T> type) {
		boolean valid = true;
		String error = null;
		// already added Task ?
		if (map.containsKey(object.getClass().getName())) {
			valid = false;
			error = "Tried to add an already existing entity: "
					+ object.getName();
		}
		// if no instance can be created -> error
		try {
			object.newInstance();
		} catch (Exception e) {
			valid = false;
			error = "Cannot create an instance from " + object.getName();
		}
		// everything ok
		if (valid) {
			if (type.equals(AbstractTaskDescriptor.class)) {
				logTrace("Added new TaskDescriptor: " + object.getName());
			} else {
				if (type.equals(AbstractTask.class)) {
					logTrace("Added new Task: " + object.getName());
				} else {
					if (type.equals(AbstractState.class)) {
						logTrace("Added new State: " + object.getName());
					} else {
						if (type.equals(AbstractTextOperationDescriptor.class)) {
							logTrace("Added new TextOperationDescriptor: "
									+ object.getName());
						} else {
							if (type.equals(AbstractTextOperation.class)) {
								logTrace("Added new TextOperation: "
										+ object.getName());
							} else {
								if (type
										.equals(AbstractTextOperationState.class)) {
									logTrace("Added new TaskOperationState: "
											+ object.getName());
								} else {
									logTrace("Added new \"whatever unknown entity\": "
											+ object.getName());
								}
							}
						}
					}
				}
			}
			return (Class<T>) map.put(object.getName(), object);
		}
		// error occurred, log error and return null
		logDebug(error);
		return null;
	}

	public Class<? extends AbstractState> addState(
			Class<? extends AbstractState> newState) {
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
			Set<Class<? extends AbstractTaskDescriptor>> loadedTaskDescriptor = classLoader
					.loadAllClassesHavingSuperclass(null, file, true,
							AbstractTaskDescriptor.class);
			for (Class<? extends AbstractTaskDescriptor> loadedDescriptor : loadedTaskDescriptor) {
				addTaskDescriptor(loadedDescriptor);
			}
			Set<Class<? extends AbstractTask>> loadedClasses = classLoader
					.loadAllClassesHavingSuperclass(null, file, true,
							AbstractTask.class);
			for (Class<? extends AbstractTask> loadedTask : loadedClasses) {
				addTask(loadedTask);
			}
			Set<Class<? extends AbstractState>> loadedStates = classLoader
					.loadAllClassesHavingSuperclass(null, file, true,
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

package org.jscsi.target.task;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.conf.target.TargetConfiguration;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.task.abstracts.AbstractTaskDescriptor;
import org.jscsi.target.task.abstracts.Task;
import org.jscsi.target.task.abstracts.TaskDescriptor;

/**
 * The TaskDescriptorLoader looks for all TaskDescriptors within a given
 * directory. All valid ones will be stored and the linked Tasks can be loaded.
 * if the
 * 
 * @author Marcus Specht
 * 
 */
public class TaskDescriptorLoader {

	/** The Log interface. */
	private static final Log LOGGER = LogFactory
			.getLog(TaskDescriptorLoader.class);


	private Map<Byte, Set<TaskDescriptor>> availableTaskDescriptors;

	private final Set<File> taskDescritptorDirectories;

	public TaskDescriptorLoader(TargetConfiguration config) throws Exception {
		taskDescritptorDirectories = config.getTaskDescriptorDirectories();
		load();
		
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
		for (TaskDescriptor matchingDescriptor : getTaskDescriptor(opcode)) {
			if (matchingDescriptor != null) {
				if (matchingDescriptor.check(callingConnection, initialPDU)) {
					return matchingDescriptor.createTask();
				}
			}
		}
		logDebug("Unsupported Opcode arrived: Opcode = " + opcode);
		throw new Exception("Couldn't find a matching Task: Opcode = " + opcode);

	}

	private Set<TaskDescriptor> getTaskDescriptor(byte opcode) {
		return availableTaskDescriptors.get(opcode);
	}

	/**
	 * Returns the number of different Opcodes supported
	 * 
	 * @return
	 */
	private int getSuppurtedNumberOfOpcodes() {
		return availableTaskDescriptors.keySet().size();
	}

	
	public void load(){
		logTrace("Loading available task descriptor files from " + taskDescritptorDirectories.size() + " different directories:");
		logTrace("");
		Map<Byte, Set<TaskDescriptor>> newLoaded = new ConcurrentHashMap<Byte, Set<TaskDescriptor>>();	
		for(File directory : taskDescritptorDirectories){
			logTrace("Loading from " + directory.getAbsolutePath());
			loadAvailableTaskDescriptors(directory, newLoaded);
		}
		availableTaskDescriptors = newLoaded;
		logTrace("TaskLoader is supporting " + getSuppurtedNumberOfOpcodes()
				+ " different Opcodes using "
				+ getTotalNumberOfImplementedTasks()
				+ " different implemented Tasks");
	}
	
	/**
	 * Loads one TaskDescriptor from a file.
	 * @param taskDescriptor file address
	 * @return
	 * @throws TaskException
	 * @throws ConflictedTaskException
	 */
	private TaskDescriptor loadAvailableTaskDescriptor(File taskDescriptor)
			throws TaskException, ConflictedTaskException {
		String className = null;
		TaskDescriptor loadedTaskDescriptor = null;
		Class<?> conflictedTaskDescriptor = null;
		if (taskDescriptor.isFile()) {
			byte loadedOpcode;
			className = taskDescriptor.getAbsolutePath();
			// try to load a java Object from the file
			try {
				
				ClassLoader.getSystemClassLoader().loadClass(className);
				//loadedTaskDescriptor = (TaskDescriptor) Class
					//	.forName(className).newInstance();
			} catch (Exception e) {
				e.printStackTrace();
				throw new TaskException(
						"Loading TaskDescriptor failed, no valid TaskDescriptor.class: "
								+ e.getMessage());

			}
			// if Object is a TaskDescriptor, check if a descriptor yet
			// exists, that has identical parameter
			loadedOpcode = loadedTaskDescriptor.getSupportedOpcode().value();
			if (availableTaskDescriptors.containsKey(loadedOpcode)) {
				for (TaskDescriptor equalOpcode : availableTaskDescriptors
						.get(loadedOpcode)) {
					if (equalOpcode
							.compare((AbstractTaskDescriptor) loadedTaskDescriptor)) {
						conflictedTaskDescriptor = equalOpcode.getClass();
						throw new ConflictedTaskException(
								"Tried to load a TaskDescriptor that would conflict with an already existing one: "
										+ taskDescriptor.getAbsolutePath()
										+ " and "
										+ conflictedTaskDescriptor.getName());
					}
				}
			}

		} else {
			throw new TaskException("Parameter is no file address: "
					+ taskDescriptor.getAbsolutePath());
		}
		return loadedTaskDescriptor;

	}
	
	/**
	 * Loads recursively every contained <? extends AbstractTaskDescriptor>.class into
	 * loadedTaskDescriptors. LoadedTaskDescriptors may be null 
	 * @param rootDirectory will load recursively from root
	 * @param loadedTaskDescriptors may be null
	 * @return Map filled with all loaded TaskDescriptor
	 */
	private Map<Byte, Set<TaskDescriptor>> loadAvailableTaskDescriptors(
			File rootDirectory,
			Map<Byte, Set<TaskDescriptor>> loadedTaskDescriptors) {
		if (loadedTaskDescriptors == null) {
			loadedTaskDescriptors = new ConcurrentHashMap<Byte, Set<TaskDescriptor>>();
		}
		for (File possibleTaskDescriptor : rootDirectory.listFiles()) {
			if (possibleTaskDescriptor.isDirectory()) {
				loadedTaskDescriptors.putAll(loadAvailableTaskDescriptors(
						possibleTaskDescriptor, loadedTaskDescriptors));
			} else {
				TaskDescriptor newDescriptor;
				try {
					newDescriptor = loadAvailableTaskDescriptor(possibleTaskDescriptor);
				} catch (Exception e) {
					continue;
				}
				if (!loadedTaskDescriptors.containsKey(newDescriptor
						.getSupportedOpcode())) {
					Set<TaskDescriptor> newSet = new HashSet<TaskDescriptor>();
					newSet.add(newDescriptor);
					loadedTaskDescriptors.put(newDescriptor
							.getSupportedOpcode().value(), newSet);
				} else {
					loadedTaskDescriptors.get(
							newDescriptor.getSupportedOpcode().value()).add(
							newDescriptor);
				}

			}
		}
		return loadedTaskDescriptors;
	}

	/**
	 * Returns the total number of different loaded Task objects the TaskLoader
	 * can use. This number is equal or greater thán the supported number of
	 * different Opcodes.
	 * 
	 * @return
	 */
	private int getTotalNumberOfImplementedTasks() {
		int result = 0;
		for (byte opcode : availableTaskDescriptors.keySet()) {
			result += availableTaskDescriptors.get(opcode).size();
		}
		return result;
	}

	/**
	 * Logs a trace Message specific to this Connection, if trace log is enabled
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
	 * Logs a debug Message specific to this Connection, if debug log is enabled
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

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

	private static final String DEFAULT_TASK_DESCRIPTOR_DIRECTORY = "task/tasks/";

	private static final String TASK_DESCRIPTOR = "TaskDescriptor";

	private final Map<Byte, Set<TaskDescriptor>> availableTaskDescriptors;

	private final Set<File> taskDescritptorDirectories;

	public TaskDescriptorLoader(TargetConfiguration config) throws Exception {
		taskDescritptorDirectories = config.getTaskDescriptorDirectories();
		availableTaskDescriptors = loadAvailableTasks(this.taskDescritptorDirectories);
		logTrace("TaskLoader is supporting " + getSuppurtedNumberOfOpcodes()
				+ " different Opcodes using "
				+ getTotalNumberOfImplementedTasks()
				+ " different implemented Tasks");
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
	 * Loads every TaskDescriptor the specified directory contains.
	 */
	public static Map<Byte, Set<TaskDescriptor>> loadAvailableTasks(
			Set<File> taskDirectories) {
		Map<Byte, Set<TaskDescriptor>> availableTasks = new ConcurrentHashMap<Byte, Set<TaskDescriptor>>();
		String className = null;
		TaskDescriptor loadedTaskDescriptor;
		boolean test;
		for (File directory : taskDirectories) {
			for (File possibleTaskDescriptor : directory.listFiles()) {
				test = true;
				byte loadedOpcode;
				Class<?> conflictedTaskDescriptor = null;
				if (possibleTaskDescriptor.isFile()) {
					className = possibleTaskDescriptor.getName();
					// try to load a java Object from the file
					try {
						loadedTaskDescriptor = (TaskDescriptor) Class.forName(
								className).newInstance();
					} catch (Exception e) {
						logDebug("Loading TaskDescriptor failed: " + className);
						continue;
					}
					// if Object is a TaskDescriptor, check if a descriptor yet
					// exists, that has identical parameter
					loadedOpcode = (loadedTaskDescriptor).getSupportedOpcode()
							.value();
					if (availableTasks.containsKey(loadedOpcode)) {
						for (TaskDescriptor equalOpcode : availableTasks
								.get(loadedOpcode)) {
							if (equalOpcode
									.compare((AbstractTaskDescriptor) loadedTaskDescriptor)) {
								test = false;
								conflictedTaskDescriptor = equalOpcode
										.getClass();
								break;
							}
						}
					}

				} else {
					logDebug("Couldn't load TaskDescriptor, file is no TaskDescriptor: "
							+ possibleTaskDescriptor.getAbsolutePath());
					continue;
				}
				if (test) {
					// if there isn't yet a loaded Set, create one
					if (!availableTasks.containsKey(loadedOpcode)) {
						Set<TaskDescriptor> newDescriptorSet = new HashSet<TaskDescriptor>();
						availableTasks.put(loadedOpcode, newDescriptorSet);
					}
					// add Task to the appropriate Set
					availableTasks.get(loadedOpcode).add(loadedTaskDescriptor);
					logTrace("Succesfully loaded Task Descriptor: " + className);
				} else {
					logDebug("Tried to load a TaskDescriptor that would conflict with an already existing one: "
							+ possibleTaskDescriptor.getAbsolutePath());
				}
			}
		}
		logTrace("Loaded " + availableTasks.size() + " task descriptors from " + taskDirectories.size() + " different directories");
		return availableTasks;
	}

	/**
	 * Returns the number of different Opcodes supported
	 * 
	 * @return
	 */
	private int getSuppurtedNumberOfOpcodes() {
		return availableTaskDescriptors.keySet().size();
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

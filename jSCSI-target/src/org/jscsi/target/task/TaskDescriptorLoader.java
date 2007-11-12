package org.jscsi.target.task;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.conf.OperationalTextConfiguration;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.task.TaskAbstracts.AbstractTaskDescriptor;
import org.jscsi.target.task.TaskAbstracts.Task;
import org.jscsi.target.task.TaskAbstracts.TaskDescriptor;

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

	private final Map<Byte, TaskDescriptor> availableTaskDescriptors;

	private final File taskDescritptorDirectory;

	public TaskDescriptorLoader() {
		this.taskDescritptorDirectory = new File(
				DEFAULT_TASK_DESCRIPTOR_DIRECTORY);
		availableTaskDescriptors = loadAvailableTasks(taskDescritptorDirectory);
	}

	public TaskDescriptorLoader(String taskDirectory) throws Exception {
		this.taskDescritptorDirectory = new File(taskDirectory);
		if (!this.taskDescritptorDirectory.isDirectory()) {
			throw new Exception(
					"Cannot load tasks , argument is not a directory: "
							+ taskDirectory);
		}
		availableTaskDescriptors = loadAvailableTasks(this.taskDescritptorDirectory);

	}

	/**
	 * Creates a TaskObject
	 * @param initialPDU
	 * @param callingConnection
	 * @return
	 * @throws Exception
	 */
	public Task createTask(ProtocolDataUnit initialPDU,
			Connection callingConnection) throws Exception {
		byte opcode = initialPDU.getBasicHeaderSegment().getOpCode().value();
		TaskDescriptor matchingDescriptor = getTaskDescriptor(opcode);
		if (matchingDescriptor != null) {
			if (matchingDescriptor.check(callingConnection, initialPDU)) {
				return matchingDescriptor.createTask();
			}
		}
		throw new Exception("Couldn't find a matching Task: Opcode = " + opcode);
	}
	
	public TaskDescriptor getTaskDescriptor(byte opcode){
		return availableTaskDescriptors.get(opcode);
	}

	/**
	 * Loads every TaskDescriptor the specified directory contains.
	 */
	public static Map<Byte, TaskDescriptor> loadAvailableTasks(
			File taskDirectory) {
		Map<Byte, TaskDescriptor> availableTasks = new ConcurrentHashMap<Byte, TaskDescriptor>();
		String className = null;
		Object loadedTaskDescriptor;
		boolean test;
		for (File possibleTaskDescriptor : taskDirectory.listFiles()) {
			test = false;
			if (possibleTaskDescriptor.isFile()) {
				className = possibleTaskDescriptor.getName();
				// try to load a java Object from the file
				try {
					loadedTaskDescriptor = Class.forName(className);
				} catch (Exception e) {
					logDebug("Loading TaskDescriptor failed: " + className);
					break;
				}
				// if File is valid Object, check if Object implement
				// TaskDescriptor
				for (Class<? extends AbstractTaskDescriptor> implementedInterfaces : loadedTaskDescriptor
						.getClass().getInterfaces()) {
					if (implementedInterfaces.getSimpleName().equals(
							TASK_DESCRIPTOR)) {
						byte opcode = ((TaskDescriptor) loadedTaskDescriptor)
								.getSuppotedOpcode().value();
						// add to availableTaskDescriptors, if not already
						// existing
						if (!availableTasks.containsKey(opcode)) {
							availableTasks.put(opcode,
									((TaskDescriptor) loadedTaskDescriptor));
							test = true;
						} else {
							logDebug("Tried to load a TaskDesccriptor with an already existing Operational Code: ExistingDescriptor: "
									+ availableTasks.get(opcode).getClass()
											.getSimpleName()
									+ " IdenticalDescriptor: "
									+ loadedTaskDescriptor.getClass()
											.getSimpleName());
						}
						break;
					}
				}
			}
			if (test) {
				logTrace("Succesfully loaded Task Descriptor: " + className);
			} else {
				logDebug("Couldn't load TaskDescriptor, file is no TaskDescriptor: "
						+ possibleTaskDescriptor.getAbsolutePath());
			}
		}
		return availableTasks;
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

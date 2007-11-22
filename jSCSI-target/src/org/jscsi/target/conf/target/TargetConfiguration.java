package org.jscsi.target.conf.target;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jscsi.target.Target;
import org.jscsi.target.task.TargetTaskRouter;

public class TargetConfiguration {
	
	/** The logger interface. */
	private static final Log LOGGER = LogFactory.getLog(TargetTaskRouter.class);
	
	private String targetName;
	
	private int targetPortalGroupTag;
	
	private final Set<Integer> listeningPorts;
	
	private final Set<File> taskDescriptorDirectories;
	
	public TargetConfiguration(){
		listeningPorts = new HashSet<Integer>();
		taskDescriptorDirectories = new HashSet<File>();
		loadTestParameter();
		logTrace("Initialized target configuration");
	}

	public String getTargetName() {
		return targetName;
	}
	
	public void addTaskDescriptorDirectory(String directory){
		addTaskDescriptorDirectory(new File(directory));
	}
	
	public void addTaskDescriptorDirectory(File directory){
		if(directory.isDirectory()){
			taskDescriptorDirectories.add(directory);
			logTrace("Added " + directory.getAbsolutePath() + " as TaskDescriptor containing directory");
		}else{
			logDebug("Wanted to add TaskDescriptor containing directory, no directory: " + directory.getAbsolutePath());
		}
	}
	

	public Set<File> getTaskDescriptorDirectories() {
		return taskDescriptorDirectories;
	}

	public void setTargetName(String targetName) {
		this.targetName = targetName;
	}

	public int getTargetPortalGroupTag() {
		return targetPortalGroupTag;
	}

	public void setTargetPortalGroupTag(int targetPortalGroupTag) {
		this.targetPortalGroupTag = targetPortalGroupTag;
	}
	
	public Set<Integer> getListeningPorts() {
		return listeningPorts;
	}

	public void configureTarget(Target target){
		target.setTargetName(getTargetName());
		target.setTargetPortalGroupTag(getTargetPortalGroupTag());
	}
	
	public void loadTestParameter(){
		targetName = "iqn.de.uni-konstanz.inf.disy.eames:test.testdrive";
		targetPortalGroupTag = 1;
		listeningPorts.add(3260);
		listeningPorts.add(3262);
		addTaskDescriptorDirectory("src/org/jscsi/target/task/standard/");
		logTrace("loaded test parameters, no config file is used");
	}
	
	/**
	 * Logs a trace Message, if trace log is enabled
	 * within the logging environment.
	 * 
	 * @param logMessage
	 */
	private void logTrace(String logMessage) {
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
	private void logDebug(String logMessage) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.trace(" Message: " + logMessage);
		}
	}
	
}

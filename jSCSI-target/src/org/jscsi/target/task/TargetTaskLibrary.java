package org.jscsi.target.task;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jscsi.target.task.abstracts.State;
import org.jscsi.target.task.abstracts.Task;
import org.jscsi.target.task.abstracts.TaskDescriptor;
import org.jscsi.target.util.Singleton;

public class TargetTaskLibrary {
	
	private static final Map<String, TaskDescriptor> loadedTaskDescriptors = new ConcurrentHashMap<String, TaskDescriptor>();
	
	private static final Map<String, Task> loadedTasks = new ConcurrentHashMap<String, Task>();
	
	private static final Map<String, State> loadedStates = new ConcurrentHashMap<String, State>();
	
	private TargetTaskLibrary(){
		
	}
	
	public final TaskDescriptor getTaskDescriptor(String name){
		return null;
		
	}
	
	public final Task getTask(String name){
		return null;
		
	}
	
	public final State getState(String name){
		return null;
		
	}
	
	public final void addSource(URL url){
		
	}
	
	public final void addSource(File file){
		
	}
	
	public final void addSource(byte[] code){
		
	}
	
	
	public static TargetTaskLibrary getInstance(){
		if(!Singleton.hasInstance(TargetTaskLibrary.class)){
			Singleton.setInstance(new TargetTaskLibrary());
		}
		TargetTaskLibrary instance = null;
		try {
			instance = Singleton.getInstance(TargetTaskLibrary.class);
		} catch (ClassNotFoundException e) {
			throw new Error("Couldn't load instance of " + TargetTaskLibrary.class);
		}
		return instance;
	}
	
}

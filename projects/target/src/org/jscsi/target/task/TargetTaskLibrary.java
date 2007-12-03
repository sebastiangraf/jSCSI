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
		return loadedTaskDescriptors.get(name);
		
	}
	
	public final Task getTask(String name){
		return loadedTasks.get(name);
		
	}
	
	public final State getState(String name){
		return loadedStates.get(name);
		
	}
	
	public final void addSource(URL url){
		
	}
	
	public final void addSource(File file){
		
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

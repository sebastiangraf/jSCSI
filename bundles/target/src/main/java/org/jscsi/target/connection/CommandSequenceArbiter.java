package org.jscsi.target.connection;

public class CommandSequenceArbiter {
	
	/** the maximum allowed Task within all Sessions TargetWide**/
	private int maximumAllowedTasks;
	
	/** the maximum allowed number of Tasks every Session can ask for **/
	private int maximumAllowedTasksPerSession;
	
	/** The real minimum will be zero, but this value decides if a new Session can be loaded**/
	private int minimumAllowedTasksPerSession;
	
	public CommandSequenceArbiter(){
		loadMinimumValues();
	}
	
	
	
	/**
	 * Will load mimimum parameters. 
	 * Which means the arbiter will only allow one Initiator
	 */
	private void loadMinimumValues(){
		maximumAllowedTasks = 2;
		maximumAllowedTasksPerSession = 2;
		minimumAllowedTasksPerSession = 2;
		
		
	}
	
}

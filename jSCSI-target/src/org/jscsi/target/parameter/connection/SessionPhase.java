package org.jscsi.target.parameter.connection;

public enum SessionPhase {
	
	
	/**
	 * 
	 */
	SecurityNegiotiationPhase(0),
	
	/**
	 * 
	 */
	LoginOpertionalPhase(1),
	
	/**
	 * 
	 */
	FullFeaturePhase(3),
	
	/**
	 * Unknown can be used to signal a target environment that the actual
	 * session type is unknown, i.e. at startup.
	 */
	Unknown(4);
	
	private final int currentPhase;
	
	private SessionPhase(int phase){
		currentPhase = phase;
	}
	
	public int value(){
		return currentPhase;
	}
	
}

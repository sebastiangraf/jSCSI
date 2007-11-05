package org.jscsi.target.parameter.connection;

public enum Phase {

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

	private Phase(int phase) {
		currentPhase = phase;
	}

	public int value() {
		return currentPhase;
	}

	public String stringValue() {
		switch (currentPhase) {
		case 0:
			return "SecurityNegotiationPhase";
		case 1:
			return "LoginOpertionalPhase";
		case 3:
			return "FullFeaturePhase";
		case 4:
			return "Unknown";
		}
		return "NotSupported";
	}

}

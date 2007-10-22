package org.jscsi.target.parameter.connection;

public enum SessionType {
	
	/**
	 * Unknown type, especially used in a target environment,
	 * if the actual type isn't yet negotiated.
	 */
	Unknown("Unkown"),
	
	/**
	 * Normal operational session - an unrestricted session.
	 */
	NormalOperationalSession("NormalOperationSession"),
	
	/**
	 * Discovery-session - a session only opened for target
	 * discovery.  The target MUST ONLY accept text requests with the
	 * SendTargets key and a logout request with the reason "close
	 * the session".  All other requests MUST be rejected.
	 */
	DiscoverySession("DiscoverySession");
	
	private final String sessionType;
	
	private SessionType(String type){
		sessionType = type;
	}
	
	public String getValue(){
		return sessionType;
	}
	
	

}

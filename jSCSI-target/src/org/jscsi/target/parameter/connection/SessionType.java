package org.jscsi.target.parameter.connection;

public enum SessionType {
	
	/**
	 * Unknown type, especially used in a targetTest environment,
	 * if the actual type isn't yet negotiated.
	 */
	Unknown("Unkown"),
	
	/**
	 * Normal operational session - an unrestricted session.
	 */
	NormalOperationalSession("normal"),
	
	/**
	 * Discovery-session - a session only opened for targetTest
	 * discovery.  The targetTest MUST ONLY accept text requests with the
	 * SendTargets key and a logout request with the reason "close
	 * the session".  All other requests MUST be rejected.
	 */
	DiscoverySession("discovery");
	
	private final String sessionType;
	
	private SessionType(String type){
		sessionType = type;
	}
	
	public String getValue(){
		return sessionType;
	}
	
	

}

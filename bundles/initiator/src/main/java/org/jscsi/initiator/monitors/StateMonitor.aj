package org.jscsi.initiator.monitors;

import org.jscsi.initiator.connection.state.IState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This aspect is used to keep
 * track of the states.
 * 
 * @author Andreas Rain
 *
 */
public aspect StateMonitor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger("StateMonitor");
	
	/**
	 * Point cutting the execute method of the IState.
	 * Looking at the state itself in the observations.
	 * @param s
	 */
	pointcut execute(IState s):
		call(* IState.execute()) && target(s);
	
	before(IState s) : execute(s){
		LOGGER.info("Executing state: " + s.toString());
	}
	
	after(IState s) : execute(s){
		LOGGER.info("Finished state execution of " + s.toString());
	}
}

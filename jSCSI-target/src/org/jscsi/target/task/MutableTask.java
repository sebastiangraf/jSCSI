package org.jscsi.target.task;

import org.jscsi.connection.SerialArithmeticNumber;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;

public interface MutableTask extends Task {
	
	/**
	 * Sets the current processing State.
	 * @param state current State
	 */
	public void setState(State state);
	
	/**
	 * Returns the current processing State
	 * @return current state
	 */
	public State getState();
	
	/**
	 * IF a PDU is received while waiting specified nanoSeconds,
	 * the PDU is returned.
	 * @param nanoWaitingTime waiting time in nanoseconds, 0 is equal infinity
	 * @return the receivedPDU or null, if no PDU arrived while waiting
	 */
	public ProtocolDataUnit receivePDU(long nanoWaitingTime);
	
	/**
	 * The Connection this Task has an allegiance to.
	 * @return
	 */
	public Connection getReferencedConnection();
	
	/**
	 * Sets the Tasks ITT, is only allowed once because an ITT never changes
	 * while a Task is running.
	 * @throws OperationException 
	 */
	public void setITT(SerialArithmeticNumber itt) throws OperationException;
	
	/**
	 * If the Task and State processing needs a location to store an
	 * identifying TargetTransferTag, here it is. The TTT is not
	 * used identifying a appropriate Task for incoming PDUs.
	 * @throws OperationException 
	 */
	public void setTTT(SerialArithmeticNumber ttt);
	
	
	
}

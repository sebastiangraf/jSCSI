package org.jscsi.target.task.abstracts;

import org.jscsi.connection.SerialArithmeticNumber;
import org.jscsi.parser.ProtocolDataUnit;


public interface Task{
	
	/**
	 * Assigns an appropriate PDU to this Task.
	 */
	public void assignPDU(ProtocolDataUnit pdu);
	
	/**
	 * Assigns an appropriate PDU to this Task and tells the Task
	 * to process it.
	 * @param pdu
	 */
	public void processPDU(ProtocolDataUnit pdu);
	
	/**
	 * Returns the Task's Initiator Transfer Tag.
	 * The Initiator Transfer Tag is unique within an I-T Nexus and is 
	 * used to identify appropriate existing Tasks.
	 * @return the Task's ITT.
	 */
	public SerialArithmeticNumber getITT();
	
	/**
	 * The TargetTransferTag can be used within a targetTest environment 
	 * to place information in a response PDU. The initiator guarantees to
	 * use the TTT for every following PDUs. 
	 * @return the task's TTT, if used.
	 */
	public SerialArithmeticNumber getTTT();
	
	/**
	 * Executes the Task, i.e. starts the initial State.
	 */
	public void execute();
	
	/**
	 * Tells the Task to process a receivedPDU.
	 */
	public void signalReceivedPDU();
	
}

package org.jscsi.target.task.abstracts;

import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;


public interface TaskDescriptor {

	public boolean check(Connection con, ProtocolDataUnit initialPDU);
	
	public Task createTask() throws OperationException;
	
	public OperationCode getSupportedOpcode();
	
	public Class<? extends AbstractTask> getReferencedTask();
	
	/**
	 * Compares one TaskDescriptor to another. Returns true
	 * if a Session could be in a state, both descriptors can be used.
	 * 
	 * @param taskDescriptor
	 * @return true if both TaskDescripors describe the same Task
	 */
	public boolean compare(AbstractTaskDescriptor taskDescriptor);
	
}

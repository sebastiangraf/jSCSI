package org.jscsi.target.task;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;


public interface TaskDescriptor {

	public boolean check(Connection con, ProtocolDataUnit initialPDU);
	
	public Task createTask() throws OperationException;
	
	public Class<? extends AbstractTask> getReferencedTask();
	
}

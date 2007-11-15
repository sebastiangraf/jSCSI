package org.jscsi.target.task.TaskAbstracts;

import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;


public interface TaskDescriptor {

	public boolean check(Connection con, ProtocolDataUnit initialPDU);
	
	public Task createTask() throws OperationException;
	
	public OperationCode getSupportedOpcode();
	
	public Class<? extends AbstractTask> getReferencedTask();
	
}

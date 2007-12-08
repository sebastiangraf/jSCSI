package org.jscsi.target.task.abstracts;

public interface State extends Operation {
	
	public Task getReferencedTask();
	
	public void define(MutableTask refTask) throws OperationException;

}

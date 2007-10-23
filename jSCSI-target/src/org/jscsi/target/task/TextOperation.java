package org.jscsi.target.task;

import org.jscsi.target.conf.OperationalTextKey;

public interface TextOperation {
	
	public boolean supportsKey(OperationalTextKey key);
	
	public void assignParameter(OperationalTextKey key) throws OperationException;
	
	public State getReferencedState();
	
}

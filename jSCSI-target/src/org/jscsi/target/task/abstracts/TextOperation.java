package org.jscsi.target.task.abstracts;

import org.jscsi.target.conf.operationalText.OperationalTextKey;

public interface TextOperation {
	
	public boolean supportsKey(OperationalTextKey key);
	
	public void assignParameter(OperationalTextKey key) throws OperationException;
	
	public State getReferencedState();
	
}

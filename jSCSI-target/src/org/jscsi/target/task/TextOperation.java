package org.jscsi.target.task;

import java.util.List;

import org.jscsi.target.conf.OperationalTextKey;

public interface TextOperation {
	
	public List<OperationalTextKey> getSupportedKeys();
	
	public void assignParameter(OperationalTextKey key);	
	
}

package org.jscsi.target.task;

import org.jscsi.target.conf.OperationalTextKey;


public interface TextOperationDescriptor {
	
	public boolean supports(OperationalTextKey key);
	
	public TextOperation createTextOperation() throws OperationException;
	
	public Class<? extends AbstractTextOperation> getReferencedTextOperation();
	
}

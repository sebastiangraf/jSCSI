package org.jscsi.target.task.abstracts;

import org.jscsi.target.conf.operationalText.OperationalTextKey;


public interface TextOperationDescriptor {
	
	public boolean supports(OperationalTextKey key);
	
	public TextOperation createTextOperation() throws OperationException;
	
	public Class<? extends AbstractTextOperation> getReferencedTextOperation();
	
}

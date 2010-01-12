package org.jscsi.target.task.abstracts;

import java.util.HashSet;
import java.util.Set;

import org.jscsi.target.conf.operationalText.OperationalTextKey;


public class AbstractTextOperationDescriptor implements TextOperationDescriptor{

	/** the described task */
	private final Class<? extends AbstractTextOperation> refTextOperation;
	
	/** every supported text parameter, i.e. OperationalTextKeys */
	private final Set<String> supportedKeys;
	
	public AbstractTextOperationDescriptor(Class<? extends AbstractTextOperation> refTextOperation, Set<String> supportedKeys){
		this.refTextOperation = refTextOperation;
		this.supportedKeys = supportedKeys;
	}
	
	public AbstractTextOperationDescriptor(Class<? extends AbstractTextOperation> refTextOperation, String supportedKey){
		this.refTextOperation = refTextOperation;
		this.supportedKeys = new HashSet<String>();
		this.supportedKeys.add(supportedKey);
	}
	
	
	
	public TextOperation createTextOperation() throws OperationException {
		String className = refTextOperation.getName();
		TextOperation textOperation = null;
		try {
			textOperation = (TextOperation) Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new OperationException("Couldn't find referenced Text Operation: " + refTextOperation.getName());
		}
		return textOperation;
	}

	public Class<? extends AbstractTextOperation> getReferencedTextOperation() {
		return refTextOperation;
	}

	public boolean supports(OperationalTextKey key) {
		if(supportedKeys.contains(key)){
			return true;
		}
		return false;
	}


}

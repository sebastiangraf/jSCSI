package org.jscsi.target.task.abstracts;

import org.jscsi.target.task.TargetTaskLibrary;
import org.jscsi.target.util.Singleton;

/**
 * A State represents one processing instruction set,
 * that will end the initiator's command request successfully or not,
 * if an unrecoverable error occurred.  
 * @author Marcus Specht
 */
public abstract class AbstractState extends AbstractOperation implements State, Runnable{
	
	
	/** the State's referenced Task */
	private Task refTask;
	
	private boolean defined;
	
	public AbstractState(){
		defined = false;
	}
	
	public void define(Task refTask) throws OperationException{
		if(!defined){
			this.refTask = refTask;
			defined = true;
		}else{
			throw new OperationException("State is already defined!");
		}
	}
	
	/*public void define(String refTaskClassName) throws OperationException{
		if(!defined){
			try {
				TargetTaskLibrary library = Singleton.getInstance(TargetTaskLibrary.class);
				this.refTask = library.createTask(refTaskClassName);
			} catch (ClassNotFoundException e) {
				throw new OperationException("Couldn't load referenced Task");
			}
			defined = true;
		}else{
			throw new OperationException("State is already defined!");
		}
	}*/
	
	/** Constructor with referenced Task */
	public AbstractState(Task refTask){
		this.refTask = refTask;
	}
	
	/**
	 * Returns the referenced Task
	 * @return referenced Task
	 */
	public Task getReferencedTask() {
		return refTask;
	}



}

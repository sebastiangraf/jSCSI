package org.jscsi.target.task.abstracts;

/**
 * A State represents one processing instruction set,
 * that will end the initiator's command request successfully or not,
 * if an unrecoverable error occurred.  
 * @author Marcus Specht
 */
public abstract class AbstractState extends AbstractOperation implements State, Runnable{
	
	
	/** the State's referenced Task */
	private MutableTask refTask;
	
	private boolean defined;
	
	public AbstractState(){
		defined = false;
	}
	
	public void define(MutableTask refTask) throws OperationException{
		synchronized(refTask){
			if(!defined){
				this.refTask = refTask;
				defined = true;
			}else{
				throw new OperationException("State is already defined!");
			}
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
	
	/** Constructor with referenced Task *//*
	public AbstractState(Task refTask){
		this.refTask = refTask;
	}*/
	
	/**
	 * Returns the referenced Task
	 * @return referenced Task
	 */
	public MutableTask getReferencedTask() {
		return refTask;
	}


	
	
	



}

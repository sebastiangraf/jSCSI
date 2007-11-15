package org.jscsi.target.task.TaskAbstracts;

import java.util.HashSet;
import java.util.Set;

import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.parameter.connection.Phase;
import org.jscsi.target.parameter.connection.SessionType;

/**
 * A task descriptor is used by an iSCSI target environment to
 * create an appropriate task, analyzing an initializing PDU. 
 * This class is only representing an abstract TaskDescriptor.
 * A working descriptor needs to extend this one,
 * loading this constructor with well chosen parameters and place it in a directory
 * the target environment is using to load task descriptors.
 * @author Marcus Specht
 *
 */
public abstract class AbstractTaskDescriptor implements TaskDescriptor{
	
	
	/** the described task */
	private final Class<? extends AbstractTask> refTask;
	
	/** the valid OperationCode */
	private final OperationCode opcode;
	
	/** all allowed SessionTypes */
	private final Set<SessionType> allowedSessionTypes;
	
	/** all allowed SessionPhases */
	private final Set<Phase> allowedSessionPhases;
	
	public AbstractTaskDescriptor(OperationCode opcode, SessionType type, Phase phase, Class<? extends AbstractTask> refTask) throws OperationException{
		this.opcode = opcode;
		allowedSessionTypes = new HashSet<SessionType>();
		allowedSessionPhases = new HashSet<Phase>();
		allowedSessionTypes.add(type);
		allowedSessionPhases.add(phase);
		this.refTask = refTask;
		try {
			Class.forName(refTask.getName());
		} catch (ClassNotFoundException e) {
			throw new OperationException("Couldn't find the referenced Task: " + refTask.getName());
		}
		
	}
	
	public AbstractTaskDescriptor(OperationCode opcode, Set<SessionType> types, Phase phase, Class<? extends AbstractTask> refTask) throws OperationException{
		this.opcode = opcode;
		allowedSessionPhases = new HashSet<Phase>();
		allowedSessionTypes = types;
		allowedSessionPhases.add(phase);
		this.refTask = refTask;
		try {
			Class.forName(refTask.getName());
		} catch (ClassNotFoundException e) {
			throw new OperationException("Couldn't find the referenced Task: " + refTask.getName());
		}
	}
	
	public AbstractTaskDescriptor(OperationCode opcode, SessionType type, Set<Phase> phases, Class<? extends AbstractTask> refTask) throws OperationException{
		this.opcode = opcode;
		allowedSessionTypes = new HashSet<SessionType>();
		allowedSessionPhases = phases;
		allowedSessionTypes.add(type);
		this.refTask = refTask;
		try {
			Class.forName(refTask.getName());
		} catch (ClassNotFoundException e) {
			throw new OperationException("Couldn't find the referenced Task: " + refTask.getName());
		}
	}
	
	public AbstractTaskDescriptor(OperationCode opcode, Set<SessionType> types, Set<Phase> phases, Class<? extends AbstractTask> refTask) throws OperationException{
		this.opcode = opcode;
		this.allowedSessionTypes = types;
		this.allowedSessionPhases = phases;
		this.refTask = refTask;
		try {
			Class.forName(refTask.getName());
		} catch (ClassNotFoundException e) {
			throw new OperationException("Couldn't find the referenced Task: " + refTask.getName());
		}
	}
	
	/**
	 * Checks if the Connection/Session is allowed to create a Task,
	 * appropriate to the initial PDU.
	 * @param con the Connection that wants load the Task
	 * @param initialPDU the checked PDU
	 */
	public boolean check(Connection con, ProtocolDataUnit initialPDU) {
		
		if(!allowedSessionTypes.contains(con.getPhase())){
			return false;
		}
		
		if(!allowedSessionPhases.contains(con.getPhase())){
			return false;
		}
		
		if(initialPDU.getBasicHeaderSegment().getOpCode().compareTo(opcode) != 0){
			return false;
		}
		return true;
	}
	
	/**
	 * Returns a new task Instance
	 *@return the new Task Instance
	 * @throws OperationException 
	 */
	public Task createTask() throws OperationException {
		String className = refTask.getName();
		Task task = null;
		try {
			task = (Task) Class.forName(className).newInstance();
		} catch (Exception e) {
			throw new OperationException("Couldn't find referenced Task Operation: " + refTask.getName());
		}
		return task;
	}
	
	/**
	 * Returns the Task.class which is described by this TaskDescriptor.
	 * @return described Task.class
	 */
	public Class<? extends AbstractTask> getReferencedTask(){
		return refTask;
	}
	
	
	/**
	 * Compares one TaskDescriptor to another. Returns true
	 * if a Session could be in a state, both descriptors can be used.
	 * 
	 * @param taskDescriptor
	 * @return true if both TaskDescripors describe the same Task
	 */
	public boolean compare(AbstractTaskDescriptor taskDescriptor){
		
		if(taskDescriptor.opcode.compareTo(opcode) == 0){
			//if both descriptors support the same Opcode,
			//there must be a different supported phase or type, else
			//they both can support an incoming PDU at a specific state the Session can have
			for(Phase phase : taskDescriptor.allowedSessionPhases){
				if(allowedSessionPhases.contains(phase)){
					return true;
				}
			}
			for(SessionType type : taskDescriptor.allowedSessionTypes){
				if(allowedSessionTypes.contains(type)){
					return true;
				}
			}
		}	
		return false;
	}

	public OperationCode getSupportedOpcode() {
		// TODO Auto-generated method stub
		return opcode;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((allowedSessionPhases == null) ? 0 : allowedSessionPhases
						.hashCode());
		result = prime
				* result
				+ ((allowedSessionTypes == null) ? 0 : allowedSessionTypes
						.hashCode());
		result = prime * result + ((opcode == null) ? 0 : opcode.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final AbstractTaskDescriptor other = (AbstractTaskDescriptor) obj;
		if (allowedSessionPhases == null) {
			if (other.allowedSessionPhases != null)
				return false;
		} else if (!allowedSessionPhases.equals(other.allowedSessionPhases))
			return false;
		if (allowedSessionTypes == null) {
			if (other.allowedSessionTypes != null)
				return false;
		} else if (!allowedSessionTypes.equals(other.allowedSessionTypes))
			return false;
		if (opcode == null) {
			if (other.opcode != null)
				return false;
		} else if (!opcode.equals(other.opcode))
			return false;
		return true;
	}

	
	
}

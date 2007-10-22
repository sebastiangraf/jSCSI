package org.jscsi.target.task;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.parameter.connection.SessionPhase;
import org.jscsi.target.parameter.connection.SessionType;
import org.jscsi.target.parameter.pdu.Opcode;

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
	private final Class refTask;
	
	/** the valid OperationCode */
	private final OperationCode opcode;
	
	/** all allowed SessionTypes */
	private final Set<SessionType> allowedSessionTypes;
	
	/** all allowed SessionPhases */
	private final Set<SessionPhase> allowedSessionPhases;
	
	public AbstractTaskDescriptor(OperationCode opcode, SessionType type, SessionPhase phase, Class refTask) throws OperationException{
		this.opcode = opcode;
		allowedSessionTypes = new HashSet<SessionType>();
		allowedSessionPhases = new HashSet<SessionPhase>();
		allowedSessionTypes.add(type);
		allowedSessionPhases.add(phase);
		this.refTask = refTask;
		try {
			Class.forName(refTask.getName());
		} catch (ClassNotFoundException e) {
			throw new OperationException("Couldn't find the referenced Task: " + refTask.getName());
		}
		
	}
	
	public AbstractTaskDescriptor(OperationCode opcode, Set<SessionType> types, SessionPhase phase, Class refTask) throws OperationException{
		this.opcode = opcode;
		allowedSessionPhases = new HashSet<SessionPhase>();
		allowedSessionTypes = types;
		allowedSessionPhases.add(phase);
		this.refTask = refTask;
		try {
			Class.forName(refTask.getName());
		} catch (ClassNotFoundException e) {
			throw new OperationException("Couldn't find the referenced Task: " + refTask.getName());
		}
	}
	
	public AbstractTaskDescriptor(OperationCode opcode, SessionType type, Set<SessionPhase> phases, Class refTask) throws OperationException{
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
	
	public AbstractTaskDescriptor(OperationCode opcode, Set<SessionType> types, Set<SessionPhase> phases, Class refTask) throws OperationException{
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
		
		if(!allowedSessionTypes.contains(con.getReferencedSession().getSessionType())){
			return false;
		}
		
		if(!allowedSessionPhases.contains(con.getReferencedSession().getSessionPhase())){
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
	 */
	public Task createTask() {
		String className = refTask.getName();
		Task task = null;
		try {
			task = (Task) Class.forName(className).newInstance();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return task;
	}
	
	/**
	 * Returns the Task.class which is described by this TaskDescriptor.
	 * @return described Task.class
	 */
	public Class getReferencedTask(){
		return refTask;
	}
	
	
	/**
	 * Compares the TaskDescriptor
	 * @param taskDescriptor
	 * @return true if both TaskDescripors describe the same Task
	 */
	public boolean compare(AbstractTaskDescriptor taskDescriptor){
		if(taskDescriptor.opcode.compareTo(opcode) != 0){
			return false;
		}
		
		if(!taskDescriptor.allowedSessionPhases.containsAll(allowedSessionPhases)){
			return false;
		}
		if(!taskDescriptor.allowedSessionTypes.containsAll(allowedSessionTypes)){
			return false;
		}
		return true;
	}
	
	
}

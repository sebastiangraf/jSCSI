package org.jscsi.target.util.tempClassLoaderTestFiles;

import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.parameter.connection.Phase;
import org.jscsi.target.parameter.connection.SessionType;
import org.jscsi.target.task.abstracts.AbstractTask;
import org.jscsi.target.task.abstracts.AbstractTaskDescriptor;
import org.jscsi.target.task.abstracts.OperationException;

public class LoginRequestTaskDescriptor extends AbstractTaskDescriptor {
	
	public LoginRequestTaskDescriptor() throws OperationException{
		this(OperationCode.LOGIN_REQUEST, SessionType.NormalOperationalSession, Phase.LoginOpertionalPhase, LoginRequestTask.class);
	}
	
	public LoginRequestTaskDescriptor(OperationCode opcode, SessionType type,
			Phase phase, Class<? extends AbstractTask> refTask)
			throws OperationException {
		super(OperationCode.LOGIN_REQUEST, SessionType.NormalOperationalSession, Phase.LoginOpertionalPhase, LoginRequestTask.class);
	}

	public class LoginRequestTask extends AbstractTask{


		
		public LoginRequestTask(Connection refConnection,
				ProtocolDataUnit initialPDU) {
			super(refConnection, initialPDU);
			setState(new LeadingLoginRequestState(this));
		}

		public void execute() {
			
			
		}

	}
	
}

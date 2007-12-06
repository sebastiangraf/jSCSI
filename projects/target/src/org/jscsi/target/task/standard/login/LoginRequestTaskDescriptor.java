package org.jscsi.target.task.standard.login;

import java.util.HashSet;

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
		super();
		define(OperationCode.LOGIN_REQUEST,SessionType.NormalOperationalSession, Phase.LoginOpertionalPhase, LoginRequestTask.class);
	}
	

	
	
}

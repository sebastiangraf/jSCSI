package org.jscsi.target.task.standard.login;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.task.abstracts.AbstractTask;

public class LoginRequestTask extends AbstractTask{


	
	public LoginRequestTask() {
		super();
		// TODO Auto-generated constructor stub
	}

	public LoginRequestTask(Connection refConnection,
			ProtocolDataUnit initialPDU) {
		super(refConnection, initialPDU);
		setState(new LeadingLoginRequestState(this));
	}

	public void execute() {
		
		
	}

}

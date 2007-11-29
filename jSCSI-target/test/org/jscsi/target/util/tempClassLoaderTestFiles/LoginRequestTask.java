package org.jscsi.target.util.tempClassLoaderTestFiles;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.task.abstracts.AbstractTask;

public class LoginRequestTask extends AbstractTask{


	
	public LoginRequestTask(Connection refConnection,
			ProtocolDataUnit initialPDU) {
		super(refConnection, initialPDU);
		setState(new LeadingLoginRequestState(this));
	}

	public void execute() {
		
		
	}

}

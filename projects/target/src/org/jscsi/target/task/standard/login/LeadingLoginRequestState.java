package org.jscsi.target.task.standard.login;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.task.abstracts.AbstractState;

public class LeadingLoginRequestState extends AbstractState {

	public LeadingLoginRequestState() {
		super();
	}

	@Override
	public void run() {
		while (!suspended()) {
			ProtocolDataUnit newPDU = getReferencedTask().receivePDU(0);
			//process PDU

		}

		while (suspended()) {
			awaitRestart(0);
		}
	}

}

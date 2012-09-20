/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jscsi.initiator.monitors;

import org.jscsi.initiator.connection.SenderWorker;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.data.DataOutParser;
import org.jscsi.parser.login.LoginRequestParser;
import org.jscsi.parser.login.LoginResponseParser;
import org.jscsi.parser.logout.LogoutRequestParser;
import org.jscsi.parser.logout.LogoutResponseParser;
import org.jscsi.parser.nop.NOPOutParser;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.snack.SNACKRequestParser;
import org.jscsi.parser.text.TextRequestParser;
import org.jscsi.parser.tmf.TaskManagementFunctionRequestParser;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This aspect is used to monitor the pdu's while
 * being sent to the target.
 * 
 * @author Andreas Rain
 *
 */
public aspect SenderWorkerMonitor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger("SenderWorkerMonitor");
	
	/**
	 * Point cutting the recieve method on the senderworker.
	 * @param s
	 */
	pointcut recieve(SenderWorker s) :
		call(* SenderWorker.receiveFromWire()) && target(s);

	/**
	 * Point cutting the send method on the SenderWorker to look at the PDU.
	 * @param s
	 */
	pointcut send(SenderWorker s, ProtocolDataUnit p) :
		call(* SenderWorker.sendOverWire(ProtocolDataUnit)) && target(s) && args(p);
	
	before(SenderWorker s) : recieve(s){
		LOGGER.info("Preparing to recieve from wire.");
	}
	
	before(SenderWorker s, ProtocolDataUnit p) : send(s, p){
		LOGGER.info("Sending over wire PDU: " + p);

		/**
		 * Assertions on read in PDUs This can only happen when a request went
		 * out so only request opcodes are checked.
		 */
		switch (p.getBasicHeaderSegment().getOpCode()) {
		case NOP_OUT:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), NOPOutParser.class);
			break;
		case SCSI_COMMAND:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), SCSICommandParser.class);
			break;
		case SCSI_TM_REQUEST:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), TaskManagementFunctionRequestParser.class);
			break;
		case LOGIN_REQUEST:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), LoginRequestParser.class);
			break;
		case TEXT_REQUEST:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), TextRequestParser.class);
			break;
		case SCSI_DATA_OUT:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), DataOutParser.class);
			break;
		case LOGOUT_REQUEST:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), LogoutRequestParser.class);
			break;
		case SNACK_REQUEST:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), SNACKRequestParser.class);
			break;
		default:
			break;
		}
		
	}
}

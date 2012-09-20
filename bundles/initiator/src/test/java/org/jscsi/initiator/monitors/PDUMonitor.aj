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

import java.nio.channels.SocketChannel;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.asynchronous.AsynchronousMessageParser;
import org.jscsi.parser.data.DataInParser;
import org.jscsi.parser.data.DataOutParser;
import org.jscsi.parser.login.LoginRequestParser;
import org.jscsi.parser.login.LoginResponseParser;
import org.jscsi.parser.logout.LogoutResponseParser;
import org.jscsi.parser.nop.NOPOutParser;
import org.jscsi.parser.r2t.Ready2TransferParser;
import org.jscsi.parser.reject.RejectParser;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.scsi.SCSIResponseParser;
import org.jscsi.parser.snack.SNACKRequestParser;
import org.jscsi.parser.text.TextRequestParser;
import org.jscsi.parser.text.TextResponseParser;
import org.jscsi.parser.tmf.TaskManagementFunctionRequestParser;
import org.jscsi.parser.tmf.TaskManagementFunctionResponseParser;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This aspect is used to monitor pdu's when they are written and read.
 * 
 * @author Andreas Rain
 * 
 */
public aspect PDUMonitor {

	private static final Logger LOGGER = LoggerFactory.getLogger("PDUMonitor");

	/**
	 * Point cutting the read function on PDUs to have a closer look at reading
	 * PDUs.
	 * 
	 * @param p
	 * @param c
	 */
	pointcut read(ProtocolDataUnit p, SocketChannel c) :
		call(* ProtocolDataUnit.read(SocketChannel)) && target(p) && args(c);

	/**
	 * Point cutting the send function on PDUs to have a closer look at reading
	 * PDUs.
	 * 
	 * @param p
	 * @param c
	 */
	pointcut write(ProtocolDataUnit p, SocketChannel c) :
		call(* ProtocolDataUnit.write(SocketChannel)) && target(p) && args(c);

	before(ProtocolDataUnit p, SocketChannel c) : read(p, c){
		LOGGER.debug("Reading PDU.");
	}

	after(ProtocolDataUnit p, SocketChannel c) : read(p, c){
		LOGGER.info("Read PDU: " + p);

		/**
		 * Assertions on read in PDUs This can only happen when a request went
		 * out so only request opcodes are checked.
		 */
		switch (p.getBasicHeaderSegment().getOpCode()) {
		case NOP_IN:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), NOPOutParser.class);
			break;
		case SCSI_RESPONSE:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), SCSIResponseParser.class);
			break;
		case SCSI_TM_RESPONSE:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), TaskManagementFunctionResponseParser.class);
			break;
		case LOGIN_RESPONSE:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), LoginResponseParser.class);
			break;
		case TEXT_RESPONSE:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), TextResponseParser.class);
			break;
		case SCSI_DATA_IN:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), DataInParser.class);
			break;
		case LOGOUT_RESPONSE:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), LogoutResponseParser.class);
			break;
		case R2T:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), Ready2TransferParser.class);
			break;
		case ASYNC_MESSAGE:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), AsynchronousMessageParser.class);
			break;
		case REJECT:
			// Checking if the parser is correct.
			Assert.assertEquals(p.getBasicHeaderSegment().getParser()
					.getClass(), RejectParser.class);
			break;
		default:
			break;
		}
	}

	before(ProtocolDataUnit p, SocketChannel c) : write(p, c){
		LOGGER.debug("Writing to PDU: " + p);
	}

}

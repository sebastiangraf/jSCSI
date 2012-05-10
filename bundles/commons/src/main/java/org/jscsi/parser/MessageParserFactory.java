/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
package org.jscsi.parser;

import java.util.NoSuchElementException;

import org.jscsi.parser.data.DataInParser;
import org.jscsi.parser.data.DataOutParser;
import org.jscsi.parser.login.LoginRequestParser;
import org.jscsi.parser.login.LoginResponseParser;
import org.jscsi.parser.logout.LogoutRequestParser;
import org.jscsi.parser.logout.LogoutResponseParser;
import org.jscsi.parser.nop.NOPInParser;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>MessageParserFactory</h1>
 * <p>
 * This class creates a specified parser object. But only <b>one</b> object of this kind. With this technique
 * it minimizes the object creation process and affect the execution speed, too.
 * 
 * @author Volker Wildi, University of Konstanz
 */
public final class MessageParserFactory {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Logger Interface. */
    private static final Logger LOGGER = LoggerFactory.getLogger(MessageParserFactory.class);

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Default constructor, which prevent a instance of this class. */
    private MessageParserFactory() {

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the parser object with the given operation code.
     * 
     * @param protocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>AbstractMessageParser</code> object.
     * @param operationCode
     *            The operation code of the requested <code>AbstractMessageParser</code>.
     * @return The instance of the requested <code>AbstractMessageParser</code>.
     * @see org.jscsi.parser.OperationCode
     */
    public static final AbstractMessageParser getParser(final ProtocolDataUnit protocolDataUnit,
        final OperationCode operationCode) {

        return createParser(protocolDataUnit, operationCode);
    }

    /**
     * Creates an instance of a concrete <code>AbstractMessageParser</code> object.
     * 
     * @param protocolDataUnit
     *            The reference <code>ProtocolDataUnit</code> instance, which
     *            contains this <code>AbstractMessageParser</code> object.
     * @param operationCode
     *            The operation code of the requested <code>AbstractMessageParser</code>.
     * @return The instance of the requested <code>AbstractMessageParser</code>.
     * @see org.jscsi.parser.OperationCode
     */
    private static final AbstractMessageParser createParser(final ProtocolDataUnit protocolDataUnit,
        final OperationCode operationCode) {

        switch (operationCode) {
        case LOGIN_REQUEST:
            return new LoginRequestParser(protocolDataUnit);
        case LOGIN_RESPONSE:
            return new LoginResponseParser(protocolDataUnit);
        case LOGOUT_REQUEST:
            return new LogoutRequestParser(protocolDataUnit);
        case LOGOUT_RESPONSE:
            return new LogoutResponseParser(protocolDataUnit);
        case TEXT_REQUEST:
            return new TextRequestParser(protocolDataUnit);
        case TEXT_RESPONSE:
            return new TextResponseParser(protocolDataUnit);
        case SCSI_DATA_OUT:
            return new DataOutParser(protocolDataUnit);
        case SCSI_DATA_IN:
            return new DataInParser(protocolDataUnit);
        case NOP_OUT:
            return new NOPOutParser(protocolDataUnit);
        case NOP_IN:
            return new NOPInParser(protocolDataUnit);
        case R2T:
            return new Ready2TransferParser(protocolDataUnit);
        case REJECT:
            return new RejectParser(protocolDataUnit);
        case SNACK_REQUEST:
            return new SNACKRequestParser(protocolDataUnit);
        case SCSI_TM_REQUEST:
            return new TaskManagementFunctionRequestParser(protocolDataUnit);
        case SCSI_TM_RESPONSE:
            return new TaskManagementFunctionResponseParser(protocolDataUnit);
        case SCSI_COMMAND:
            return new SCSICommandParser(protocolDataUnit);
        case SCSI_RESPONSE:
            return new SCSIResponseParser(protocolDataUnit);
        default:
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Parser not supported with this operation code " + operationCode);
            }
            throw new NoSuchElementException();
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

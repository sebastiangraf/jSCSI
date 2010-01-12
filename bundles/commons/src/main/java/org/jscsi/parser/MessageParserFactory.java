/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * MessageParserFactory.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.parser;

import java.util.NoSuchElementException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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

/**
 * <h1>MessageParserFactory</h1>
 * <p>
 * This class creates a specified parser object. But only <b>one</b> object of
 * this kind. With this technique it minimizes the object creation process and
 * affect the execution speed, too.
 * 
 * @author Volker Wildi
 */
public final class MessageParserFactory {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Logger Interface. */
  private static final Log LOGGER = LogFactory
      .getLog(MessageParserFactory.class);

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
   *          The reference <code>ProtocolDataUnit</code> instance, which
   *          contains this <code>AbstractMessageParser</code> object.
   * @param operationCode
   *          The operation code of the requested
   *          <code>AbstractMessageParser</code>.
   * @return The instance of the requested <code>AbstractMessageParser</code>.
   * @see OperatorCode
   */
  public static final AbstractMessageParser getParser(
      final ProtocolDataUnit protocolDataUnit, final OperationCode operationCode) {

    return createParser(protocolDataUnit, operationCode);
  }

  /**
   * Creates an instance of a concrete <code>AbstractMessageParser</code>
   * object.
   * 
   * @param protocolDataUnit
   *          The reference <code>ProtocolDataUnit</code> instance, which
   *          contains this <code>AbstractMessageParser</code> object.
   * @param operationCode
   *          The operation code of the requested
   *          <code>AbstractMessageParser</code>.
   * @return The instance of the requested <code>AbstractMessageParser</code>.
   * @see OperationCode
   */
  private static final AbstractMessageParser createParser(
      final ProtocolDataUnit protocolDataUnit, final OperationCode operationCode) {

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
          LOGGER.error("Parser not supported with this operation code "
              + operationCode);
        }
        throw new NoSuchElementException();
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

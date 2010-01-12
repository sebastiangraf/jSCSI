/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * LoginRequestState.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.initiator.connection.state;

import java.util.LinkedList;
import java.util.Queue;

import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory;
import org.jscsi.parser.datasegment.IDataSegment;
import org.jscsi.parser.datasegment.IDataSegmentIterator;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.SettingsMap;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.datasegment.IDataSegmentIterator.IDataSegmentChunk;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.login.ISID;
import org.jscsi.parser.login.LoginRequestParser;
import org.jscsi.parser.login.LoginStage;

/**
 * <h1>LoginRequestState</h1> <p/> This state handles a Login Request.
 * 
 * @author Volker Wildi
 */
public final class LoginRequestState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * The maximum version number, which is supported by this iSCSI Implementation
   * (RFC3720).
   */
  private static final byte MAXIMUM_VERSION = 0x00;

  /**
   * The minimum version number, which is supported by this iSCSI Implementation
   * (RFC3720).
   */
  private static final byte MINIMUM_VERSION = 0x00;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The Next Stage of the login process. */
  private final LoginStage nextStage;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a <code>LoginRequestState</code> instance, which uses
   * the given connection for transmission.
   * 
   * @param initConnection
   *          The context connection, which is used for the network
   *          transmission.
   * @param initNextStage
   *          The next stage to which should transfered to.
   */
  public LoginRequestState(final Connection initConnection,
      final LoginStage initNextStage) {

    super(initConnection);
    nextStage = initNextStage;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final void execute() throws InternetSCSIException {

    final SettingsMap loginParameters = connection.getSettings();
    LOGGER.info("Sending these login parameters:\n" + loginParameters);

    final int maxRecvDataSegmentLength = connection
        .getSettingAsInt(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH);
    final ISID isid = ISID.createRandom(System.currentTimeMillis());

    LoginRequestParser loginRequest;
    boolean continueFlag;
    // here the finalFlag represents the transitFlag
    boolean finalFlag;
    final IDataSegment dataSegment = DataSegmentFactory.create(loginParameters
        .asByteBuffer(), DataSegmentFormat.TEXT, maxRecvDataSegmentLength);
    final IDataSegmentIterator iterator = dataSegment.iterator();
    final Queue<ProtocolDataUnit> protocolDataUnits = new LinkedList<ProtocolDataUnit>();

    IDataSegmentChunk dataSegmentChunk;
    ProtocolDataUnit protocolDataUnit;
    int bytes2Transfer = dataSegment.getLength();

    while (bytes2Transfer > 0 && iterator.hasNext()) {

      if (bytes2Transfer <= maxRecvDataSegmentLength) {
        // last PDU to send...
        dataSegmentChunk = iterator.next(bytes2Transfer);
        continueFlag = false;
      } else {
        dataSegmentChunk = iterator.next(maxRecvDataSegmentLength);
        continueFlag = true;
      }

      finalFlag = !continueFlag;
      protocolDataUnit = protocolDataUnitFactory.create(true, finalFlag,
          OperationCode.LOGIN_REQUEST, "None", "None");
      loginRequest = (LoginRequestParser) protocolDataUnit
          .getBasicHeaderSegment().getParser();

      loginRequest.setContinueFlag(continueFlag);
      loginRequest.setCurrentStageNumber(connection.getSession().getPhase());
      LOGGER.debug("Phase:\n" + loginRequest.getCurrentStageNumber());
      if (finalFlag) {
        loginRequest.setNextStageNumber(nextStage);
      }
      loginRequest.setMaxVersion(MAXIMUM_VERSION);
      loginRequest.setMinVersion(MINIMUM_VERSION);

      loginRequest.setInitiatorSessionID(isid);
      loginRequest.setTargetSessionIdentifyingHandle(connection.getSession()
          .getTargetSessionIdentifyingHandle());

      protocolDataUnit.setDataSegment(dataSegmentChunk);

      protocolDataUnits.offer(protocolDataUnit);
      bytes2Transfer -= maxRecvDataSegmentLength;
    }

    connection.send(protocolDataUnits);
    connection.nextState(new LoginResponseState(connection, nextStage));
    super.stateFollowing = true;
//    return true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
}

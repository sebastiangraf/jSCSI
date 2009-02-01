/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * LoginResponseState.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.initiator.connection.state;

import org.jscsi.initiator.connection.Connection;
import org.jscsi.initiator.connection.phase.FullFeaturePhase;
import org.jscsi.initiator.connection.phase.LoginOperationalNegotiationPhase;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory;
import org.jscsi.parser.datasegment.IDataSegment;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.login.LoginResponseParser;
import org.jscsi.parser.login.LoginStage;

/**
 * <h1>LoginResponseState</h1> <p/> This state handles a Login Response.
 * 
 * @author Volker Wildi
 */
public final class LoginResponseState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * The Next Stage of the login process, that the iSCSI Target wants to transit
   * to.
   */
  private final LoginStage nextStage;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a <code>LoginResponseState</code> instance, which
   * uses the given connection for transmission.
   * 
   * @param initConnection
   *          The context connection, which is used for the network
   *          transmission.
   * @param initNextStage
   *          The next stage to which should transfered to.
   */
  public LoginResponseState(final Connection initConnection,
      final LoginStage initNextStage) {

    super(initConnection);
    nextStage = initNextStage;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final void execute() throws InternetSCSIException {

    ProtocolDataUnit protocolDataUnit;
    final IDataSegment loginResponse = DataSegmentFactory.create(
        DataSegmentFormat.TEXT, connection
            .getSettingAsInt(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH));

    do {
      protocolDataUnit = connection.receive();

      if (!(protocolDataUnit.getBasicHeaderSegment().getParser() instanceof LoginResponseParser)) {
        break;
      }

      loginResponse.append(protocolDataUnit.getDataSegment(), protocolDataUnit
          .getBasicHeaderSegment().getDataSegmentLength());
    } while (!protocolDataUnit.getBasicHeaderSegment().isFinalFlag());
    // extract Target Session Handle Identifying Handle
    final LoginResponseParser parser = (LoginResponseParser) protocolDataUnit
        .getBasicHeaderSegment().getParser();
    connection.getSession().setTargetSessionIdentifyingHandle(
        parser.getTargetSessionIdentifyingHandle());
    // Set the Expected Status Sequence Number to the initial value received
    // from the target. Then add the constant 2 to this value, because the
    // incrementExpectedStatusSequence() method was already invoked.
    connection
        .setExpectedStatusSequenceNumber(parser.getStatusSequenceNumber() + 2);

    // check parameters....
      LOGGER.info("Retrieving these login parameters:\n"
          + loginResponse.getSettings());

    connection.update(loginResponse.getSettings());

      LOGGER.info("Updated settings to these:\n" + connection.getSettings());
    // is a transit to the next stage possible
    if (protocolDataUnit.getBasicHeaderSegment().isFinalFlag()) {
      if (nextStage == LoginStage.LOGIN_OPERATIONAL_NEGOTIATION) {
        connection.getSession()
            .setPhase(new LoginOperationalNegotiationPhase());
      } else if (nextStage == LoginStage.FULL_FEATURE_PHASE) {
        connection.getSession().setPhase(new FullFeaturePhase());
//        return false;
      }
    }

//    return true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public final Exception isCorrect(final ProtocolDataUnit protocolDataUnit) {

    if (protocolDataUnit.getBasicHeaderSegment().getParser() instanceof LoginResponseParser) {
      return null;
    } else {
      return new IllegalStateException(new StringBuilder("Parser ").append(
          protocolDataUnit.getBasicHeaderSegment().getParser().toString())
          .append(" is instance of ").append(
              protocolDataUnit.getBasicHeaderSegment().getParser().getClass()
                  .toString()).append(" and not instance if LoginParser!")
          .toString());
    }

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

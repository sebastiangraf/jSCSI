/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id: GetConnectionsResponseState.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.connection;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory;
import org.jscsi.parser.datasegment.IDataSegment;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.text.TextResponseParser;

/**
 * <h1>GetConnectionsResponseState</h1>
 * <p/>
 * 
 * This state handles the response of a TextRequest PDU. So, there can be opened
 * more connections to these targets listed in this response.
 * 
 * @author Volker Wildi
 */
final class GetConnectionsResponseState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a <code>GetConnectionsResponseState</code>
   * instance, which uses the given connection for transmission.
   * 
   * @param initConnection
   *          The context connection, which is used for the network
   *          transmission.
   */
  protected GetConnectionsResponseState(final Connection initConnection) {

    super(initConnection);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public boolean execute() throws InternetSCSIException {

    ProtocolDataUnit protocolDataUnit;
    final IDataSegment textResponse = DataSegmentFactory.create(
        DataSegmentFormat.TEXT, connection
            .getSettingAsInt(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH));

    do {
      protocolDataUnit = connection.receive();

      if (!(protocolDataUnit.getBasicHeaderSegment().getParser() instanceof TextResponseParser)) {
        break;
      }

      textResponse.append(protocolDataUnit.getDataSegment(), protocolDataUnit
          .getBasicHeaderSegment().getDataSegmentLength());
    } while (!protocolDataUnit.getBasicHeaderSegment().isFinalFlag());

    // extract Target Session Handle Identifying Handle
    // final TextResponseParser parser = (TextResponseParser)
    // protocolDataUnit.getBasicHeaderSegment().getParser();
    // final ByteBuffer textDataSegment = ByteBuffer
    // .allocate(AbstractDataSegment.getTotalLength(textResponse.getLength()));
    // textResponse.serialize(textDataSegment, 0);
    //
    // try {
    // final String response = new String(textDataSegment.array(), "UTF-8");
    // final String[] lines = response.split("\0");
    // } catch (UnsupportedEncodingException e) {
    // if (LOGGER.isErrorEnabled()) {
    // LOGGER.error("Unsupported Encoding Exception: " +
    // e.getLocalizedMessage());
    // }
    // }

    return false;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  // /** {@inheritDoc} */
  // @Override
  // public final boolean isCorrect(final ProtocolDataUnit protocolDataUnit)
  // throws InternetSCSIException {
  //
  // // FIXME: Reject must also be supported.
  // return protocolDataUnit.getBasicHeaderSegment().getParser() instanceof
  // TextResponseParser;
  // }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

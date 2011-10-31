/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
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
package org.jscsi.initiator.connection.state;

import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory;
import org.jscsi.parser.datasegment.IDataSegment;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.text.TextResponseParser;

/**
 * <h1>GetConnectionsResponseState</h1> <p/> This state handles the response of
 * a TextRequest PDU. So, there can be opened more connections to these targets
 * listed in this response.
 * 
 * @author Volker Wildi
 */
final class GetConnectionsResponseState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a <code>GetConnectionsResponseState</code> instance,
   * which uses the given connection for transmission.
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
  public void execute() throws InternetSCSIException {

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

//    return false;
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

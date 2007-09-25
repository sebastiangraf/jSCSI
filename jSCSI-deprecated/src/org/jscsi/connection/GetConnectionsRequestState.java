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
 * $Id: GetConnectionsRequestState.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.connection;

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
import org.jscsi.parser.text.TextRequestParser;

/**
 * <h1>GetConnectionsRequestState</h1>
 * <p/>
 * 
 * This state requests a list of all possible connections to a specific target.
 * So it sends a TextRequest PDU with <code>SendTargets=</code> as the only
 * <code>OperationalTextKey</code> as data segment.
 * 
 * @author Volker Wildi
 */
final class GetConnectionsRequestState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a <code>GetConnectionsRequestState</code> instance,
   * which uses the given connection for transmission.
   * 
   * @param initConnection
   *          The context connection, which is used for the network
   *          transmission.
   */
  protected GetConnectionsRequestState(final Connection initConnection) {

    super(initConnection);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final boolean execute() throws InternetSCSIException {

    final ProtocolDataUnit protocolDataUnit = protocolDataUnitFactory.create(
        false, true, OperationCode.TEXT_REQUEST, connection
            .getSetting(OperationalTextKey.HEADER_DIGEST), connection
            .getSetting(OperationalTextKey.DATA_DIGEST));
    final TextRequestParser parser = (TextRequestParser) protocolDataUnit
        .getBasicHeaderSegment().getParser();

    final SettingsMap settings = new SettingsMap();
    settings.add(OperationalTextKey.SEND_TARGETS, "");

    final IDataSegment dataSegment = DataSegmentFactory.create(settings
        .asByteBuffer(), DataSegmentFormat.TEXT, connection
        .getSettingAsInt(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH));

    int bytes2Process = dataSegment.getLength();
    for (IDataSegmentIterator dataSegmentIterator = dataSegment.iterator(); dataSegmentIterator
        .hasNext();) {
      IDataSegmentChunk dataSegmentChunk = dataSegmentIterator
          .next(bytes2Process);
      protocolDataUnit.setDataSegment(dataSegmentChunk);
      parser.setTargetTransferTag(0xFFFFFFFF);
    }

    connection.enqueue(protocolDataUnit);
    connection.setState(new GetConnectionsResponseState(connection));
    return true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

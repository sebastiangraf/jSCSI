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
 * $Id: WriteFirstBurstState.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.connection;

import java.util.ArrayList;
import java.util.List;

import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.data.DataOutParser;
import org.jscsi.parser.datasegment.IDataSegmentIterator;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.IDataSegmentIterator.IDataSegmentChunk;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>WriteFirstBurstState</h1>
 * <p/>
 * 
 * This state handles a first Write Sending State, which sends at most
 * <code>FirstBurstLength</code> bytes in the first sequence.
 * 
 * @author Volker Wildi
 */
final class WriteFirstBurstState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The iterator of this data segment to send as next. */
  private IDataSegmentIterator iterator;

  /**
   * The Target Transfer Tag, which is sent by the iSCSI Target within a
   * Ready2Transfer PDU.
   */
  private final int targetTransferTag;

  /** The sequence number of this data package unit. */
  private int dataSequenceNumber;

  /** The start offset of the data to send. */
  private int bufferOffset;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a <code>WriteFirstBurstState</code> instance, which
   * sends the first data sequence.
   * 
   * @param initConnection
   *          This is the connection, which is used for the network
   *          transmission.
   * @param initIterator
   *          The next chunk of the data to send.
   * @param initTargetTransferTag
   *          The Target Transfer Tag to use as next.
   * @param initDataSequenceNumber
   *          The Data Sequence Number to use as next.
   * @param initBufferOffset
   *          The start offset of the data to send.
   */
  public WriteFirstBurstState(final Connection initConnection,
      final IDataSegmentIterator initIterator, final int initTargetTransferTag,
      final int initDataSequenceNumber, final int initBufferOffset) {

    super(initConnection);
    iterator = initIterator;
    targetTransferTag = initTargetTransferTag;
    dataSequenceNumber = initDataSequenceNumber;
    bufferOffset = initBufferOffset;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final boolean execute() throws InternetSCSIException {

    final List<ProtocolDataUnit> protocolDataUnits = new ArrayList<ProtocolDataUnit>(
        1);

    ProtocolDataUnit protocolDataUnit;
    DataOutParser dataOut;
    IDataSegmentChunk dataSegmentChunk;
    boolean finalFlag = false;
    final int maxRecvDataSegmentLength = connection
        .getSettingAsInt(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH);
    // the remaining bytes to send (do not forget the immediately send data!)
    int bytes2Transfer = connection
        .getSettingAsInt(OperationalTextKey.FIRST_BURST_LENGTH)
        - bufferOffset;

    while (bytes2Transfer > 0 && iterator.hasNext()) {
      if (bytes2Transfer <= maxRecvDataSegmentLength) {
        dataSegmentChunk = iterator.next(bytes2Transfer);
        finalFlag = true;
      } else {
        dataSegmentChunk = iterator.next(maxRecvDataSegmentLength);
        finalFlag = false;
      }

      protocolDataUnit = protocolDataUnitFactory.create(false, finalFlag,
          OperationCode.SCSI_DATA_OUT, connection
              .getSetting(OperationalTextKey.HEADER_DIGEST), connection
              .getSetting(OperationalTextKey.DATA_DIGEST));
      protocolDataUnit.getBasicHeaderSegment().setInitiatorTaskTag(
          connection.getSession().getInitiatorTaskTag());

      dataOut = (DataOutParser) protocolDataUnit.getBasicHeaderSegment()
          .getParser();

      dataOut.setTargetTransferTag(targetTransferTag);
      dataOut.setDataSequenceNumber(dataSequenceNumber++);
      dataOut.setBufferOffset(bufferOffset);
      bufferOffset += maxRecvDataSegmentLength;

      protocolDataUnit.setDataSegment(dataSegmentChunk);

      protocolDataUnits.add(protocolDataUnit);
      bytes2Transfer -= maxRecvDataSegmentLength;
    }

    connection.enqueue(protocolDataUnits);

    connection.setState(new WriteSecondResponseState(connection, iterator,
        dataSequenceNumber, bufferOffset));

    return true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

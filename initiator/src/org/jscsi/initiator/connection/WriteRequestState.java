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
 * $Id: WriteRequestState.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.initiator.connection;

import java.nio.ByteBuffer;

import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.DataSegmentFactory;
import org.jscsi.parser.datasegment.IDataSegment;
import org.jscsi.parser.datasegment.IDataSegmentIterator;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.scsi.SCSICommandDescriptorBlockParser;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.scsi.SCSICommandParser.TaskAttributes;

/**
 * <h1>WriteRequestState</h1>
 * <p/>
 * 
 * This state handles a Write Response with unsolicited data.
 * 
 * @author Volker Wildi
 */
final class WriteRequestState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The buffer to used for the message transfer. */
  private final ByteBuffer buffer;

  /** The task attributes of this write operation. */
  private final TaskAttributes taskAttributes;

  /** The expected length in bytes, which should be transfered. */
  private final int expectedDataTransferLength;

  /** The logical block address of the start block for this write operation. */
  private final int logicalBlockAddress;

  /** The start index of the buffer. */
  private final int bufferPosition;

  /**
   * The number of blocks (This block size is dependent on the size used on the
   * target side.) to read.
   */
  private final short transferLength;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a <code>WriteRequestState</code> instance, which
   * creates a request to the iSCSI Target.
   * 
   * @param initConnection
   *          This is the connection, which is used for the network
   *          transmission.
   * @param initBuffer
   *          This buffer should be sent.
   * @param initBufferPosition
   *          The start index of the buffer.
   * @param initTaskAttributes
   *          The task attributes of this task.
   * @param initExpectedDataTransferLength
   *          The expected length in bytes, which should be transfered.
   * @param initLogicalBlockAddress
   *          The logical block address of the first block to write.
   * @param initTransferLength
   *          The number of blocks to write.
   */
  public WriteRequestState(final Connection initConnection,
      final ByteBuffer initBuffer, final int initBufferPosition,
      final TaskAttributes initTaskAttributes,
      final int initExpectedDataTransferLength,
      final int initLogicalBlockAddress, final short initTransferLength) {

    super(initConnection);
    buffer = initBuffer;
    bufferPosition = initBufferPosition;
    taskAttributes = initTaskAttributes;
    expectedDataTransferLength = initExpectedDataTransferLength;
    logicalBlockAddress = initLogicalBlockAddress;
    transferLength = initTransferLength;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final boolean execute() throws InternetSCSIException {

    final ProtocolDataUnit protocolDataUnit = protocolDataUnitFactory.create(
        false, true, OperationCode.SCSI_COMMAND, connection
            .getSetting(OperationalTextKey.HEADER_DIGEST), connection
            .getSetting(OperationalTextKey.DATA_DIGEST));
    final SCSICommandParser scsi = (SCSICommandParser) protocolDataUnit
        .getBasicHeaderSegment().getParser();

    scsi.setReadExpectedFlag(false);
    scsi.setWriteExpectedFlag(true);
    scsi.setTaskAttributes(taskAttributes);

    scsi.setExpectedDataTransferLength(expectedDataTransferLength);

    final int maxRecvDataSegmentLength = connection
        .getSettingAsInt(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH);
    scsi.setCommandDescriptorBlock(SCSICommandDescriptorBlockParser
        .createWriteMessage(logicalBlockAddress, transferLength));

    final IDataSegment dataSegment = DataSegmentFactory.create(buffer,
        bufferPosition, expectedDataTransferLength, DataSegmentFormat.BINARY,
        maxRecvDataSegmentLength);
    final IDataSegmentIterator iterator = dataSegment.iterator();
    int bufferOffset = 0;

    if (connection.getSettingAsBoolean(OperationalTextKey.IMMEDIATE_DATA)) {
      final int min = Math.min(maxRecvDataSegmentLength, connection
          .getSettingAsInt(OperationalTextKey.FIRST_BURST_LENGTH));
      protocolDataUnit.setDataSegment(iterator.next(min));
      bufferOffset += min;
    }

    connection.enqueue(protocolDataUnit);

    if (!connection.getSettingAsBoolean(OperationalTextKey.INITIAL_R2T)
        && iterator.hasNext()) {
      connection.setState(new WriteFirstBurstState(connection, iterator,
          0xFFFFFFFF, 0, bufferOffset));
    } else {
      connection.setState(new WriteSecondResponseState(connection, iterator, 0,
          bufferOffset));
    }

    return true;
  }
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

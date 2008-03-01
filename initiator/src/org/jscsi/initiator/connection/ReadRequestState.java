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
 * $Id: ReadRequestState.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.initiator.connection;

import java.nio.ByteBuffer;

import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.scsi.SCSICommandDescriptorBlockParser;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.scsi.SCSICommandParser.TaskAttributes;

/**
 * <h1>ReadRequestState</h1>
 * <p/>
 * 
 * This state handles a Read Request with some unsolicited data.
 * 
 * @author Volker Wildi
 */
final class ReadRequestState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The buffer to used for the message transfer. */
  private final ByteBuffer buffer;

  /** The task attributes of this read operation. */
  private final TaskAttributes taskAttributes;

  /** The expected length in bytes, which should be transfered. */
  private final int expectedDataTransferLength;

  /** The logical block address of the beginning of the read operation. */
  private final int logicalBlockAddress;

  /**
   * The number of blocks (This block size is dependent on the size used on the
   * target side.) to read.
   */
  private final short transferLength;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a <code>ReadRequestState</code> instance, which
   * creates a request to the iSCSI Target.
   * 
   * @param initConnection
   *          This is the connection, which is used for the network
   *          transmission.
   * @param initBuffer
   *          This buffer should be read.
   * @param initTaskAttributes
   *          The task attributes of this task.
   * @param initExpectedDataTransferLength
   *          The expected length in bytes, which should be transfered.
   * @param initLogicalBlockAddress
   *          The logical block address of the first block to read.
   * @param initTransferLength
   *          The number of blocks to read.
   */
  public ReadRequestState(final Connection initConnection,
      final ByteBuffer initBuffer, final TaskAttributes initTaskAttributes,
      final int initExpectedDataTransferLength,
      final int initLogicalBlockAddress, final short initTransferLength) {

    super(initConnection);
    buffer = initBuffer;
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

    scsi.setReadExpectedFlag(true);
    scsi.setWriteExpectedFlag(false);
    scsi.setTaskAttributes(taskAttributes);
    scsi.setExpectedDataTransferLength(expectedDataTransferLength);
    scsi.setCommandDescriptorBlock(SCSICommandDescriptorBlockParser
        .createReadMessage(logicalBlockAddress, transferLength));

    connection.enqueue(protocolDataUnit);
    connection.setState(new ReadResponseState(connection, buffer, 0, 0));

    return true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

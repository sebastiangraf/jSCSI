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
 * $Id: CapacityRequestState.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.connection;

import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.scsi.SCSICommandDescriptorBlockParser;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.scsi.SCSICommandParser.TaskAttributes;

/**
 * <h1>CapacityRequestState</h1>
 * <p/>
 * 
 * This state handles a Capacity Request to retrieve the block size and the size
 * of the iSCSI Device.
 * 
 * @author Volker Wildi
 */
final class CapacityRequestState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The sent command has a fixed size of <code>8</code> bytes. */
  private static final int EXPECTED_DATA_TRANSFER_LENGTH = 0x08;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This object contains the informations about the capacity of the connected
   * target.
   */

  private final TargetCapacityInformations capacityInformation;

  private final TaskAttributes taskAttributes;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>CapacityRequestState</code>
   * instance.
   * 
   * @param initConnection
   *          This is the connection, which is used for the network
   *          transmission.
   * @param initCapacityInformation
   *          Store the informations about that iSCSI Device in this instance.
   * @param initTaskAttributes
   *          The task attributes, which are used with task.
   */
  protected CapacityRequestState(final Connection initConnection,
      final TargetCapacityInformations initCapacityInformation,
      final TaskAttributes initTaskAttributes) {

    super(initConnection);
    capacityInformation = initCapacityInformation;
    taskAttributes = initTaskAttributes;
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

    scsi.setExpectedDataTransferLength(EXPECTED_DATA_TRANSFER_LENGTH);

    scsi.setCommandDescriptorBlock(SCSICommandDescriptorBlockParser
        .createReadCapacityMessage());

    connection.enqueue(protocolDataUnit);
    connection.setState(new CapacityResponseState(connection,
        capacityInformation));

    return true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

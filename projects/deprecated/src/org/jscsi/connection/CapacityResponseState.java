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
 * $Id: CapacityResponseState.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.connection;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.data.DataInParser;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.scsi.SCSIStatus;

/**
 * <h1>CapacityResponseState</h1>
 * <p/>
 * 
 * This state handles a Capacity Response to extract the block size and the size
 * of the iSCSI Device.
 * 
 * @author Volker Wildi
 */
final class CapacityResponseState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This object contains the informations about the capacity of the connected
   * target.
   */
  private final TargetCapacityInformations capacityInformation;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>CapacityResponseState</code>
   * instance.
   * 
   * @param initConnection
   *          This is the connection, which is used for the network
   *          transmission.
   * @param initCapacityInformation
   *          Store the extracted informations in this instance.
   */
  protected CapacityResponseState(final Connection initConnection,
      final TargetCapacityInformations initCapacityInformation) {

    super(initConnection);
    capacityInformation = initCapacityInformation;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final boolean execute() throws InternetSCSIException {

    final ProtocolDataUnit protocolDataUnit = connection.receive();

    // first, we extract capacity informations
    if (!(protocolDataUnit.getBasicHeaderSegment().getParser() instanceof DataInParser)) {
      throw new InternetSCSIException(protocolDataUnit.getBasicHeaderSegment()
          .getParser().getClass().getSimpleName()
          + " is not the expected type of PDU.");
    }

    final DataInParser parser = (DataInParser) protocolDataUnit
        .getBasicHeaderSegment().getParser();
    capacityInformation.deserialize(protocolDataUnit.getDataSegment());

    if (!parser.isStatusFlag() || parser.getStatus() != SCSIStatus.GOOD) {
      throw new InternetSCSIException(
          "Error: Task is not completed successfully.");
    }

    return false;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * WriteSecondResponseState.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.initiator.connection.state;

import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.datasegment.IDataSegmentIterator;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.exception.OperationalTextKeyException;
import org.jscsi.parser.r2t.Ready2TransferParser;
import org.jscsi.parser.scsi.SCSIResponseParser;
import org.jscsi.parser.scsi.SCSIStatus;

/**
 * <h1>WriteSecondResponseState</h1> <p/> This state handles the response(s)
 * after the second and following data sequences was/were sent.
 * 
 * @author Volker Wildi
 */
final class WriteSecondResponseState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The chunk of the data segment to send as next. */
  private final IDataSegmentIterator iterator;

  /** The sequence number of this data package unit. */
  private final int dataSequenceNumber;

  /** The start offset of the data to send. */
  private final int bufferOffset;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a <code>WriteSecondResponseState</code> instance,
   * which handles the response of the iSCSI Target.
   * 
   * @param initConnection
   *          This is the connection, which is used for the network
   *          transmission.
   * @param initIterator
   *          The next chunk of data to send as next with the next write data
   *          sequence.
   * @param initDataSequenceNumber
   *          The Data Sequence Number to use as next.
   * @param initBufferOffset
   *          The start offset of the data to send.
   */
  public WriteSecondResponseState(final Connection initConnection,
      final IDataSegmentIterator initIterator,
      final int initDataSequenceNumber, final int initBufferOffset) {

    super(initConnection);
    iterator = initIterator;
    dataSequenceNumber = initDataSequenceNumber;
    bufferOffset = initBufferOffset;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final void execute() throws InternetSCSIException {

    final ProtocolDataUnit protocolDataUnit = connection.receive();
    LOGGER.trace("1" + protocolDataUnit);
    if (protocolDataUnit.getBasicHeaderSegment().getParser() instanceof Ready2TransferParser) {
      LOGGER.trace("2");
      final Ready2TransferParser parser = (Ready2TransferParser) protocolDataUnit
          .getBasicHeaderSegment().getParser();

      final int targetTransferTag = parser.getTargetTransferTag();
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("R2T has TTT set to " + targetTransferTag);
      }

      final int desiredDataTransferLength = parser
          .getDesiredDataTransferLength();
      if (desiredDataTransferLength > connection
          .getSettingAsInt(OperationalTextKey.MAX_BURST_LENGTH)) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error("MaxBurstLength limit is exceed.");
        }
        throw new InternetSCSIException("MaxBurstLength limit is exceed.");
      }

      connection.nextState(new WriteSecondBurstState(connection, iterator,
          targetTransferTag, desiredDataTransferLength, dataSequenceNumber,
          bufferOffset));
      super.stateFollowing = true;
      // return true;
      return;
    } else if (protocolDataUnit.getBasicHeaderSegment().getParser() instanceof SCSIResponseParser) {
      final SCSIResponseParser parser = (SCSIResponseParser) protocolDataUnit
          .getBasicHeaderSegment().getParser();

      if (!iterator.hasNext() && parser.getStatus() == SCSIStatus.GOOD) {
        connection.getSession().incrementInitiatorTaskTag();
        // return false;
        super.stateFollowing = false;
        return;
      }
    }

    throw new RuntimeException(protocolDataUnit.getBasicHeaderSegment().getParser().toString());
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public final Exception isCorrect(final ProtocolDataUnit protocolDataUnit) {

    // FIXME: Implement
    try {
      if (!connection.getSettingAsBoolean(OperationalTextKey.DATA_PDU_IN_ORDER)
          && !connection
              .getSettingAsBoolean(OperationalTextKey.DATA_SEQUENCE_IN_ORDER)) {
        return new UnsupportedOperationException(new StringBuilder(
            OperationalTextKey.DATA_PDU_IN_ORDER.toString()).append(
            " is yet not supported.").toString());
      } else {
        return null;
      }
    } catch (OperationalTextKeyException e) {
      return e;
    }

  }

  
  
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

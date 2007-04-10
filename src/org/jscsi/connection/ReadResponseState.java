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
 * $Id: ReadResponseState.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.connection;

import java.nio.ByteBuffer;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.data.DataInParser;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.scsi.SCSIResponseParser;
import org.jscsi.parser.scsi.SCSIStatus;

/**
 * <h1>ReadResponseState</h1>
 * <p/>
 * 
 * This state handles a Read Response.
 * 
 * @author Volker Wildi
 */
final class ReadResponseState extends AbstractState {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This is the wrap around divisor (2**32) of the modulo operation used by
   * incrementing the sequence numbers. See [RFC1982] for details.
   */
  private static final int WRAP_AROUND_DIVISOR = (int) Math.pow(2, 32);

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The buffer to used for the message transfer. */
  private final ByteBuffer buffer;

  /** The start offset of the data to send. */
  private int bufferOffset;

  /** The expected data sequence number of the next response. */
  private int expectedDataSequenceNumber;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>ReadResponseState</code>.
   * 
   * @param initConnection
   *          This is the connection, which is used for the network
   *          transmission.
   * @param initBuffer
   *          The buffer, where the readed bytes are stored in.
   * @param initBufferOffset
   *          The start offset of the data to send.
   * @param initExpectedDataSequenceNumber
   *          The Expected Data Sequence Number of the next response message.
   */
  public ReadResponseState(final Connection initConnection,
      final ByteBuffer initBuffer, final int initBufferOffset,
      final int initExpectedDataSequenceNumber) {

    super(initConnection);
    buffer = initBuffer;
    bufferOffset = initBufferOffset;
    expectedDataSequenceNumber = initExpectedDataSequenceNumber;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public final boolean execute() throws InternetSCSIException {

    ProtocolDataUnit protocolDataUnit;

    do {
      protocolDataUnit = connection.receive();

      if (protocolDataUnit.getBasicHeaderSegment().getParser() instanceof DataInParser) {
        final DataInParser parser = (DataInParser) protocolDataUnit
            .getBasicHeaderSegment().getParser();

        if (LOGGER.isDebugEnabled()) {
          LOGGER
              .debug("Remaining, DataSegmentLength: "
                  + buffer.remaining()
                  + ", "
                  + protocolDataUnit.getBasicHeaderSegment()
                      .getDataSegmentLength());
        }

        final ByteBuffer dataSegment = protocolDataUnit.getDataSegment();
        while (buffer.hasRemaining() && dataSegment.hasRemaining()) {
          buffer.put(dataSegment.get());
        }

        // last message with the status flag set
        if (parser.isStatusFlag() && parser.getStatus() == SCSIStatus.GOOD) {
          return false;
        } else if (connection
            .getSettingAsInt(OperationalTextKey.ERROR_RECOVERY_LEVEL) > 0
            && parser.isAcknowledgeFlag()) {
          // TODO: Test this case
          // send a DataAck
          connection.setState(new SNACKRequestState(connection, this, parser
              .getTargetTaskTag()));
          return true;
        }
      } else if (protocolDataUnit.getBasicHeaderSegment().getParser() instanceof SCSIResponseParser) {
        final SCSIResponseParser parser = (SCSIResponseParser) protocolDataUnit
            .getBasicHeaderSegment().getParser();

        final ByteBuffer dataSegment = protocolDataUnit.getDataSegment();
        while (buffer.hasRemaining() && dataSegment.hasRemaining()) {
          buffer.put(dataSegment.get());
        }

        if (parser.getStatus() == SCSIStatus.GOOD) {
          return false;
        } else {
          throw new RuntimeException();
        }
      }

    } while (!protocolDataUnit.getBasicHeaderSegment().isFinalFlag());

    return false;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public boolean isCorrect(final ProtocolDataUnit protocolDataUnit)
      throws InternetSCSIException {

    do {
      if (!(protocolDataUnit.getBasicHeaderSegment().getParser() instanceof DataInParser)) {
        break;
      }
      final DataInParser parser = (DataInParser) protocolDataUnit
          .getBasicHeaderSegment().getParser();

      if (connection.getSettingAsBoolean(OperationalTextKey.DATA_PDU_IN_ORDER)
          && connection
              .getSettingAsBoolean(OperationalTextKey.DATA_SEQUENCE_IN_ORDER)) {
        if (parser.getBufferOffset() < bufferOffset) {
          if (LOGGER.isErrorEnabled()) {
            LOGGER
                .error("This buffer offsets must be in increasing order and overlays are forbidden.");
          }

          break;
        }
        bufferOffset = parser.getBufferOffset();
      }

      if (parser.getDataSequenceNumber() != expectedDataSequenceNumber) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error("Data Sequence Number Mismatch (received, expected): "
              + parser.getDataSequenceNumber() + ", "
              + expectedDataSequenceNumber);
        }
        break;
      }

      incrementExpectedDataSequenceNumber();

      if (parser.isStatusFlag()) {
        incrementExpectedDataSequenceNumber();
        return super.isCorrect(protocolDataUnit);
      } else if (parser.getStatusSequenceNumber() != 0) {
        if (LOGGER.isErrorEnabled()) {
          LOGGER.error("Status Sequence Number must be zero.");
        }

        break;
      }

      return true;

    } while (false);

    return false;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Increments the Expected Data Sequence Number counter.
   */
  private void incrementExpectedDataSequenceNumber() {

    expectedDataSequenceNumber = (expectedDataSequenceNumber + 1)
        % WRAP_AROUND_DIVISOR;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

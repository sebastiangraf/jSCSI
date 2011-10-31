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

import java.nio.ByteBuffer;

import org.jscsi.initiator.connection.Connection;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.data.DataInParser;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.exception.OperationalTextKeyException;
import org.jscsi.parser.scsi.SCSIResponseParser;
import org.jscsi.parser.scsi.SCSIStatus;

/**
 * <h1>ReadResponseState</h1> <p/> This state handles a Read Response.
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
  public final void execute() throws InternetSCSIException {

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
//          return false;
          return;
        } else if (connection
            .getSettingAsInt(OperationalTextKey.ERROR_RECOVERY_LEVEL) > 0
            && parser.isAcknowledgeFlag()) {
          // TODO: Test this case
          // send a DataAck
          connection.nextState(new SNACKRequestState(connection, this, parser
              .getTargetTaskTag()));
//          return true;
          return;
        }
      } else if (protocolDataUnit.getBasicHeaderSegment().getParser() instanceof SCSIResponseParser) {
        final SCSIResponseParser parser = (SCSIResponseParser) protocolDataUnit
            .getBasicHeaderSegment().getParser();

        final ByteBuffer dataSegment = protocolDataUnit.getDataSegment();
        while (buffer.hasRemaining() && dataSegment.hasRemaining()) {
          buffer.put(dataSegment.get());
        }

        if (parser.getStatus() == SCSIStatus.GOOD) {
//          return false;
          super.stateFollowing = false;
          return;
        } else {
          throw new RuntimeException();
        }
      }
    } while (!protocolDataUnit.getBasicHeaderSegment().isFinalFlag());

//    return false;
    super.stateFollowing = false;
    return;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public Exception isCorrect(final ProtocolDataUnit protocolDataUnit) {

    if (!(protocolDataUnit.getBasicHeaderSegment().getParser() instanceof DataInParser)) {
      return new IllegalStateException(new StringBuilder("Parser ").append(
          protocolDataUnit.getBasicHeaderSegment().getParser().toString())
          .append(" is instance of ").append(
              protocolDataUnit.getBasicHeaderSegment().getParser().getClass()
                  .toString()).append(" and not instance if DataInParser!")
          .toString());
    }
    final DataInParser parser = (DataInParser) protocolDataUnit
        .getBasicHeaderSegment().getParser();
    try {
      if (connection.getSettingAsBoolean(OperationalTextKey.DATA_PDU_IN_ORDER)
          && connection
              .getSettingAsBoolean(OperationalTextKey.DATA_SEQUENCE_IN_ORDER)) {
        if (parser.getBufferOffset() < bufferOffset) {
          return new IllegalStateException(
              new StringBuilder(
                  "This buffer offsets must be in increasing order and overlays are forbidden.")
                  .append(" The parserOffset here is ").append(
                      parser.getBufferOffset()).append(
                      " and the bufferOffset is ").append(bufferOffset)
                  .toString());
        }
        bufferOffset = parser.getBufferOffset();
      }
    } catch (OperationalTextKeyException e) {
      return e;
    }

    if (parser.getDataSequenceNumber() != expectedDataSequenceNumber) {
      return new IllegalStateException(new StringBuilder(
          "Data Sequence Number Mismatch (received, expected): "
              + parser.getDataSequenceNumber() + ", "
              + expectedDataSequenceNumber).toString());

    }

    incrementExpectedDataSequenceNumber();

    if (parser.isStatusFlag()) {
      incrementExpectedDataSequenceNumber();
      return super.isCorrect(protocolDataUnit);
    } else if (parser.getStatusSequenceNumber() != 0) {
      return new IllegalStateException(new StringBuilder(
          "Status Sequence Number must be zero.").toString());
    }
    return null;

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

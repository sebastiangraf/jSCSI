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
package org.jscsi.initiator.connection.phase;

import java.nio.ByteBuffer;

import org.jscsi.initiator.connection.Connection;
import org.jscsi.initiator.connection.ITask;
import org.jscsi.initiator.connection.Session;
import org.jscsi.initiator.connection.TargetCapacityInformations;
import org.jscsi.initiator.connection.state.CapacityRequestState;
import org.jscsi.initiator.connection.state.GetConnectionsRequestState;
import org.jscsi.initiator.connection.state.LogoutRequestState;
import org.jscsi.initiator.connection.state.ReadRequestState;
import org.jscsi.initiator.connection.state.WriteRequestState;
import org.jscsi.parser.login.LoginStage;
import org.jscsi.parser.logout.LogoutRequestParser.LogoutReasonCode;
import org.jscsi.parser.scsi.SCSICommandParser.TaskAttributes;

/**
 * <h1>FullFeaturePhase</h1> <p/> This class represents the Full-Feature Phase
 * of a session. In this phase all commands are allowed (eg. read, write, login
 * of further connections, ...).
 * 
 * @author Volker Wildi
 */
public final class FullFeaturePhase extends AbstractPhase {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Number of blocks to read in the first stage of a read operation. */
  private static final int READ_FIRST_STAGE_BLOCKS = 64;

  /** Number of blocks to read in the second stage of a read operation. */
  private static final int READ_SECOND_STAGE_BLOCKS = 128;

  /** Number of blocks to read in the third stage of a read operation. */
  private static final int READ_THIRD_STAGE_BLOCKS = 256;

  /** Number of blocks to read in the first stage of a write operation. */
  private static final int WRITE_FIRST_STAGE_BLOCKS = 1024;

  /** Number of blocks to read in the second stage of a write operation. */
  private static final int WRITE_SECOND_STAGE_BLOCKS = 2048;

  /** Number of blocks to read in the third stage of a write operation. */
  private static final int WRITE_THIRD_STAGE_BLOCKS = 4096;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public final boolean login(final Session session) throws Exception {

    final Connection connection = session.getNextFreeConnection();
    connection.nextState(new GetConnectionsRequestState(connection));
    session.releaseUsedConnection(connection);
    return true;
  }

  // TODO: Uncomment
  // /** {@inheritDoc} */
  // @Override
  // public final void logoutConnection(final Session session) throws Exception
  // {
  //
  // final Connection connection = session.getNextFreeConnection();
  // connection.setState(new LogoutRequestState(connection,
  // LogoutReasonCode.CLOSE_CONNECTION));
  // connection.execute();
  // }

  /** {@inheritDoc} */
  @Override
  public final boolean logoutSession(final ITask task, final Session session)
      throws Exception {

    final Connection connection = session.getNextFreeConnection();
    connection.getSession().addOutstandingTask(connection,
        task);
    connection.nextState(new LogoutRequestState(connection,
        LogoutReasonCode.CLOSE_SESSION));
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean read(final ITask task, final Session session,
      final ByteBuffer dst, final int logicalBlockAddress, final long length)
      throws Exception {

    if (dst.remaining() < length) {
      throw new IllegalArgumentException("Destination buffer is too small.");
    }

    int startAddress = logicalBlockAddress;
    final long blockSize = session.getBlockSize();
    long totalBlocks = (long) Math.ceil(length / (double) blockSize);
    long bytes2Process = length;

    final Connection connection = session.getNextFreeConnection();
    connection.getSession().addOutstandingTask(connection,
        task);

    // first stage
    short blocks = (short) Math.min(READ_FIRST_STAGE_BLOCKS, totalBlocks);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Now reading sequences of length " + blocks + " blocks.");
    }

    connection.nextState(new ReadRequestState(connection, dst,
        TaskAttributes.SIMPLE, (int) Math
            .min(bytes2Process, blocks * blockSize), startAddress, blocks));
    startAddress += blocks;
    totalBlocks -= blocks;
    bytes2Process -= blocks * blockSize;

    // second stage
    blocks = (short) Math.min(READ_SECOND_STAGE_BLOCKS, totalBlocks);

    if (blocks > 0) {

      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Now reading sequences of length " + blocks + " blocks.");
      }
      connection.nextState(new ReadRequestState(connection, dst,
          TaskAttributes.SIMPLE, (int) Math.min(bytes2Process, blocks
              * blockSize), startAddress, blocks));
      startAddress += blocks;
      totalBlocks -= blocks;
      bytes2Process -= blocks * blockSize;
    }

    // third stage
    blocks = (short) Math.min(READ_THIRD_STAGE_BLOCKS, totalBlocks);

    while (blocks > 0) {

      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Now reading sequences of length " + blocks + " blocks.");
      }

      connection.nextState(new ReadRequestState(connection, dst,
          TaskAttributes.SIMPLE, (int) Math.min(bytes2Process, blocks
              * blockSize), startAddress, blocks));
      startAddress += blocks;
      totalBlocks -= blocks;
      blocks = (short) Math.min(READ_THIRD_STAGE_BLOCKS, totalBlocks);
    }
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean write(final ITask task, final Session session,
      final ByteBuffer src, final int logicalBlockAddress, final long length)
      throws Exception {

    if (src.remaining() < length) {
      throw new IllegalArgumentException(
          "Source buffer is too small. Buffer size: " + src.remaining()
              + " Expected: " + length);
    }

    int startAddress = logicalBlockAddress;
    final long blockSize = session.getBlockSize();
    int totalBlocks = (int) Math.ceil(length / (double) blockSize);
    long bytes2Process = length;
    int bufferPosition = 0;

    final Connection connection = session.getNextFreeConnection();
    connection.getSession().addOutstandingTask(connection,
        task);

    // first stage
    short blocks = (short) Math.min(WRITE_FIRST_STAGE_BLOCKS, totalBlocks);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Now sending sequences of length " + blocks + " blocks.");
    }

    int expectedDataTransferLength = (int) Math.min(bytes2Process, blocks
        * blockSize);
    connection
        .nextState(new WriteRequestState(connection, src, bufferPosition,
            TaskAttributes.SIMPLE, expectedDataTransferLength, startAddress,
            blocks));
    startAddress += blocks;
    totalBlocks -= blocks;
    bytes2Process -= blocks * blockSize;
    bufferPosition += expectedDataTransferLength;

    // second stage
    blocks = (short) Math.min(WRITE_SECOND_STAGE_BLOCKS, totalBlocks);

    if (blocks > 0) {

      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Now sending sequences of length " + blocks + " blocks.");
        LOGGER.info("Remaining, DataSegmentLength: " + bytes2Process + ", "
            + expectedDataTransferLength);
      }

      expectedDataTransferLength = (int) Math.min(bytes2Process, blocks
          * blockSize);
      connection.nextState(new WriteRequestState(connection, src,
          bufferPosition, TaskAttributes.SIMPLE, expectedDataTransferLength,
          startAddress, blocks));
      startAddress += blocks;
      totalBlocks -= blocks;
      bytes2Process -= blocks * blockSize;
      bufferPosition += expectedDataTransferLength;
    }

    // third stage
    blocks = (short) Math.min(WRITE_THIRD_STAGE_BLOCKS, totalBlocks);

    while (blocks > 0) {

      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Now sending sequences of length " + blocks + " blocks.");
      }

      expectedDataTransferLength = (int) Math.min(bytes2Process, blocks
          * blockSize);
      connection.nextState(new WriteRequestState(connection, src,
          bufferPosition, TaskAttributes.SIMPLE, expectedDataTransferLength,
          startAddress, blocks));
      startAddress += blocks;
      totalBlocks -= blocks;
      blocks = (short) Math.min(READ_THIRD_STAGE_BLOCKS, totalBlocks);
      bufferPosition += expectedDataTransferLength;
    }
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public final boolean getCapacity(final Session session,
      final TargetCapacityInformations capacityInformation) throws Exception {

    if (capacityInformation == null) {
      throw new NullPointerException();
    }

    final Connection connection = session.getNextFreeConnection();
    if (connection == null) {
      throw new NullPointerException();
    }

    connection.nextState(new CapacityRequestState(connection,
        capacityInformation, TaskAttributes.SIMPLE));
    session.releaseUsedConnection(connection);
    return true;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public final LoginStage getStage() {

    return LoginStage.FULL_FEATURE_PHASE;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

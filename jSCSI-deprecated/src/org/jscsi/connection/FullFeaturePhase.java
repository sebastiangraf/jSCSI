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
 * $Id: FullFeaturePhase.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.connection;

import java.nio.ByteBuffer;

import org.jscsi.parser.login.LoginStage;
import org.jscsi.parser.logout.LogoutRequestParser.LogoutReasonCode;
import org.jscsi.parser.scsi.SCSICommandParser.TaskAttributes;

/**
 * <h1>FullFeaturePhase</h1>
 * <p/>
 * 
 * This class represents the Full-Feature Phase of a session. In this phase all
 * commands are allowed (eg. read, write, login of further connections, ...).
 * 
 * @author Volker Wildi
 */
final class FullFeaturePhase extends AbstractPhase {

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
  public final void login(final Session session) throws Exception {

    final Connection connection = session.getNextFreeConnection();
    connection.setState(new GetConnectionsRequestState(connection));
    connection.execute();
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
  public final void logoutSession(final Session session) throws Exception {

    final Connection connection = session.getNextFreeConnection();
    connection.setState(new LogoutRequestState(connection,
        LogoutReasonCode.CLOSE_SESSION));
    connection.execute();
  }

  /** {@inheritDoc} */
  @Override
  public final void read(final Session session, final ByteBuffer dst,
      final int logicalBlockAddress, final long length) throws Exception {

    if (dst.remaining() < length) {
      throw new IllegalArgumentException("Destination buffer is too small.");
    }

    int startAddress = logicalBlockAddress;
    final long blockSize = session.getBlockSize();
    long totalBlocks = (long) Math.ceil(length / (double) blockSize);
    long bytes2Process = length;

    final Connection connection = session.getNextFreeConnection();

    // first stage
    short blocks = (short) Math.min(READ_FIRST_STAGE_BLOCKS, totalBlocks);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Now reading sequences of length " + blocks + " blocks.");
    }

    connection.setState(new ReadRequestState(connection, dst,
        TaskAttributes.SIMPLE, (int) Math
            .min(bytes2Process, blocks * blockSize), startAddress, blocks));
    connection.execute();
    startAddress += blocks;
    totalBlocks -= blocks;
    bytes2Process -= blocks * blockSize;

    // second stage
    blocks = (short) Math.min(READ_SECOND_STAGE_BLOCKS, totalBlocks);

    if (blocks > 0) {

      if (LOGGER.isInfoEnabled()) {
        LOGGER.info("Now reading sequences of length " + blocks + " blocks.");
      }
      connection.setState(new ReadRequestState(connection, dst,
          TaskAttributes.SIMPLE, (int) Math.min(bytes2Process, blocks
              * blockSize), startAddress, blocks));
      connection.execute();
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

      connection.setState(new ReadRequestState(connection, dst,
          TaskAttributes.SIMPLE, (int) Math.min(bytes2Process, blocks
              * blockSize), startAddress, blocks));
      connection.execute();
      startAddress += blocks;
      totalBlocks -= blocks;
      blocks = (short) Math.min(READ_THIRD_STAGE_BLOCKS, totalBlocks);
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void write(final Session session, final ByteBuffer src,
      final int logicalBlockAddress, final long length) throws Exception {

    if (src.remaining() < length) {
      throw new IllegalArgumentException("Source buffer is too small.");
    }

    int startAddress = logicalBlockAddress;
    final long blockSize = session.getBlockSize();
    int totalBlocks = (int) Math.ceil(length / (double) blockSize);
    long bytes2Process = length;
    int bufferPosition = 0;

    final Connection connection = session.getNextFreeConnection();

    // first stage
    short blocks = (short) Math.min(WRITE_FIRST_STAGE_BLOCKS, totalBlocks);

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Now sending sequences of length " + blocks + " blocks.");
    }

    int expectedDataTransferLength = (int) Math.min(bytes2Process, blocks
        * blockSize);
    connection
        .setState(new WriteRequestState(connection, src, bufferPosition,
            TaskAttributes.SIMPLE, expectedDataTransferLength, startAddress,
            blocks));
    connection.execute();
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
      connection.setState(new WriteRequestState(connection, src,
          bufferPosition, TaskAttributes.SIMPLE, expectedDataTransferLength,
          startAddress, blocks));
      connection.execute();
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
      connection.setState(new WriteRequestState(connection, src,
          bufferPosition, TaskAttributes.SIMPLE, expectedDataTransferLength,
          startAddress, blocks));
      connection.execute();
      startAddress += blocks;
      totalBlocks -= blocks;
      blocks = (short) Math.min(READ_THIRD_STAGE_BLOCKS, totalBlocks);
      bufferPosition += expectedDataTransferLength;
    }
  }

  /** {@inheritDoc} */
  @Override
  public final void getCapacity(final Session session,
      final TargetCapacityInformations capacityInformation) throws Exception {

    if (capacityInformation == null) {
      throw new NullPointerException();
    }

    final Connection connection = session.getNextFreeConnection();
    if (connection == null) {
      throw new NullPointerException();
    }

    connection.setState(new CapacityRequestState(connection,
        capacityInformation, TaskAttributes.SIMPLE));
    connection.execute();
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

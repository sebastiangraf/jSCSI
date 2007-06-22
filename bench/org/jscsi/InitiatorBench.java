///*
// * Copyright 2007 Marc Kramis
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * 
// *     http://www.apache.org/licenses/LICENSE-2.0
// * 
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * 
// * $Id: InitiatorBench.java 2500 2007-03-05 13:29:08Z lemke $
// * 
// */
//
//package org.jscsi;
//
//import java.nio.ByteBuffer;
//import java.util.Random;
//
//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
//import org.perfidix.BenchClass;
//
///**
// * <h1>InitiatorBench</h1>
// * <p/>
// * 
// * This class is a benchmark to measure the performance of the Java implemented
// * iSCSI Initiator.
// * 
// * @author Volker Wildi
// */
//@BenchClass(runs = 10)
//public class InitiatorBench {
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  private static final Log LOGGER = LogFactory.getLog(InitiatorBench.class);
//
//  /** Name of the device name on the iSCSI Target. */
//  private static final String TARGET_NAME = "disk5";
//
//  /** The size (in bytes) of a single block. */
//  private static final int BLOCK_SIZE = 8 * 1024;
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  /** The <code>Initiator</code> instance to use for measurements. */
//  private final Initiator initiator;
//
//  /** The random number generator to fill the buffer to send. */
//  private final Random randomGenerator;
//
//  /**
//   * The buffer containing the data, which are read/written from/to the iSCSI
//   * Target. This has a size of <code>BLOCK_SIZE</code>.
//   */
//  private final ByteBuffer buffer;
//
//  /**
//   * The buffer containing the data, which are read/written from/to the iSCSI
//   * Target. This has a size of <code>10 * BLOCK_SIZE</code>.
//   */
//  private final ByteBuffer buffer10;
//
//  /**
//   * The buffer containing the data, which are read/written from/to the iSCSI
//   * Target. This has a size of <code>100 * BLOCK_SIZE</code>.
//   */
//  private final ByteBuffer buffer100;
//
//  /**
//   * The buffer containing the data, which are read/written from/to the iSCSI
//   * Target. This has a size of <code>1000 * BLOCK_SIZE</code>.
//   */
//  private final ByteBuffer buffer1000;
//
//  /**
//   * The buffer containing the data, which are read/written from/to the iSCSI
//   * Target. This has a size of <code>10000 * BLOCK_SIZE</code>.
//   */
//  private final ByteBuffer buffer10000;
//
//  /**
//   * The buffer containing the data, which are read/written from/to the iSCSI
//   * Target. This has a size of <code>100000 * BLOCK_SIZE</code>.
//   */
//  private final ByteBuffer buffer100000;
//
//  /**
//   * The buffer containing the data, which are read/written from/to the iSCSI
//   * Target. This has a size of <code>120000 * BLOCK_SIZE</code>.
//   */
//  private final ByteBuffer buffer120000;
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  /**
//   * The last block address of the given target for the given block size of
//   * <code>BLOCK_SIZE</code> bytes.
//   */
//  private long lastBlockAddress;
//
//  private long loopCounter;
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  /**
//   * Constructor to create a new, empty <code>InitiatorBench</code> instance.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public InitiatorBench() throws Exception {
//
//    initiator = new Initiator(Configuration.create());
//    randomGenerator = new Random(System.currentTimeMillis());
//    buffer = ByteBuffer.allocate(BLOCK_SIZE);
//    buffer10 = ByteBuffer.allocate(10 * BLOCK_SIZE);
//    buffer100 = ByteBuffer.allocate(100 * BLOCK_SIZE);
//    buffer1000 = ByteBuffer.allocate(1000 * BLOCK_SIZE);
//    buffer10000 = ByteBuffer.allocate(10000 * BLOCK_SIZE);
//    buffer100000 = ByteBuffer.allocate(100000 * BLOCK_SIZE);
//    buffer120000 = ByteBuffer.allocate(120000 * BLOCK_SIZE);
//  }
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  /** {@inheritDoc} */
//  public final void setUp() {
//
//    try {
//      loopCounter++;
//      initiator.createSession(TARGET_NAME);
//      lastBlockAddress =
//          (initiator.getCapacity(TARGET_NAME) * initiator
//              .getBlockSize(TARGET_NAME))
//              / BLOCK_SIZE;
//      LOGGER.debug("Open the " + loopCounter + "th session.");
//    } catch (final Exception e) {
//      e.printStackTrace();
//    }
//  }
//
//  /** {@inheritDoc} */
//  public final void tearDown() {
//
//    try {
//      buffer.clear();
//      buffer10.clear();
//      buffer100.clear();
//      buffer1000.clear();
//      buffer10000.clear();
//      buffer100000.clear();
//      buffer120000.clear();
//      initiator.closeSession(TARGET_NAME);
//      synchronized (this) {
//        wait(3000);
//      }
//    } catch (final Exception e) {
//      e.printStackTrace();
//    }
//  }
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  /**
//   * Benchmark to measure the time needed to read 1 block.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialRead1() throws Exception {
//
//    int logicalBlockAddress = getLastBlockAddress(1);
//    initiator.read(this, TARGET_NAME, buffer, logicalBlockAddress, buffer
//        .remaining());
//  }
//
//  /**
//   * Benchmark to measure the time needed to read 10 blocks in sequential order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialRead10() throws Exception {
//
//    readSequentialBlocks(buffer10, 10);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read 100 blocks in sequential
//   * order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialRead100() throws Exception {
//
//    readSequentialBlocks(buffer100, 100);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read 1000 blocks in sequential
//   * order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialRead1000() throws Exception {
//
//    readSequentialBlocks(buffer1000, 1000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read 1000 blocks in sequential
//   * order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialRead10000() throws Exception {
//
//    readSequentialBlocks(buffer10000, 10000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read 1000 blocks in sequential
//   * order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialRead100000() throws Exception {
//
//    readSequentialBlocks(buffer100000, 100000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read 1000 blocks in sequential
//   * order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialRead120000() throws Exception {
//
//    readSequentialBlocks(buffer120000, 120000);
//  }
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  /**
//   * Benchmark to measure the time needed to write 1 block.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialWrite1() throws Exception {
//
//    int logicalBlockAddress = getLastBlockAddress(1);
//    initiator.write(this, TARGET_NAME, buffer, logicalBlockAddress, buffer
//        .remaining());
//  }
//
//  /**
//   * Benchmark to measure the time needed to write 10 blocks in sequential
//   * order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialWrite10() throws Exception {
//
//    writeSequentialBlocks(buffer10, 10);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write 100 blocks in sequential
//   * order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialWrite100() throws Exception {
//
//    writeSequentialBlocks(buffer100, 100);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write 1000 blocks in sequential
//   * order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialWrite1000() throws Exception {
//
//    writeSequentialBlocks(buffer1000, 1000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write 10000 blocks in sequential
//   * order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialWrite10000() throws Exception {
//
//    writeSequentialBlocks(buffer10000, 10000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write 100000 blocks in sequential
//   * order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialWrite100000() throws Exception {
//
//    writeSequentialBlocks(buffer100000, 100000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write 120000 blocks in sequential
//   * order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchSequentialWrite120000() throws Exception {
//
//    writeSequentialBlocks(buffer120000, 120000);
//  }
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  /**
//   * Benchmark to measure the time needed to read 1 blocks in random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomRead1() throws Exception {
//
//    readRandomBlocks(buffer, 1);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read <code>10</code> blocks in
//   * random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomRead10() throws Exception {
//
//    readRandomBlocks(buffer10, 10);
//  }
//
//  /**
//   * // * Benchmark to measure the time needed to read <code>100</code> blocks
//   * in // * random order. // * // *
//   * 
//   * @throws Exception // *
//   *           if any error occurs. //
//   */
//  public final void benchRandomRead100() throws Exception {
//
//    readRandomBlocks(buffer100, 100);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read <code>1000</code> blocks in
//   * random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomRead1000() throws Exception {
//
//    readRandomBlocks(buffer1000, 1000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read <code>10000</code> blocks in
//   * random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomRead10000() throws Exception {
//
//    readRandomBlocks(buffer10000, 10000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read <code>100000</code> blocks
//   * in random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomRead100000() throws Exception {
//
//    readRandomBlocks(buffer100000, 100000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read <code>120000</code> blocks
//   * in random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomRead120000() throws Exception {
//
//    readRandomBlocks(buffer120000, 120000);
//  }
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  /**
//   * Benchmark to measure the time needed to write one block in random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomWrite1() throws Exception {
//
//    writeRandomBlocks(buffer, 1);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write <code>10</code> blocks in
//   * random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomWrite10() throws Exception {
//
//    writeRandomBlocks(buffer10, 10);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write <code>100</code> blocks in
//   * random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomWrite100() throws Exception {
//
//    writeRandomBlocks(buffer100, 100);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write <code>1000</code> blocks in
//   * random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomWrite1000() throws Exception {
//
//    writeRandomBlocks(buffer1000, 1000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write <code>10000</code> blocks
//   * in random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomWrite10000() throws Exception {
//
//    writeRandomBlocks(buffer10000, 10000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write <code>100000</code> blocks
//   * in random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomWrite100000() throws Exception {
//
//    writeRandomBlocks(buffer100000, 100000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write <code>120000</code> blocks
//   * in random order.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchRandomWrite120000() throws Exception {
//
//    writeRandomBlocks(buffer120000, 120000);
//  }
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  /**
//   * Benchmark to measure the time needed to read 1 block at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchReadAtOnce1() throws Exception {
//
//    readBlocksAtOnce(buffer, 1);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read 10 blocks at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchReadAtOnce10() throws Exception {
//
//    readBlocksAtOnce(buffer10, 10);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read 100 blocks at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchReadAtOnce100() throws Exception {
//
//    readBlocksAtOnce(buffer100, 100);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read 1000 blocks at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchReadAtOnce1000() throws Exception {
//
//    readBlocksAtOnce(buffer1000, 1000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read 10000 blocks at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchReadAtOnce10000() throws Exception {
//
//    readBlocksAtOnce(buffer10000, 10000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read 100000 blocks at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchReadAtOnce100000() throws Exception {
//
//    readBlocksAtOnce(buffer100000, 100000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to read 120000 blocks at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchReadAtOnce120000() throws Exception {
//
//    readBlocksAtOnce(buffer120000, 120000);
//  }
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  /**
//   * Benchmark to measure the time needed to write 1 block at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchWriteAtOnce1() throws Exception {
//
//    writeBlocksAtOnce(buffer, 1);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write 10 blocks at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchWriteAtOnce10() throws Exception {
//
//    writeBlocksAtOnce(buffer10, 10);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write 100 blocks at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchWriteAtOnce100() throws Exception {
//
//    writeBlocksAtOnce(buffer100, 100);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write 1000 blocks at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchWriteAtOnce1000() throws Exception {
//
//    writeBlocksAtOnce(buffer1000, 1000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write 10000 blocks at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchWriteAtOnce10000() throws Exception {
//
//    writeBlocksAtOnce(buffer10000, 10000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write 100000 blocks at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchWriteAtOnce100000() throws Exception {
//
//    writeBlocksAtOnce(buffer100000, 100000);
//  }
//
//  /**
//   * Benchmark to measure the time needed to write 120000 blocks at once.
//   * 
//   * @throws Exception
//   *           if any error occurs.
//   */
//  public final void benchWriteAtOnce120000() throws Exception {
//
//    writeBlocksAtOnce(buffer120000, 120000);
//  }
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  private final void readSequentialBlocks(final ByteBuffer dst, final int blocks)
//      throws Exception {
//
//    int logicalBlockAddress = getLastBlockAddress(blocks);
//    for (int i = 0; i < blocks; i++) {
//      initiator.read(this, TARGET_NAME, dst, logicalBlockAddress, BLOCK_SIZE);
//      logicalBlockAddress++;
//    }
//  }
//
//  private final void writeSequentialBlocks(
//      final ByteBuffer src,
//      final int blocks) throws Exception {
//
//    int logicalBlockAddress = getLastBlockAddress(blocks);
//    for (int i = 0; i < blocks; i++) {
//      initiator.write(this, TARGET_NAME, src, logicalBlockAddress, BLOCK_SIZE);
//      logicalBlockAddress++;
//    }
//  }
//
//  private final void readRandomBlocks(final ByteBuffer dst, final int blocks)
//      throws Exception {
//
//    int logicalBlockAddress = getLastBlockAddress(blocks);
//    for (int i = 0; i < blocks; i++) {
//      initiator.read(this, TARGET_NAME, dst, logicalBlockAddress, BLOCK_SIZE);
//      logicalBlockAddress = getLastBlockAddress(blocks);
//    }
//  }
//
//  private final void writeRandomBlocks(final ByteBuffer src, final int blocks)
//      throws Exception {
//
//    int logicalBlockAddress = getLastBlockAddress(blocks);
//    for (int i = 0; i < blocks; i++) {
//      initiator.write(this, TARGET_NAME, src, logicalBlockAddress, BLOCK_SIZE);
//      logicalBlockAddress = getLastBlockAddress(blocks);
//    }
//  }
//
//  private final void readBlocksAtOnce(final ByteBuffer dst, final int blocks)
//      throws Exception {
//
//    final int logicalBlockAddress = getLastBlockAddress(blocks);
//    initiator.read(this, TARGET_NAME, dst, logicalBlockAddress, blocks
//        * BLOCK_SIZE);
//  }
//
//  private final void writeBlocksAtOnce(final ByteBuffer src, final int blocks)
//      throws Exception {
//
//    final int logicalBlockAddress = getLastBlockAddress(blocks);
//    initiator.write(this, TARGET_NAME, src, logicalBlockAddress, blocks
//        * BLOCK_SIZE);
//  }
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  private final int getLastBlockAddress(final int blocks) {
//
//    return randomGenerator.nextInt((int) lastBlockAddress - blocks + 1);
//  }
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//}

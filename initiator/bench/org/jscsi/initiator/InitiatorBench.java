/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */

package org.jscsi.initiator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.Future;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.perfidix.Benchmark;
import org.perfidix.annotation.AfterBenchClass;
import org.perfidix.annotation.BeforeBenchClass;
import org.perfidix.annotation.BenchClass;
import org.perfidix.annotation.SkipBench;
import org.perfidix.result.BenchmarkResult;
import org.perfidix.result.ClassResult;
import org.perfidix.result.MethodResult;

/**
 * <h1>InitiatorBench</h1>
 * <p/>
 * This class is a benchmark to measure the performance of the Java implemented
 * iSCSI Initiator.
 * 
 * @author Volker Wildi
 * @author Bastian Lemke
 * @author Patrice Brend'amour
 */
@BenchClass(runs = 1)
public class InitiatorBench {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private static final Log LOGGER = LogFactory.getLog(InitiatorBench.class);

  /** Name of the device name on the iSCSI Target. */
  private static final String TARGET_NAME = "testing-bench4";

  /** The size (in bytes) of a single block. */
  private static final int BLOCK_SIZE = 32 * 1024;

  private static int NUMBER_OF_BLOCKS = 100;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The <code>Initiator</code> instance to use for measurements. */
  private final Initiator initiator;

  /** The random number generator to fill the buffer to send. */
  private final Random randomGenerator;

  /**
   * The buffer containing the data, which are read/written from/to the iSCSI
   * Target. This has a size of <code>BLOCK_SIZE</code>.
   */
  private final ByteBuffer buffer;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  /**
   * The last block address of the given target for the given block size of
   * <code>BLOCK_SIZE</code> bytes.
   */
  private long lastBlockAddress;

  private long loopCounter;

  protected final int numBlocks;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>InitiatorBench</code> instance.
   * 
   * @throws Exception
   *           if any error occurs.
   */
  public InitiatorBench() throws Exception {

    initiator = new Initiator(Configuration.create());
    randomGenerator = new Random(System.currentTimeMillis());

    numBlocks = NUMBER_OF_BLOCKS;
    buffer = ByteBuffer.allocate(numBlocks * BLOCK_SIZE);

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @BeforeBenchClass
  public final void setUp() {

    try {
      loopCounter++;
      initiator.createSession(TARGET_NAME);
      lastBlockAddress = (initiator.getCapacity(TARGET_NAME) * initiator
          .getBlockSize(TARGET_NAME))
          / BLOCK_SIZE;
      LOGGER.debug("Open the " + loopCounter + "th session.");
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  @AfterBenchClass
  public final void tearDown() {

    try {
      buffer.clear();
      initiator.closeSession(TARGET_NAME);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Benchmark to measure the time needed to read 1 block.
   * 
   * @throws Exception
   *           if any error occurs.
   */
  public final void sequentialRead() {

    // int logicalBlockAddress = getLastBlockAddress(1);
    // final Future<Void> read = initiator.multiThreadedRead(this, TARGET_NAME,
    // buffer, logicalBlockAddress, buffer
    // .remaining());
    // read.get();
    try {
      readSequentialBlocks(buffer, numBlocks);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Benchmark to measure the time needed to write 1 block.
   * 
   * @throws Exception
   *           if any error occurs.
   */
  public final void sequentialWrite() {

    // int logicalBlockAddress = getLastBlockAddress(1);
    // final Future<Void> write = initiator.multiThreadedWrite(this,
    // TARGET_NAME, buffer, logicalBlockAddress, buffer
    // .remaining());
    // write.get();
    try {
      writeSequentialBlocks(buffer, numBlocks);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Benchmark to measure the time needed to read 1 blocks in random order.
   * 
   * @throws Exception
   *           if any error occurs.
   */
  public final void randomRead() {

    try {
      readRandomBlocks(buffer, numBlocks);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Benchmark to measure the time needed to write one block in random order.
   * 
   * @throws Exception
   *           if any error occurs.
   */
  public final void randomWrite() {

    try {
      writeRandomBlocks(buffer, numBlocks);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Benchmark to measure the time needed to read 1 block at once.
   * 
   * @throws Exception
   *           if any error occurs.
   */
  public final void readAtOnce() {

    try {
      readBlocksAtOnce(buffer, numBlocks);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Benchmark to measure the time needed to write 1 block at once.
   * 
   * @throws Exception
   *           if any error occurs.
   */
  public final void writeAtOnce() {

    try {
      writeBlocksAtOnce(buffer, numBlocks);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @SkipBench
  private final void readSequentialBlocks(final ByteBuffer dst, final int blocks)
      throws Exception {

    dst.clear();
    int logicalBlockAddress = getLastBlockAddress(blocks);
    for (int i = 0; i < blocks; i++) {
      final Future<Void> read = initiator.multiThreadedRead(this, TARGET_NAME,
          dst, logicalBlockAddress, BLOCK_SIZE);
      read.get();
      logicalBlockAddress++;
    }
  }

  @SkipBench
  private final void writeSequentialBlocks(final ByteBuffer src,
      final int blocks) throws Exception {

    src.clear();
    int logicalBlockAddress = getLastBlockAddress(blocks);
    for (int i = 0; i < blocks; i++) {
      final Future<Void> write = initiator.multiThreadedWrite(this,
          TARGET_NAME, src, logicalBlockAddress, BLOCK_SIZE);
      write.get();
      logicalBlockAddress++;
    }
  }

  @SkipBench
  private final void readRandomBlocks(final ByteBuffer dst, final int blocks)
      throws Exception {

    dst.clear();
    int logicalBlockAddress = getLastBlockAddress(blocks);
    for (int i = 0; i < blocks; i++) {
      final Future<Void> read = initiator.multiThreadedRead(this, TARGET_NAME,
          dst, logicalBlockAddress, BLOCK_SIZE);
      read.get();
      logicalBlockAddress = getLastBlockAddress(blocks);
    }
  }

  @SkipBench
  private final void writeRandomBlocks(final ByteBuffer src, final int blocks)
      throws Exception {

    src.clear();
    int logicalBlockAddress = getLastBlockAddress(blocks);
    for (int i = 0; i < blocks; i++) {
      final Future<Void> write = initiator.multiThreadedWrite(this,
          TARGET_NAME, src, logicalBlockAddress, BLOCK_SIZE);
      write.get();
      logicalBlockAddress = getLastBlockAddress(blocks);
    }
  }

  @SkipBench
  private final void readBlocksAtOnce(final ByteBuffer dst, final int blocks)
      throws Exception {

    dst.clear();
    final int logicalBlockAddress = getLastBlockAddress(blocks);
    final Future<Void> read = initiator.multiThreadedRead(this, TARGET_NAME,
        dst, logicalBlockAddress, blocks * BLOCK_SIZE);
    read.get();
  }

  @SkipBench
  private final void writeBlocksAtOnce(final ByteBuffer src, final int blocks)
      throws Exception {

    src.clear();
    final int logicalBlockAddress = getLastBlockAddress(blocks);
    final Future<Void> write = initiator.multiThreadedWrite(this, TARGET_NAME,
        src, logicalBlockAddress, blocks * BLOCK_SIZE);
    write.get();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private final int getLastBlockAddress(final int blocks) {

    return randomGenerator.nextInt((int) lastBlockAddress - blocks + 1);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public static void main(String[] args) throws Exception {

    String path = "./benches/bench3/";
    final int numRuns = 5;
    try {
      // final Benchmark bench = new Benchmark();
      // bench.setLogger(false);
      // bench.add(new InitiatorBench(1));
      // Result r = bench.run(10);
      // RawData rawRes = new RawData(path+"block1/");
      // rawRes.visit(r);
      //
      // final Benchmark bench10 = new Benchmark();
      // bench10.setLogger(false);
      // bench10.add(new InitiatorBench(10));
      // Result r10 = bench10.run(10);
      // RawData rawRes10 = new RawData(path+"block10/");
      // rawRes10.visit(r10);

      for (int f = 1; f <= 10; f++) {
        int numBlocks = f * 100;
        for (int i = 0; i < numRuns; i++) {
          final Benchmark bench1x = new Benchmark();
          InitiatorBench.setNumberOfBlocks(numBlocks);
          bench1x.add(InitiatorBench.class);
          BenchmarkResult r1x = bench1x.run();
          final File outputFolder = new File(path + "perfidixDebug");
          //outputFolder.mkdirs();
          // CSVOutput rawRes1x = new CSVOutput(outputFolder);
          // rawRes1x.visitBenchmark(r1x);
          // final TabularSummaryOutput output = new TabularSummaryOutput();
          // output.visitBenchmark(r1x);
          final PrintStream normalStream = System.out;
          System.setOut(new PrintStream(new FileOutputStream(outputFolder),
              true));
          System.out.println("Bench\n" + r1x);
          System.setOut(normalStream);
          for (final ClassResult classRes : r1x.getIncludedResults()) {
            System.out.println("Class " + classRes.getElementName() + "\n"
                + classRes.toString());
            for (final MethodResult methRes : classRes.getIncludedResults()) {
              System.out.println("Method " + methRes.getElementName() + "\n"
                  + methRes.toString());
            }
          }

        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Setting the number of blocks, static because of class-param in
   * {@link Benchmark#add(Class)}
   * 
   * @param numBlocks
   *          the new number of blocks.
   */
  public final static void setNumberOfBlocks(final int numBlocks) {

    NUMBER_OF_BLOCKS = numBlocks;
  }

}

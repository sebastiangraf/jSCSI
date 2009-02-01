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
import java.nio.ByteBuffer;
import java.util.concurrent.Future;


import org.perfidix.annotation.Bench;
import org.perfidix.Benchmark;
import org.perfidix.annotation.SkipBench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.ouput.CSVOutput;
import org.perfidix.result.BenchmarkResult;

/**
 * <h1>JSCSIBench</h1> <p/> This class is a benchmark to measure the
 * performance of the Java implemented iSCSI Initiator against the OpenISCSI Initiator as a File Device.
 * 
 * @author Patrice Brend'amour
 * @author Bastian Lemke
 */

public class JSCSIBench extends ISCSIBench{

  /** Name of the device name on the iSCSI Target. */
  private static final String TARGET_NAME = "testing-bench4";

  /** the path to store benches. */
  private static final File BENCHPATH = new File("./benches/bench7/jscsi/");

  /**
   * The buffer containing the data, which are read/written from/to the iSCSI
   * Target. This has a size of <code>BLOCK_SIZE</code>.
   */
  protected ByteBuffer buffer;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The <code>Initiator</code> instance to use for measurements with JSCSI. */
  private Initiator initiator;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------



  /** open jscsi connection to the target. */
  @SkipBench
  public void openConnection(){
    buffer = ByteBuffer.allocateDirect(BLOCK_SIZE);
    try {
      initiator = new Initiator(Configuration.create());
      initiator.createSession(TARGET_NAME);
      lastBlockAddress = (initiator.getCapacity(TARGET_NAME) * initiator
          .getBlockSize(TARGET_NAME))
          / BLOCK_SIZE;   
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /** close connection from open-iscsi to the target. */
  @SkipBench
  public void closeConnection(){

    try {
      initiator.closeSession(TARGET_NAME);
    } catch (final Exception e) {
      e.printStackTrace();
    }
  }

  //--------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  @SkipBench
  public final void read(int numBlocks)
  {
    int logicalBlockAddress = getLastBlockAddress(numBlocks);
    for (int i = 0; i < numBlocks; i++) {
      buffer.clear();
      Future<Void> read;
      try {
        read = initiator.multiThreadedRead(this, TARGET_NAME, 
            buffer, logicalBlockAddress, BLOCK_SIZE);
        read.get();
        logicalBlockAddress = getLastBlockAddress(numBlocks);
      } catch (Exception e) {
        e.printStackTrace();
      }

    }
  }
  @SkipBench
  public final void write(int numBlocks)
  {
    buffer.clear();

    int logicalBlockAddress = getLastBlockAddress(numBlocks);
    for (int i = 0; i < numBlocks; i++) {
      try { 
        final Future<Void> write  = initiator.multiThreadedWrite(this, 
            TARGET_NAME, buffer, logicalBlockAddress, BLOCK_SIZE);
        write.get();
        logicalBlockAddress = getLastBlockAddress(numBlocks);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }


  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public static void main(String[] args) throws Exception{

    final Benchmark bench = new Benchmark();
    bench.add(JSCSIBench.class);

    CSVOutput output = new CSVOutput(BENCHPATH);
    final BenchmarkResult res = bench.run(KindOfArrangement.ShuffleArrangement, output);
    output.visitBenchmark(res);

  }
  //--------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void read100()
  {
    read(100);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void read200()
  {
    read(200);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void read300()
  {
    read(300);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void read400()
  {
    read(400);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void read500()
  {
    read(500);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void read600()
  {
    read(600);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void read700()
  {
    read(700);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void read800()
  {
    read(800);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void read900()
  {
    read(900);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void read1000()
  {
    read(1000);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void write100()
  {
    write(100);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void write200()
  {
    write(200);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void write300()
  {
    write(300);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void write400()
  {
    write(400);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void write500()
  {
    write(500);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void write600()
  {
    write(600);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void write700()
  {
    write(700);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void write800()
  {
    write(800);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void write900()
  {
    write(900);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openConnection", afterEachRun = "closeConnection")
  public void write1000()
  {
    write(1000);
  }
}

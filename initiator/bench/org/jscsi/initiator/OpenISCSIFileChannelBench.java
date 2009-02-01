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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;

import org.perfidix.Benchmark;
import org.perfidix.annotation.BeforeBenchClass;
import org.perfidix.annotation.Bench;
import org.perfidix.annotation.SkipBench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.ouput.TabularSummaryOutput;
import org.perfidix.result.BenchmarkResult;

/**
 * <h1>OpenISCSIBench</h1> <p/> This class is a benchmark to measure the
 * performance of the Java implemented iSCSI Initiator against the OpenISCSI Initiator as a File Device.
 * 
 * @author Patrice Brend'amour
 * @author Bastian Lemke
 */

public final class OpenISCSIFileChannelBench extends ISCSIBench {

  /** The Path of the OpenSCSI Device. */
  private static final String OSCSIPATH = "/oscsi/";


  /** The <code>FileOutputStream</code> instance to use for measurements with OpenISCSI. */
  private FileOutputStream oscsiOut;
  /** The <code>FileInputStream</code> instance to use for measurements with OpenISCSI. */
  private FileInputStream oscsiIn;

  /** The Output FileChannel for OpenISCSI Bench. */
  private FileChannel oscsiOutChannel;

  /** The Input FileChannel for OpenISCSI Bench. */
  private FileChannel oscsiInChannel;



  /** the path to store benches. */
  private static final File BENCHPATH = new File("./benches/bench7/openISCSI-FileChannelBench/");
  /**
   * The buffer containing the data, which are read/written from/to the iSCSI
   * Target. This has a size of <code>BLOCK_SIZE</code>.
   */
  protected ByteBuffer buffer;
  
  /**
   * Buffer to Compare with.
   * 
   */
  protected byte[] readControlBuffer;
  
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>OpenISCSIBench</code> instance.
   * @param blocks
   *            number of Blocks to Benchmark
   * @throws Exception
   *           if any error occurs.
   */

  @BeforeBenchClass
  public void makeCompareBuffer() {
    try {
      ByteBuffer buf = ByteBuffer.allocate(BLOCK_SIZE);
      FileInputStream In = new FileInputStream("oscsi-in.dat");
      FileChannel InChannel = In.getChannel();
      InChannel.read(buf);
      readControlBuffer=buf.array();
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** open UNbuffered connection from open-iscsi to the target. */
  @SkipBench
  public void openInConnection() {
    try {
      oscsiIn = new FileInputStream(OSCSIPATH + "oscsi-in.dat");
      oscsiInChannel = oscsiIn.getChannel();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } 

    openConnection();
  }
  /** open UNbuffered connection from open-iscsi to the target. */
  @SkipBench
  public void openOutConnection() {
    try {
      oscsiOut = new FileOutputStream(OSCSIPATH + "oscsi.dat");
      oscsiOutChannel = oscsiOut.getChannel(); 
      openConnection();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /** close connection from open-iscsi to the target.
   */
  @SkipBench
  public void closeInConnection() {

    try {
      oscsiInChannel.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
  /** close connection from open-iscsi to the target.
   */
  @SkipBench
  public void closeOutConnection() {

    try {
      oscsiOutChannel.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }
  @SkipBench
  public void openConnection() {
    buffer = ByteBuffer.allocateDirect(BLOCK_SIZE);
  }
  @SkipBench
  public void closeConnection() {
    //Do something
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  @SkipBench
  public void read(int numBlocks) {
    for (int i = 0; i < numBlocks; i++) {
      buffer.clear();
      try {
        oscsiInChannel.position(0);
        oscsiInChannel.read(buffer);
        assert Arrays.equals(buffer.array(),readControlBuffer);
      } catch (IOException e) {
        e.printStackTrace();
      }

    }

  }


  @SkipBench
  public void write(int numBlocks) {
    for (int i = 0; i < numBlocks; i++) {
      buffer.clear();
      try {
        oscsiOutChannel.write(buffer);
        oscsiOutChannel.force(false);
      } catch (IOException e) {
        e.printStackTrace();
      }

    }
  }

  //--------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Bench(runs = NUMRUNS, beforeEachRun = "openInConnection", afterEachRun = "closeInConnection")
  public void read100()
  {
    read(100);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openInConnection", afterEachRun = "closeInConnection")
  public void read200()
  {
    read(200);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openInConnection", afterEachRun = "closeInConnection")
  public void read300()
  {
    read(300);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openInConnection", afterEachRun = "closeInConnection")
  public void read400()
  {
    read(400);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openInConnection", afterEachRun = "closeInConnection")
  public void read500()
  {
    read(500);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openInConnection", afterEachRun = "closeInConnection")
  public void read600()
  {
    read(600);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openInConnection", afterEachRun = "closeInConnection")
  public void read700()
  {
    read(700);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openInConnection", afterEachRun = "closeInConnection")
  public void read800()
  {
    read(800);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openInConnection", afterEachRun = "closeInConnection")
  public void read900()
  {
    read(900);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openInConnection", afterEachRun = "closeInConnection")
  public void read1000()
  {
    read(1000);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Bench(runs = NUMRUNS, beforeEachRun = "openOutConnection", afterEachRun = "closeOutConnection")
  public void write100()
  {
    write(100);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openOutConnection", afterEachRun = "closeOutConnection")
  public void write200()
  {
    write(200);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openOutConnection", afterEachRun = "closeOutConnection")
  public void write300()
  {
    write(300);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openOutConnection", afterEachRun = "closeOutConnection")
  public void write400()
  {
    write(400);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openOutConnection", afterEachRun = "closeOutConnection")
  public void write500()
  {
    write(500);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openOutConnection", afterEachRun = "closeOutConnection")
  public void write600()
  {
    write(600);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openOutConnection", afterEachRun = "closeOutConnection")
  public void write700()
  {
    write(700);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openOutConnection", afterEachRun = "closeOutConnection")
  public void write800()
  {
    write(800);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openOutConnection", afterEachRun = "closeOutConnection")
  public void write900()
  {
    write(900);
  }

  @Bench(runs = NUMRUNS, beforeEachRun = "openOutConnection", afterEachRun = "closeOutConnection")
  public void write1000()
  {
    write(1000);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public static void main(final String[] args) throws Exception{

    final Benchmark bench = new Benchmark();
    bench.add(OpenISCSIFileChannelBench.class);

//        final CSVOutput output = new CSVOutput(BENCHPATH);
//        final BenchmarkResult res = bench.run(KindOfArrangement.ShuffleArrangement, new TabularSummaryOutput());
//        output.visitBenchmark(res);
    final BenchmarkResult res =
      bench.run(
          KindOfArrangement.ShuffleArrangement,
          new TabularSummaryOutput());
    new TabularSummaryOutput().visitBenchmark(res);


  }

  //--------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}


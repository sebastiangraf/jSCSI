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

import org.perfidix.annotation.Bench;
import org.perfidix.Benchmark;
import org.perfidix.annotation.SkipBench;
import org.perfidix.element.KindOfArrangement;
import org.perfidix.ouput.CSVOutput;
import org.perfidix.result.BenchmarkResult;
import org.perfidix.result.ClassResult;
import org.perfidix.result.MethodResult;

/**
 * <h1>OpenISCSIBench</h1> <p/> This class is a benchmark to measure the
 * performance of the Java implemented iSCSI Initiator against the OpenISCSI Initiator as a File Device.
 * 
 * @author Patrice Brend'amour
 * @author Bastian Lemke
 */

public class OpenISCSIBench extends ISCSIBench{

  /**
   * Library name of the shared native library.
   */
  static {
    System.loadLibrary("OpenISCSIBench");
  }


  /** The Path of the OpenSCSI Device. */
  private static final String OSCSIDEVICE = "/dev/raw1";

  /** the path to store benches. */
  private static final File BENCHPATH = new File("./benches/bench7/openISCSI/");
  /**
   * The buffer containing the data, which are read/written from/to the iSCSI
   * Target. This has a size of <code>BLOCK_SIZE</code>.
   */
  protected byte[] buffer;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** open UNbuffered connection from open-iscsi to the target. */
  @SkipBench
  public void openConnection(){

    openDevice(OSCSIDEVICE, false);
    buffer = new byte[BLOCK_SIZE];
    randomGenerator.nextBytes(buffer);
  }

  /** close connection from open-iscsi to the target.
   */
  @SkipBench
  public void closeConnection(){

    closeDevice();
  }



  //---------------------------------------------------------------------------
  // ----- native methods --------------------------------------------
  // ---------------------------------------------------------------------------

  /**
   * Opens a connection to a device.
   * 
   * @param deviceName
   *          The device filename of the mounted iSCSI target via Open-iSCSI.
   * @param buffered
   *          <code>true</code> if the device is buffered or not (by-passing
   *          any buffer).
   */
  private native void openDevice(final String deviceName, final boolean buffered);

  /**
   * Closes an open connection to a device.
   */
  private native void closeDevice();


  /**
   * Reads the given data buffer from the device, starting at position
   * <code>address</code>.
   * 
   * @param address
   *          Offset (logical block address) where to start reading data. This
   *          is typically a multiple of <code>512 bytes</code>.
   * @param data
   *          Data buffer to store the read data.
   * @param copyToBuffer
   *          <code>true</code> if the read buffer should be copied in the
   *          given buffer <code>data</code>. If not,
   *          <code>copyToBuffer</code> must be set to <code>false</code>.
   */
  private native void nativeReadBlock(final long address, 
      final byte[] data, final boolean copyToBuffer) throws Exception;

  /**
   * Writes the given data buffer to the device, starting at position
   * <code>address</code>.
   * 
   * @param address
   *          Offset (logical block address) where to start writing data. This
   *          is typically a multiple of <code>512 bytes</code>.
   * @param data
   *          Data buffer to write data from.
   */
  private native void nativeWriteBlock(final long address, final byte[] data) throws Exception;


  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @SkipBench
  public final void read(int numBlocks)
  {
    int logicalBlockAddress = getLastBlockAddress(numBlocks);
    for (int i = 0; i < numBlocks; i++) {
      try {
        nativeReadBlock(logicalBlockAddress,buffer,true);
      } catch (Exception e) {
        e.printStackTrace();
      }
      logicalBlockAddress = getLastBlockAddress(numBlocks);
    }

  }


  @SkipBench
  public final void write(int numBlocks)
  {
    int logicalBlockAddress = getLastBlockAddress(numBlocks);
    for (int i = 0; i < numBlocks; i++) {
      try {
        nativeWriteBlock(logicalBlockAddress,buffer);
      } catch (Exception e) {
        e.printStackTrace();
      }
      randomGenerator.nextBytes(buffer);
      logicalBlockAddress = getLastBlockAddress(numBlocks);
    }
  }


  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public static void main(String[] args) throws Exception{

    final Benchmark bench = new Benchmark();
    bench.add(OpenISCSIBench.class);

//    CSVOutput output = new CSVOutput(BENCHPATH);
//    final BenchmarkResult res = bench.run(KindOfArrangement.NoArrangement, output);
//    output.visitBenchmark(res);
    //DEBUG
    final BenchmarkResult res = bench.run();
    final File outputFolder = new File("./benches/bench7/openISCSI/");
    outputFolder.mkdirs();
    final PrintStream normalStream = System.out;
    System.setOut(new PrintStream(new FileOutputStream(outputFolder + "perfidixDebug"),
        true));
    System.out.println("Bench\n" + res);
    System.setOut(normalStream);
    for (final ClassResult classRes : res.getIncludedResults()) {
      System.out.println("Class " + classRes.getElementName() + "\n"
          + classRes.toString());
      for (final MethodResult methRes : classRes.getIncludedResults()) {
        System.out.println("Method " + methRes.getElementName() + "\n"
            + methRes.toString());
      }
    }

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

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  
}

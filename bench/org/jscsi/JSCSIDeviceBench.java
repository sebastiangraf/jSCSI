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
 * $Id: JSCSIDeviceBench.java 2526 2007-03-08 15:06:11Z lemke $
 * 
 */

package org.jscsi;

import java.io.IOException;
import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

import de.unikn.inf.disy.idefix.perfidix.Benchmark;
import de.unikn.inf.disy.idefix.perfidix.Benchmarkable;
import de.unikn.inf.disy.idefix.perfidix.Result;

/**
 * <h1>JSCSIDeviceBench</h1>
 * <p/>
 * 
 * Benchmark to compare the jSCSI Device with Raid1 Device.
 * 
 * @author Bastian Lemke
 */
public class JSCSIDeviceBench extends Benchmarkable {

  private static String TARGET = "titan04";

  private static final int BLOCK_SIZE = 4096;

  private static final long ADDRESS = 0;

  /** Size (in blocks) of the data to used for sending. */
  private static final int TEST_DATA_SIZE =25600;

  private static final int RUNS = 1000;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private Device device;

  private static Logger logger = Logger.getLogger(DeviceBench.class);

  private static String log;

  /** The random number generator to fill the buffer to send. */
  private final Random randomGenerator;

  private static int benchCounter = 0;

  /** This array contains the data. */
  private final byte[] testData;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public JSCSIDeviceBench() throws Exception{

    randomGenerator = new Random(System.currentTimeMillis());
    testData = new byte[TEST_DATA_SIZE * BLOCK_SIZE];
    randomGenerator.nextBytes(testData);
    logger.setLevel(Level.INFO);
    logger.addAppender(new ConsoleAppender(new PatternLayout()));
    device = new JSCSIDevice(TARGET);
    device.open();
  }

  public final void setUp() {

    try {
      benchCounter++;
      log = benchCounter + " : ";
      log += "JSCSIDevice - ";
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public final void benchWrite_JSCSIDevice() throws Exception {

    device.write(ADDRESS, testData);
    logger.info(log + "benchWrite_JSCSIDevice() finished.");
  }

  public final void _benchRead_JSCSIDevice() throws Exception {

    device.read(ADDRESS, testData);
    logger.info(log + "benchRead_JSCSIDevice() finished.");
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public static void main(String[] args) throws InstantiationException,
      IllegalAccessException, IOException {

    final Benchmark benchMark = new Benchmark();
    benchMark.add(JSCSIDeviceBench.class);
    benchMark.useMilliMeter();

    final Result benchMarkResult = benchMark.run(RUNS);
    logger.info(benchMarkResult);
    logger.info("transferred blocks: " + TEST_DATA_SIZE);
  }
}

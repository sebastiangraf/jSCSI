/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * JSCSIDeviceBench.java 2526 2007-03-08 15:06:11Z lemke $
 */

package org.jscsi.initiator.devices;

import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.perfidix.annotation.AfterBenchClass;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.BeforeBenchClass;
import org.perfidix.annotation.Bench;
import org.perfidix.annotation.BenchClass;
import org.perfidix.annotation.SkipBench;

/**
 * <h1>JSCSIDeviceBench</h1>
 * <p/>
 * 
 * @author Bastian Lemke
 */
@BenchClass(runs = 10)
public class JSCSIDeviceBench {

  private static final String TARGET = "titan04";

  private static final int BLOCK_SIZE = 4096;

  /** Address to start read/write from. */
  private static final long START_ADDRESS = 0;

  /** Size (in blocks) to read/write from/to the target(s). */
  private static final int TEST_DATA_SIZE = 10;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private final Device device;

  private final Logger logger = Logger.getLogger(JSCSIDeviceBench.class);

  /** The random number generator to fill the buffer to send. */
  private final Random randomGenerator;

  private int benchCounter = 0;

  /** This array contains the data. */
  private final byte[] testData;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public JSCSIDeviceBench() throws Exception {

    randomGenerator = new Random(System.currentTimeMillis());
    testData = new byte[TEST_DATA_SIZE * BLOCK_SIZE];
    randomGenerator.nextBytes(testData);
    device = new JSCSIDevice(TARGET);

    logger.setLevel(Level.ALL);
    logger.addAppender(new ConsoleAppender(new PatternLayout()));
  }

  @BeforeBenchClass
  public final void setUp() throws Exception {

    device.open();
    logger.debug("Device " + device.getName() + " opened.");
  }

  @AfterBenchClass
  public final void tearDown() throws Exception {

    String deviceName = device.getName();
    device.close();
    logger.debug("Device " + deviceName + " closed.");
  }

  @AfterEachRun
  public final void increaseBenchCounter() {

    benchCounter++;
  }

  @SkipBench
  public final void setUpWrite() {

    logger.info(benchCounter + ": started to write " + TEST_DATA_SIZE
        * BLOCK_SIZE + " bytes to " + device.getName() + " - " + START_ADDRESS
        + ".");
  }

  @SkipBench
  public final void setUpRead() {

    logger.info(benchCounter + ": started to read " + TEST_DATA_SIZE
        * BLOCK_SIZE + " bytes from " + device.getName() + " - "
        + START_ADDRESS + ".");
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Bench(beforeEachRun = "setUpWrite")
  public final void write_JSCSIDevice() throws Exception {

    device.write(START_ADDRESS, testData);

  }

  @Bench(beforeEachRun = "setUpRead")
  public final void read_JSCSIDevice() throws Exception {

    device.read(START_ADDRESS, testData);
  }
}

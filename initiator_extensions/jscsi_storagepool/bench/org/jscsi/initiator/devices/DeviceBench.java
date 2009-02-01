/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id$
 */

package org.jscsi.initiator.devices;

import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.perfidix.annotation.AfterEachRun;
import org.perfidix.annotation.Bench;
import org.perfidix.annotation.BenchClass;
import org.perfidix.annotation.SkipBench;

/**
 * <h1>DeviceBench</h1>
 * <p/>
 * Benchmark to compare the jSCSI Device with Raid1 and Raid0 Devices.
 * 
 * @author Bastian Lemke
 */
@BenchClass(runs = 10)
public class DeviceBench {

  private static final String[] TARGET_NAMES = { "titan04", "titan05",
      "titan06", "titan07", "titan08" };

  private static final int BLOCK_SIZE = 8192;

  /** Address to start read/write from. */
  private static final long START_ADDRESS = 0;

  /** Size (in blocks) to read/write from/to the target(s). */
  private static final int TEST_DATA_SIZE = 1000;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private Device device = null;

  private WriteBufferDevice bufferDevice = null;

  private final Logger logger = Logger.getLogger(DeviceBench.class);

  /** The random number generator to fill the buffer to send. */
  private final Random randomGenerator;

  private int benchCounter = 0;

  /** This array contains the data. */
  private final byte[] testData;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public DeviceBench() {

    // create the test data
    randomGenerator = new Random(System.currentTimeMillis());
    testData = new byte[TEST_DATA_SIZE * BLOCK_SIZE];
    randomGenerator.nextBytes(testData);

    logger.setLevel(Level.ALL);
    logger.addAppender(new ConsoleAppender(new PatternLayout()));
  }

  @AfterEachRun
  public final void tearDownMethod() throws Exception {

    if (device != null) {
      String deviceName = device.getName();
      device.close();
      logger.debug("Device " + deviceName + " closed.");
      device = null;
    } else if (bufferDevice != null) {
      String deviceName = bufferDevice.getName();
      bufferDevice.close();
      logger.debug("Device " + deviceName + " closed.");
      bufferDevice = null;
    }
  }

  @AfterEachRun
  public final void increaseBenchCounter() {

    benchCounter++;
  }

  @SkipBench
  public final void setUpJSCSIDevice() throws Exception {

    device = new JSCSIDevice(TARGET_NAMES[0]);
    device.open();
    logger.debug("Device " + device.getName() + " opened.");
  }

  @SkipBench
  public final void setUpRaid1Device_4_Targets() throws Exception {

    device = new Raid1Device(new Device[] { new JSCSIDevice(TARGET_NAMES[0]),
        new JSCSIDevice(TARGET_NAMES[1]), new JSCSIDevice(TARGET_NAMES[2]),
        new JSCSIDevice(TARGET_NAMES[3]) });
    device.open();
    logger.debug("Device " + device.getName() + " opened.");
  }

  @SkipBench
  public final void setUpRaid0Device_4_Targets() throws Exception {

    device = new Raid0Device(new Device[] { new JSCSIDevice(TARGET_NAMES[0]),
        new JSCSIDevice(TARGET_NAMES[1]), new JSCSIDevice(TARGET_NAMES[2]),
        new JSCSIDevice(TARGET_NAMES[3]) });
    device.open();
    logger.debug("Device " + device.getName() + " opened.");
  }

  @SkipBench
  public final void setUpJSCSIDevice_WriteBuffer() throws Exception {

    bufferDevice = new WriteBufferDevice(new JSCSIDevice(TARGET_NAMES[0]));
    bufferDevice.open();
    logger.debug("Device " + bufferDevice.getName() + " opened.");
  }

  @SkipBench
  public final void setUpJSCSIDevice_Prefetcher() throws Exception {

    device = new PrefetchDevice(new JSCSIDevice(TARGET_NAMES[0]));
    device.open();
    logger.debug("Device " + device.getName() + " opened.");
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Bench(beforeFirstRun = "setUpJSCSIDevice")
  public final void write_JSCSIDevice() throws Exception {

    device.write(START_ADDRESS, testData);
    logger.info(benchCounter + ": write_JSCSIDevice() finished.");
  }

  @Bench(beforeFirstRun = "setUpJSCSIDevice_WriteBuffer")
  public final void write_JSCSIDevice_WriteBuffer() throws Exception {

    bufferDevice.write(START_ADDRESS, testData);
    bufferDevice.flush();
    logger.info(benchCounter + ": write_JSCSIDevice_WriteBuffer() finished.");
  }

  @Bench(beforeFirstRun = "setUpRaid1Device_4_Targets")
  public final void write_Raid1Device_4_Targets() throws Exception {

    device.write(START_ADDRESS, testData);
    logger.info(benchCounter + ": write_Raid1Device_4_Targets() finished.");
  }

  @Bench(beforeFirstRun = "setUpRaid0Device_4_Targets")
  public final void write_Raid0Device_4_Targets() throws Exception {

    device.write(START_ADDRESS, testData);
    logger.info(benchCounter + ": write_Raid0Device_4_Targets() finished.");
  }

  @Bench(beforeFirstRun = "setUpJSCSIDevice")
  public final void benchRead_JSCSIDevice() throws Exception {

    device.read(START_ADDRESS, testData);
    logger.info(benchCounter + ": benchRead_JSCSIDevice() finished.");
  }

  @Bench(beforeFirstRun = "setUpJSCSIDevice_Prefetcher")
  public final void read_JSCSIDevice_Prefetcher() throws Exception {

    device.write(START_ADDRESS, testData);
    logger.info(benchCounter + ": read_JSCSIDevice_Prefetcher() finished.");
  }

  @Bench(beforeFirstRun = "setUpRaid1Device_4_Targets")
  public final void read_Raid1Device_4_Targets() throws Exception {

    device.read(START_ADDRESS, testData);
    logger.info(benchCounter + ": read_Raid1Device_4_Targets() finished.");
  }

  @Bench(beforeFirstRun = "setUpRaid0Device_4_Targets")
  public final void read_Raid0Device_4_Targets() throws Exception {

    device.read(START_ADDRESS, testData);
    logger.info(benchCounter + ": read_Raid0Device_4_Targets() finished.");
  }
}

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
 * $Id$
 * 
 */

package org.jscsi;

import java.util.Random;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.perfidix.Bench;

/**
 * <h1>DeviceBench</h1>
 * <p/>
 * 
 * Benchmark to compare the jSCSI Device with Raid1 Device.
 * 
 * @author Bastian Lemke
 */
public class DeviceBench {

  private static final String[] TARGET_NAMES =
      { "titan04", "titan05", "titan06", "titan07", "titan08" };

  private static final int BLOCK_SIZE = 8192;

  private static final long START_ADDRESS = 0;

  /** Size (in blocks) of the data to used for sending. */
  private static final int TEST_DATA_SIZE = 100;

  private static final int RUNS = 1;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private Device device = null;

  private static Logger logger = Logger.getLogger(DeviceBench.class);

  private static String log;

  /** The random number generator to fill the buffer to send. */
  private final Random randomGenerator;

  private static int benchCounter = 0;

  private static int methodCounter = 0;

  /** This array contains the data. */
  private final byte[] testData;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  public DeviceBench() {

    randomGenerator = new Random(System.currentTimeMillis());
    testData = new byte[TEST_DATA_SIZE * BLOCK_SIZE];
    randomGenerator.nextBytes(testData);
    logger.setLevel(Level.INFO);
    logger.addAppender(new ConsoleAppender(new PatternLayout()));
  }

  public final void tearDownMethod() throws Exception {

    if (methodCounter == RUNS) {
      methodCounter = 0;
      String deviceName = device.getName();
      device.close();
      logger.debug("Device " + deviceName + " closed.");
      device = null;
    }
    benchCounter++;
  }

  public final void setUpJSCSIDevice() throws Exception {
    if (device == null) {
      device = new JSCSIDevice(TARGET_NAMES[0]);
      device.open();
    }
    methodCounter++;
    log = benchCounter + " : " + "JSCSIDevice - ";
  }

  public final void setUpRaid1Device_4_Targets() throws Exception {
    if (device == null) {
      device =
          new Raid1Device(new Device[] {
              new JSCSIDevice(TARGET_NAMES[0]),
              new JSCSIDevice(TARGET_NAMES[1]),
              new JSCSIDevice(TARGET_NAMES[2]),
              new JSCSIDevice(TARGET_NAMES[3]) });
      device.open();
    }
    methodCounter++;
    log = benchCounter + " : " + "Raid1Device (4 Targets) - ";
  }

  public final void setUpRaid0Device_4_Targets() throws Exception {
    if (device == null) {
      device =
          new Raid0Device(new Device[] {
              new JSCSIDevice(TARGET_NAMES[0]),
              new JSCSIDevice(TARGET_NAMES[1]),
              new JSCSIDevice(TARGET_NAMES[2]),
              new JSCSIDevice(TARGET_NAMES[3]) });
      device.open();
    }
    methodCounter++;
    log = benchCounter + " : " + "Raid0Device (4 Targets) - ";
  }

  public final void setUpJSCSIDevice_WriteBuffer() throws Exception {
    if (device == null) {
      device = new WriteBufferDevice(new JSCSIDevice(TARGET_NAMES[0]));
      device.open();
    }
    methodCounter++;
    log = benchCounter + " : " + "JSCSIDevice (WriteBuffer) - ";
  }
  
  public final void setUpJSCSIDevice_Prefetcher() throws Exception {
    if (device == null) {
      device = new PrefetchDevice(new JSCSIDevice(TARGET_NAMES[0]));
      device.open();
    }
    methodCounter++;
    log = benchCounter + " : " + "JSCSIDevice (Prefetcher) - ";
  }  

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Bench(beforeEveryBenchRun = "setUpJSCSIDevice", afterEveryBenchRun = "tearDownMethod", runs = RUNS)
  public final void write_JSCSIDevice() throws Exception {

    device.write(START_ADDRESS, testData);
    logger.info(log + "write_JSCSIDevice() finished.");
  }
  
  @Bench(beforeEveryBenchRun = "setUpJSCSIDevice_WriteBuffer", afterEveryBenchRun = "tearDownMethod", runs = RUNS)
  public final void write_JSCSIDevice_WriteBuffer() throws Exception {

    device.write(START_ADDRESS, testData);
    logger.info(log + "write_JSCSIDevice_WriteBuffer() finished.");
  }

  @Bench(beforeEveryBenchRun = "setUpRaid1Device_4_Targets", afterEveryBenchRun = "tearDownMethod", runs = RUNS)
  public final void write_Raid1Device_4_Targets() throws Exception {

    device.write(START_ADDRESS, testData);
    logger.info(log + "write_Raid1Device_4_Targets() finished.");
  }

  @Bench(beforeEveryBenchRun = "setUpRaid0Device_4_Targets", afterEveryBenchRun = "tearDownMethod", runs = RUNS)
  public final void write_Raid0Device_4_Targets() throws Exception {

    device.write(START_ADDRESS, testData);
    logger.info(log + "write_Raid0Device_4_Targets() finished.");
  }

  @Bench(beforeEveryBenchRun = "setUpJSCSIDevice", afterEveryBenchRun = "tearDownMethod", runs = RUNS)
  public final void benchRead_JSCSIDevice() throws Exception {

    device.read(START_ADDRESS, testData);
    logger.info(log + "benchRead_JSCSIDevice() finished.");
  }
  
  @Bench(beforeEveryBenchRun = "setUpJSCSIDevice_Prefetcher", afterEveryBenchRun = "tearDownMethod", runs = RUNS)
  public final void read_JSCSIDevice_Prefetcher() throws Exception {

    device.write(START_ADDRESS, testData);
    logger.info(log + "read_JSCSIDevice_Prefetcher() finished.");
  }

  @Bench(beforeEveryBenchRun = "setUpRaid1Device_4_Targets", afterEveryBenchRun = "tearDownMethod", runs = RUNS)
  public final void read_Raid1Device_4_Targets() throws Exception {

    device.read(START_ADDRESS, testData);
    logger.info(log + "read_Raid1Device_4_Targets() finished.");
  }

  @Bench(beforeEveryBenchRun = "setUpRaid0Device_4_Targets", afterEveryBenchRun = "tearDownMethod", runs = RUNS)
  public final void read_Raid0Device_4_Targets() throws Exception {

    device.read(START_ADDRESS, testData);
    logger.info(log + "read_Raid0Device_4_Targets() finished.");
  }
}

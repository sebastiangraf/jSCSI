// TODO: move to Perfidix 2




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
// * $Id$
// * 
// */
//
//package org.jscsi;
//
//import java.io.IOException;
//import java.util.Random;
//
//import org.apache.log4j.ConsoleAppender;
//import org.apache.log4j.Level;
//import org.apache.log4j.Logger;
//import org.apache.log4j.PatternLayout;
//
//import de.unikn.inf.disy.idefix.perfidix.Benchmark;
//import de.unikn.inf.disy.idefix.perfidix.Benchmarkable;
//import de.unikn.inf.disy.idefix.perfidix.Result;
//
///**
// * <h1>DeviceBench</h1>
// * <p/>
// * 
// * Benchmark to compare the jSCSI Device with Raid1 Device.
// * 
// * @author Bastian Lemke
// */
//public class DeviceBench extends Benchmarkable {
//
//  private static String TARGET = "titan04";
//
//  private static String[] TWO_TARGETS = { "titan04", "titan05" };
//
//  private static String[] THREE_TARGETS = { "titan04", "titan05", "titan06" };
//
//  private static String[] FOUR_TARGETS = { "titan04", "titan05", "titan06",
//      "titan07" };
//
//  private static String[] FIVE_TARGETS = { "titan04", "titan05", "titan06",
//      "titan07", "titan08" };
//
//  private static final int BLOCK_SIZE = 4096;
//
//  private static final long ADDRESS = 0;
//
//  /** Size (in blocks) of the data to used for sending. */
//  private static final int TEST_DATA_SIZE = 10000;
//
//  private static final int RUNS = 3;
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  private Device device;
//
//  private static Logger logger = Logger.getLogger(DeviceBench.class);
//
//  private static String log;
//
//  /** The random number generator to fill the buffer to send. */
//  private final Random randomGenerator;
//
//  private static int benchCounter = 0;
//
//  /** This array contains the data. */
//  private final byte[] testData;
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  public DeviceBench() {
//
//    randomGenerator = new Random(System.currentTimeMillis());
//    testData = new byte[TEST_DATA_SIZE * BLOCK_SIZE];
//    randomGenerator.nextBytes(testData);
//    logger.setLevel(Level.INFO);
//    logger.addAppender(new ConsoleAppender(new PatternLayout()));
//  }
//
//  /**
//   * Setting up the Device for the Bench-Methods. The Bench-Methods MUST be in
//   * the following order: -----------------------------------------------------
//   * 1. Read/Write JSCSIDevice, -----------------------------------------------
//   * 2. Read/Write Raid1Device (2 Targets), -----------------------------------
//   * 3. Read/Write Raid1Device (3 Targets), -----------------------------------
//   * 4. Read/Write Raid1Device (4 Targets), -----------------------------------
//   * 5. Read/Write Raid1Device (5 Targets), -----------------------------------
//   * 6. Read/Write JSCSIDevice, -----------------------------------------------
//   * 7. Read/Write Raid1Device (2 Targets), -----------------------------------
//   * 8. Read/Write Raid1Device (3 Targets), -----------------------------------
//   * 9. Read/Write Raid1Device (4 Targets), -----------------------------------
//   * 10. Read/Write Raid1Device (5 Targets), ----------------------------------
//   * ...
//   */
//  public final void setUp() {
//
//    try {
//      log = benchCounter + " : ";
//      if (benchCounter < RUNS * 1) {
//        device = new JSCSIDevice(TARGET);
//        log += "JSCSIDevice - ";
//      } else if (benchCounter < RUNS * 2) {
//        device = new Raid1Device(TWO_TARGETS);
//        log += "Raid1Device (2 Targets) - ";
//      } else if (benchCounter < RUNS * 3) {
//        device = new Raid1Device(THREE_TARGETS);
//        log += "Raid1Device (3 Targets) - ";
//      } else if (benchCounter < RUNS * 4) {
//        device = new Raid1Device(FOUR_TARGETS);
//        log += "Raid1Device (4 Targets) - ";
//      } else if (benchCounter < RUNS * 5) {
//        device = new Raid1Device(FIVE_TARGETS);
//        log += "Raid1Device (5 Targets) - ";
//      }
//      device.open();
//      logger.debug("Device " + device.getName() + " opened.");
//      benchCounter++;
//      if (benchCounter >= RUNS * 5) {
//        benchCounter = 0;
//      }
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//
//  public final void tearDown() {
//
//    try {
//      String deviceName = device.getName();
//      device.close();
//      logger.debug("Device " + deviceName + " closed.");
//      device = null;
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//  }
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  public final void benchWrite_JSCSIDevice() throws Exception {
//
//    device.write(ADDRESS, testData);
//    logger.info(log + "benchWrite_JSCSIDevice() finished.");
//  }
//
//  public final void benchWrite_Raid1Device_2_Targets() throws Exception {
//
//    device.write(ADDRESS, testData);
//    logger.info(log + "benchWrite_Raid1Device_2_Targets() finished.");
//  }
//
//  public final void benchWrite_Raid1Device_3_Targets() throws Exception {
//
//    device.write(ADDRESS, testData);
//    logger.info(log + "benchWrite_Raid1Device_3_Targets() finished.");
//  }
//
//  public final void benchWrite_Raid1Device_4_Targets() throws Exception {
//
//    device.write(ADDRESS, testData);
//    logger.info(log + "benchWrite_Raid1Device_4_Targets() finished.");
//  }
//
//  public final void benchWrite_Raid1Device_5_Targets() throws Exception {
//
//    device.write(ADDRESS, testData);
//    logger.info(log + "benchWrite_Raid1Device_5_Targets() finished.");
//  }
//
//  public final void benchRead_JSCSIDevice() throws Exception {
//
//    device.read(ADDRESS, testData);
//    logger.info(log + "benchRead_JSCSIDevice() finished.");
//  }
//
//  public final void benchRead_Raid1Device_2_Targets() throws Exception {
//
//    device.read(ADDRESS, testData);
//    logger.info(log + "benchRead_Raid1Device_2_Targets() finished.");
//  }
//
//  public final void benchRead_Raid1Device_3_Targets() throws Exception {
//
//    device.read(ADDRESS, testData);
//    logger.info(log + "benchRead_Raid1Device_3_Targets() finished.");
//  }
//
//  public final void benchRead_Raid1Device_4_Targets() throws Exception {
//
//    device.read(ADDRESS, testData);
//    logger.info(log + "benchRead_Raid1Device_4_Targets() finished.");
//  }
//
//  public final void benchRead_Raid1Device_5_Targets() throws Exception {
//
//    device.read(ADDRESS, testData);
//    logger.info(log + "benchRead_Raid1Device_5_Targets() finished.");
//  }
//
//  // --------------------------------------------------------------------------
//  // --------------------------------------------------------------------------
//
//  public static void main(String[] args) throws InstantiationException,
//      IllegalAccessException, IOException {
//
//    final Benchmark benchMark = new Benchmark();
//    benchMark.add(DeviceBench.class);
//    benchMark.useMilliMeter();
//
//    final Result benchMarkResult = benchMark.run(RUNS);
//    logger.info(benchMarkResult);
//    logger.info("transferred blocks: " + TEST_DATA_SIZE);
//  }
//}

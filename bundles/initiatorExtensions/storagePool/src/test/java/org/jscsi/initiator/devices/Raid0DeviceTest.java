/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * Raid1DeviceTest.java 2641 2007-04-10 09:46:28Z lemke $
 */

package org.jscsi.initiator.devices;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.jscsi.initiator.devices.Device;
import org.jscsi.initiator.devices.JSCSIDevice;
import org.jscsi.initiator.devices.Raid0Device;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class Raid0DeviceTest {

  /** previous targetNames - "disk5", "disk6" */
  private static final String[] targetNames = { "testing-xen2-disk1",
      "testing-xen2-disk2" };

  private static final Device[] jSCSIDevices = new Device[targetNames.length];

  /** Number of Blocks to write */
  private static final int TEST_DATA_SIZE = 320;

  private static byte[] testData;

  private static long address;

  private static Device device;

  private static Random randomGenerator;

  public Raid0DeviceTest() throws Exception {

    for (int i = 0; i < targetNames.length; i++) {
      jSCSIDevices[i] = new JSCSIDevice(targetNames[i]);
    }
  }

  @Before
  public final void setUp() throws Exception {

    device = new Raid0Device(jSCSIDevices);
    device.open();
    randomGenerator = new Random(System.currentTimeMillis());
    testData = new byte[TEST_DATA_SIZE * device.getBlockSize()];
    randomGenerator.nextBytes(testData);
  }

  @After
  public final void tearDown() throws Exception {

    device.close();
  }

  /**
   * Test write on the beginning of a Device.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public final void testReadWriteBeginning() throws Exception {

    address = 0;

    device.write(address, testData);
    byte[] result = new byte[TEST_DATA_SIZE * device.getBlockSize()];
    device.read(address, result);
    for (int i = 0; i < TEST_DATA_SIZE * device.getBlockSize(); i++) {
      assertEquals(result[i], testData[i]);
    }
  }

  /**
   * Tests, if an exception is thrown, when the requested address is less than
   * 0.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testWriteAddressToLow() throws Exception {

    address = -1;
    device.write(address, testData);
  }

  /**
   * Tests, if an exception is thrown, when the requested address is less than
   * 0.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testReadAddressToLow() throws Exception {

    address = -1;
    device.read(address, testData);
  }

  /**
   * Tests, if an exception is thrown, when data would exceed the maximum
   * available address.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testWriteOverTheEnd() throws Exception {

    address = device.getBlockCount() - TEST_DATA_SIZE;
    byte[] data = new byte[TEST_DATA_SIZE * device.getBlockSize() * 2];
    randomGenerator.nextBytes(data);
    device.write(address, data);
  }

  /**
   * Tests, if an exception is thrown, when data would exceed the maximum
   * available address.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testReadOverTheEnd() throws Exception {

    address = device.getBlockCount() - TEST_DATA_SIZE;
    byte[] result = new byte[TEST_DATA_SIZE * device.getBlockSize() * 2];
    device.read(address, result);
  }

  /**
   * Test write on the end of a Device.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public final void testReadWriteEnd() throws Exception {

    address = device.getBlockCount() - TEST_DATA_SIZE;
    device.write(address, testData);
    byte[] result = new byte[TEST_DATA_SIZE * device.getBlockSize()];
    device.read(address, result);
    for (int i = 0; i < TEST_DATA_SIZE; i++) {
      assertEquals(result[i], testData[i]);
    }
  }

  /**
   * Tests, if an exception is thrown, when the requested address higher than
   * the highest available address.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testWriteAddressToHigh() throws Exception {

    address = device.getBlockCount() - TEST_DATA_SIZE + 1;
    device.write(address, testData);
  }

  /**
   * Tests, if an exception is thrown, when the requested address higher than
   * the highest available address.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testReadAddressToHigh() throws Exception {

    address = device.getBlockCount() - TEST_DATA_SIZE + 1;
    device.read(address, testData);
  }

  /**
   * Tests, if an exception is thrown, when data isn't a multiple of blockSize.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testWriteIllegalSize() throws Exception {

    address = 0;
    byte[] data = new byte[1];
    randomGenerator.nextBytes(data);
    device.write(address, data);
  }

  /**
   * Tests, if an exception is thrown, when data isn't a multiple of blockSize.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test(expected = IllegalArgumentException.class)
  public void testReadIllegalSize() throws Exception {

    address = 0;
    byte[] result = new byte[1];
    device.read(address, result);
  }

  /**
   * Random write and read.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public void testRandomReadWrite() throws Exception {

    address = randomGenerator.nextInt((int) device.getBlockCount());
    device.write(address, testData);
    final byte[] result = new byte[TEST_DATA_SIZE * device.getBlockSize()];

    device.read(address, result);
    for (int i = 0; i < TEST_DATA_SIZE * device.getBlockSize(); i++) {
      assertEquals(result[i], testData[i]);
    }
  }

  /**
   * Tests, if an exception is thrown, when data is written to an unopened Raid0
   * Device.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test(expected = IllegalStateException.class)
  public void testWriteNotOpened() throws Exception {

    Device newDevice = new Raid0Device(jSCSIDevices);
    address = 0;
    newDevice.write(address, testData);
  }

  /**
   * Tests, if an exception is thrown, when data is read from an unopened Raid0
   * Device.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test(expected = IllegalStateException.class)
  public void testReadNotOpened() throws Exception {

    Device newDevice = new Raid0Device(jSCSIDevices);
    address = 0;
    byte[] result = new byte[TEST_DATA_SIZE * device.getBlockSize()];
    newDevice.read(address, result);
  }
}

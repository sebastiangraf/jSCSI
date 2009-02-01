/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * JSCSIDeviceTest.java 2641 2007-04-10 09:46:28Z lemke $
 */

package org.jscsi.initiator.devices;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.jscsi.initiator.devices.JSCSIDevice;
import org.jscsi.initiator.devices.PrefetchDevice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PrefetchDeviceTest {

  /** previous Target_Name - "disk6" */
  private static final String TARGET_NAME = "testing-xen2-disk1";

  private static PrefetchDevice device;

  /* Byte arrays (for all blocksizes) which are written to the targets. */
  private final byte[] testDataBlock8kb;

  private final byte[] testDataBlock128kb;

  Random randomGenerator;

  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------

  public PrefetchDeviceTest() {

    // create byte arrays with random values for all blocksizes.
    randomGenerator = new Random(System.currentTimeMillis());
    testDataBlock8kb = new byte[8 * 1024];
    randomGenerator.nextBytes(testDataBlock8kb);
    testDataBlock128kb = new byte[128 * 1024];
    randomGenerator.nextBytes(testDataBlock128kb);
  }

  @Before
  public final void setUp() throws Exception {

    device = new PrefetchDevice(new JSCSIDevice(TARGET_NAME));
    device.open();
  }

  @After
  public final void tearDown() throws Exception {

    device.close();
  }

  /**
   * Test prefetching with 8kb blocks.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public final void testReadWrite8kb() throws Exception {

    int prefetchLength = device.getPrefetchLength() + 2;
    int blockFactor = testDataBlock8kb.length / device.getBlockSize();
    long address = 0;

    for (int i = 0; i < prefetchLength; i++) {
      device.write((address + i * blockFactor), testDataBlock8kb);
    }
    for (int j = 0; j < prefetchLength; j++) {
      byte[] result = new byte[testDataBlock8kb.length];
      device.read((address + j * blockFactor), result);
      for (int i = 0; i < testDataBlock8kb.length; i++) {
        assertEquals(testDataBlock8kb[i], result[i]);
      }
    }
  }

  /**
   * Test reverse prefetching with 8kb blocks.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public final void testReverseReadWrite8kb() throws Exception {

    int prefetchLength = device.getPrefetchLength() + 2;
    int blockFactor = testDataBlock8kb.length / device.getBlockSize();
    long address = (prefetchLength - 1) * (blockFactor);

    for (int i = 0; i < prefetchLength; i++) {
      device.write((address - i * blockFactor), testDataBlock8kb);
    }
    for (int j = 0; j < prefetchLength; j++) {
      byte[] result = new byte[testDataBlock8kb.length];
      device.read((address - j * blockFactor), result);
      for (int i = 0; i < testDataBlock8kb.length; i++) {
        assertEquals(testDataBlock8kb[i], result[i]);
      }
    }
  }

  /**
   * Test prefetching with 128kb blocks.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public final void testReadWriteBeginning128kb() throws Exception {

    int prefetchLength = device.getPrefetchLength();
    int blockFactor = testDataBlock128kb.length / device.getBlockSize();
    long address = 0;

    for (int i = 0; i < prefetchLength; i++) {
      device.write(address + i * blockFactor, testDataBlock128kb);
    }
    for (int j = 0; j < prefetchLength; j++) {
      byte[] result = new byte[testDataBlock128kb.length];
      device.read(address + j * blockFactor, result);
      for (int i = 0; i < testDataBlock128kb.length; i++) {
        assertEquals(result[i], testDataBlock128kb[i]);
      }
    }
  }

  /**
   * Test reverse prefetching with 128kb blocks.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public final void testReverseReadWrite128kb() throws Exception {

    int prefetchLength = device.getPrefetchLength() + 2;
    int blockFactor = testDataBlock128kb.length / device.getBlockSize();
    long address = (prefetchLength - 1) * (blockFactor);

    for (int i = 0; i < prefetchLength; i++) {
      device.write((address - i * blockFactor), testDataBlock128kb);
    }
    for (int j = 0; j < prefetchLength; j++) {
      byte[] result = new byte[testDataBlock128kb.length];
      device.read((address - j * blockFactor), result);
      for (int i = 0; i < testDataBlock128kb.length; i++) {
        assertEquals(testDataBlock128kb[i], result[i]);
      }
    }
  }
}

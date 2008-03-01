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
 * $Id: JSCSIDeviceTest.java 2641 2007-04-10 09:46:28Z lemke $
 * 
 */

package org.jscsi.initiator.devices;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.jscsi.initiator.devices.JSCSIDevice;
import org.jscsi.initiator.devices.WriteBufferDevice;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WriteBufferDeviceTest {

  private static final String TARGET_NAME = "disk6";

  private static WriteBufferDevice device;

  /* Byte arrays (for all blocksizes) which are written to the targets. */
  private final byte[] testDataBlock8kb;

  private final byte[] testDataBlock128kb;

  Random randomGenerator;

  // ---------------------------------------------------------------------------
  // ---------------------------------------------------------------------------

  public WriteBufferDeviceTest() {

    // create byte arrays with random values for all blocksizes.
    randomGenerator = new Random(System.currentTimeMillis());
    testDataBlock8kb = new byte[8 * 1024];
    randomGenerator.nextBytes(testDataBlock8kb);
    testDataBlock128kb = new byte[128 * 1024];
    randomGenerator.nextBytes(testDataBlock128kb);
  }

  @Before
  public final void setUp() throws Exception {

    device = new WriteBufferDevice(new JSCSIDevice(TARGET_NAME));
    device.open();
  }

  @After
  public final void tearDown() throws Exception {

    device.close();
  }

  /**
   * Test buffered write with 8kb blocks on the device.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public final void testReadWriteBeginning8kb() throws Exception {

    int writeBlockCount = 100;
    int blockFactor = testDataBlock8kb.length / device.getBlockSize();
    long address = 0;

    for (int i = 0; i < writeBlockCount; i++) {
      device.write(address + i * blockFactor, testDataBlock8kb);
    }
    device.flush();
    for (int j = 0; j < writeBlockCount; j++) {
      byte[] result = new byte[testDataBlock8kb.length];
      device.read(address + j * blockFactor, result);
      for (int i = 0; i < testDataBlock8kb.length; i++) {
        assertEquals(result[i], testDataBlock8kb[i]);
      }
    }
  }

  /**
   * Test buffered write with 128kb blocks on the device.
   * 
   * @throws Exception
   *           These should never be thrown.
   */
  @Test
  public final void testReadWriteBeginning128kb() throws Exception {

    int writeBlockCount = 100;
    int blockFactor = testDataBlock128kb.length / device.getBlockSize();
    long address = 0;

    for (int i = 0; i < writeBlockCount; i++) {
      device.write(address + i * blockFactor, testDataBlock128kb);
    }
    device.flush();
    for (int j = 0; j < writeBlockCount; j++) {
      byte[] result = new byte[testDataBlock128kb.length];
      device.read(address + j * blockFactor, result);
      for (int i = 0; i < testDataBlock128kb.length; i++) {
        assertEquals(result[i], testDataBlock128kb[i]);
      }
    }
  }

}

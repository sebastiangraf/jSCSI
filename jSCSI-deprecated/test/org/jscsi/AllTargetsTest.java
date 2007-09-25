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

package org.jscsi;

import java.net.ConnectException;
import java.util.Random;

import org.jscsi.parser.exception.NoSuchSessionException;
import org.junit.Test;

/**
 * Tests, if all targets are available.
 * 
 * @author Bastian Lemke
 */
public class AllTargetsTest {
  private static final String[] TARGET_NAMES =
      {
          "titan04",
          "titan05",
          "titan06",
          "titan07",
          "titan08",
          "titan09",
          "titan10",
          "titan11",
          "titan12",
          "titan13",};

  /** Number of Blocks to write */
  private static final int TEST_DATA_SIZE = 1;

  private static byte[] testData;

  private static long address;

  private static Device device;

  private static Random randomGenerator;

  @Test
  public final void test() {

    for (String target : TARGET_NAMES) {
      System.out.print(target);
      try {
        device = new JSCSIDevice(target);
        device.open();
        System.out.println(" (" + device.getName() + ") --- OK");
        randomGenerator = new Random(System.currentTimeMillis());
        testData = new byte[TEST_DATA_SIZE * device.getBlockSize()];
        randomGenerator.nextBytes(testData);
      } catch (NoSuchSessionException nsse) {
        System.out.println(" --- FAILED");
      } catch (ConnectException ce) {
        System.out.println(" --- FAILED");
      } catch (Exception e) {

        e.printStackTrace();
      }
    }
  }
}

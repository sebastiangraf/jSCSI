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
 * $Id: CRC32CDigestTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.digest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * <h1>CRC32CDigestTest</h1>
 * <p>
 * Tests the correct calculation of CRCs by using the CRCR32C generator polynom.
 * 
 * @author Volker Wildi
 * 
 */
public class CRC32CDigestTest {

  /** Test case, with length of <code>32</code> bytes of zeros. */
  private static final int[] TEST_CASE_1 = { 0x0000000, 0x0000000, 0x0000000,
      0x0000000, 0x0000000, 0x0000000, 0x0000000, 0x0000000 };

  /** Expected CRC. */
  private static final int TEST_CASE_1_RESULT = 0xAA36918A;

  /** Test case, with length of <code>32</code> bytes of ones. */
  private static final int[] TEST_CASE_2 = { 0xFFFFFFFF, 0xFFFFFFFF,
      0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF };

  /** Expected CRC. */
  private static final int TEST_CASE_2_RESULT = 0x43ABA862;

  /**
   * Test case, with length of <code>32</code> bytes of incrementing
   * <code>0x00...0x1F</code>.
   */
  private static final int[] TEST_CASE_3 = { 0x00010203, 0x04050607,
      0x08090A0B, 0x0C0D0E0F, 0x10111213, 0x14151617, 0x18191A1B, 0x1C1D1E1F };

  /** Expected CRC. */
  private static final int TEST_CASE_3_RESULT = 0x4E79DD46;

  /**
   * Test case, with length of <code>32</code> bytes of decrementing
   * <code>0x1F...0x00</code>.
   */
  private static final int[] TEST_CASE_4 = { 0x1F1E1D1C, 0x1B1A1918,
      0x17161514, 0x13121110, 0x0F0E0D0C, 0x0B0A0908, 0x07060504, 0x03020100 };

  /** Expected CRC. */
  private static final int TEST_CASE_4_RESULT = 0x5CDB3F11;

  /**
   * Test case, with length of <code>44</code> bytes of an iSCSI Read Command
   * PDU.
   */
  private static final int[] TEST_CASE_5 = { 0x01C00000, 0x00000000,
      0x00000000, 0x00000000, 0x14000000, 0x00000400, 0x00000014, 0x00000018,
      0x28000000, 0x00000000, 0x02000000, 0x00000000 };

  /** Expected CRC. */
  private static final int TEST_CASE_5_RESULT = 0x563A96D9;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Instance of the class, which is to be tested. */
  protected CRC32CDigest crc = new CRC32CDigest();

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Reset the CRC to its initial value after each successful calculation. */
  @Before
  public void setUp() {

    crc.reset();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Tests the Slicing-by-4 algorithm for an integer array with TEST_CASE_1. */
  @Test
  public void testSlicingByFour1() {

    crc.slicingBy4(TEST_CASE_1);
    assertEquals((long) TEST_CASE_1_RESULT, crc.getValue());
  }

  /** Tests the Slicing-by-4 algorithm for an integer array with TEST_CASE_2. */
  @Test
  public void testSlicingByFour2() {

    crc.slicingBy4(TEST_CASE_2);
    assertEquals((long) TEST_CASE_2_RESULT, crc.getValue());
  }

  /** Tests the Slicing-by-4 algorithm for an integer array with TEST_CASE_3. */
  @Test
  public void testSlicingByFour3() {

    crc.slicingBy4(TEST_CASE_3);
    assertEquals((long) TEST_CASE_3_RESULT, crc.getValue());
  }

  /** Tests the Slicing-by-4 algorithm for an integer array with TEST_CASE_4. */
  @Test
  public void testSlicingByFour4() {

    crc.slicingBy4(TEST_CASE_4);
    assertEquals((long) TEST_CASE_4_RESULT, crc.getValue());
  }

  /** Tests the Slicing-by-4 algorithm for an integer array with TEST_CASE_5. */
  @Test
  public void testSlicingByFour5() {

    crc.slicingBy4(TEST_CASE_5);
    assertEquals((long) TEST_CASE_5_RESULT, crc.getValue());
  }

  /**
   * Tests the incremental version of the Slicing-by-4 algorithm with
   * TEST_CASE_1.
   */
  @Test
  public void testUpdate1() {

    for (int i = 0; i < TEST_CASE_1.length; i++) {
      crc.update(TEST_CASE_1[i]);
    }
    assertEquals((long) TEST_CASE_1_RESULT, crc.getValue());
  }

  /**
   * Tests the incremental version of the Slicing-by-4 algorithm with
   * TEST_CASE_2.
   */
  @Test
  public void testUpdate2() {

    for (int i = 0; i < TEST_CASE_2.length; i++) {
      crc.update(TEST_CASE_2[i]);
    }
    assertEquals((long) TEST_CASE_2_RESULT, crc.getValue());
  }

  /**
   * Tests the incremental version of the Slicing-by-4 algorithm with
   * TEST_CASE_3.
   */
  @Test
  public void testUpdate3() {

    for (int i = 0; i < TEST_CASE_3.length; i++) {
      crc.update(TEST_CASE_3[i]);
    }
    assertEquals((long) TEST_CASE_3_RESULT, crc.getValue());
  }

  /**
   * Tests the incremental version of the Slicing-by-4 algorithm with
   * TEST_CASE_4.
   */
  @Test
  public void testUpdate4() {

    for (int i = 0; i < TEST_CASE_4.length; i++) {
      crc.update(TEST_CASE_4[i]);
    }
    assertEquals((long) TEST_CASE_4_RESULT, crc.getValue());
  }

  /**
   * Tests the incremental version of the Slicing-by-4 algorithm with
   * TEST_CASE_5.
   */
  @Test
  public void testUpdate5() {

    for (int i = 0; i < TEST_CASE_5.length; i++) {
      crc.update(TEST_CASE_5[i]);
    }
    assertEquals((long) TEST_CASE_5_RESULT, crc.getValue());
  }

}

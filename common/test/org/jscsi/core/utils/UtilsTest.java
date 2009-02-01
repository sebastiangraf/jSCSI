/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id: UtilsTest.java
 * 2500 2007-03-05 13:29:08Z lemke $
 */

package org.jscsi.core.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests the Utils class of correctness.
 * 
 * @author Volker Wildi
 */
public class UtilsTest {

  /**
   * Tests the conversion from a signed byte to an unsigned integer number.
   */
  @Test
  public void testGetUnsignedInt() {

    assertEquals(0xF0, Utils.getUnsignedInt((byte) 0xF0));
  }

  /**
   * Tests the conversion from a signed short to an unsigned long number.
   */
  @Test
  public void testGetUnsignedLongFromShort() {

    assertEquals(0xBD18L, Utils.getUnsignedLong((short) 0xBD18));
  }

  /**
   * Tests the conversion from a signed integer to an unsigned long number.
   */
  @Test
  public void testGetUnsignedLongFromInt() {

    assertEquals(0xC254F92AL, Utils.getUnsignedLong(0xC254F92A));
  }
}

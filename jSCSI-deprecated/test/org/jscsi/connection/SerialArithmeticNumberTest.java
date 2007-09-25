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
 * $Id: SerialArithmeticNumberTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.connection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * <h1>SerialArithmetricNumberTest</h1>
 * <p/>
 * 
 * @author Volker Wildi
 */
public final class SerialArithmeticNumberTest {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Test
  public final void testInitialize() {

    final SerialArithmeticNumber serialNumber = new SerialArithmeticNumber(1);
    assertEquals(1, serialNumber.getValue());
  }

  @Test
  public final void testCompare1() {

    final SerialArithmeticNumber serialNumber = new SerialArithmeticNumber();
    assertEquals(0, serialNumber.getValue());

    assertTrue(serialNumber.compareTo(4) < 0);
  }

  @Test
  public final void testCompare2() {

    final SerialArithmeticNumber sNumber = new SerialArithmeticNumber(4);
    assertEquals(4, sNumber.getValue());

    assertTrue(sNumber.compareTo(3) > 0);
  }

  @Test
  public final void testCompare3() {

    final SerialArithmeticNumber sNumber = new SerialArithmeticNumber(2);
    assertEquals(2, sNumber.getValue());

    assertTrue(sNumber.compareTo(0xFFFFFFFF - 1) > 0);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

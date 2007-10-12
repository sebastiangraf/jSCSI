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
 * $Id: SettingsMapTest.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.parser.datasegment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

/**
 * <h1>SettingsMapTest</h1>
 * <p/>
 * 
 * @author Volker Wildi
 */
public final class SettingsMapTest {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private static SettingsMap settingsMap;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Before
  public final void setUp() {

    settingsMap = new SettingsMap();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Test
  public final void testAdd() {

    settingsMap.add(OperationalTextKey.AUTH_METHOD, "None,CRC32CDigest");

  }

  @Test
  public final void testGet() {

    settingsMap.add(OperationalTextKey.MAX_CONNECTIONS, "512");
    assertEquals("512", settingsMap.get(OperationalTextKey.MAX_CONNECTIONS));
  }

  @Test(expected = IllegalArgumentException.class)
  public final void testRemove1() {

    settingsMap.add(OperationalTextKey.AUTH_METHOD, "None");

    settingsMap.remove(OperationalTextKey.DATA_DIGEST);
  }

  @Test
  public final void testRemove2() {

    settingsMap.add(OperationalTextKey.DATA_PDU_IN_ORDER, "No");
    settingsMap.add(OperationalTextKey.ERROR_RECOVERY_LEVEL, "1");

    assertEquals(2, settingsMap.entrySet().size());

    settingsMap.remove(OperationalTextKey.ERROR_RECOVERY_LEVEL);
    assertEquals(1, settingsMap.entrySet().size());
  }

  @Test
  public final void testEquals() {

    settingsMap.add(OperationalTextKey.DATA_DIGEST, "Yes");

    SettingsMap anotherSettingsMap = new SettingsMap();
    anotherSettingsMap.add(OperationalTextKey.DATA_DIGEST, "Yes");

    assertTrue(settingsMap.equals(anotherSettingsMap));
  }

  @Test
  public final void testUnequals() {

    settingsMap.add(OperationalTextKey.DATA_DIGEST, "Yes");

    SettingsMap anotherSettingsMap = new SettingsMap();
    anotherSettingsMap.add(OperationalTextKey.DATA_DIGEST, "No");

    assertFalse(settingsMap.equals(anotherSettingsMap));
  }

  @Test
  public final void testClear() {

    settingsMap.add(OperationalTextKey.DEFAULT_TIME_2_WAIT, "2");
    assertEquals("2", settingsMap.get(OperationalTextKey.DEFAULT_TIME_2_WAIT));

    settingsMap.clear();
    settingsMap.get(OperationalTextKey.DEFAULT_TIME_2_WAIT);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Test
  public final void testAndMerge() {

    settingsMap.add(OperationalTextKey.IF_MARKER, "Yes");
    settingsMap.add(OperationalTextKey.OF_MARKER, "No");
    assertEquals(2, settingsMap.entrySet().size());

    final ResultFunctionFactory resultFactory = new ResultFunctionFactory();
    settingsMap.update(OperationalTextKey.IF_MARKER, "Yes", resultFactory
        .create("And"));

    assertEquals(2, settingsMap.entrySet().size());
    assertEquals("Yes", settingsMap.get(OperationalTextKey.IF_MARKER));

    settingsMap.update(OperationalTextKey.OF_MARKER, "No", resultFactory
        .create("And"));

    assertEquals(2, settingsMap.entrySet().size());
    assertEquals("No", settingsMap.get(OperationalTextKey.OF_MARKER));
  }

  @Test
  public final void testOrMerge() {

    settingsMap.add(OperationalTextKey.DATA_PDU_IN_ORDER, "Yes");
    settingsMap.add(OperationalTextKey.IMMEDIATE_DATA, "No");
    assertEquals(2, settingsMap.entrySet().size());

    final ResultFunctionFactory resultFactory = new ResultFunctionFactory();
    settingsMap.update(OperationalTextKey.DATA_PDU_IN_ORDER, "No",
        resultFactory.create("Or"));

    assertEquals(2, settingsMap.entrySet().size());
    assertEquals("Yes", settingsMap.get(OperationalTextKey.DATA_PDU_IN_ORDER));
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  @Test
  public final void testMaxMerge() {

    settingsMap.add(OperationalTextKey.IF_MARKER, "Yes");
    settingsMap.add(OperationalTextKey.DEFAULT_TIME_2_WAIT, "4");
    assertEquals(2, settingsMap.entrySet().size());

    final ResultFunctionFactory resultFactory = new ResultFunctionFactory();
    settingsMap.update(OperationalTextKey.DEFAULT_TIME_2_WAIT, "5",
        resultFactory.create("Max"));

    assertEquals(2, settingsMap.entrySet().size());
    assertEquals("5", settingsMap.get(OperationalTextKey.DEFAULT_TIME_2_WAIT));
  }

  @Test
  public final void testMinMerge() {

    settingsMap.add(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH, "16384");
    settingsMap.add(OperationalTextKey.FIRST_BURST_LENGTH, "4096");
    assertEquals(2, settingsMap.entrySet().size());

    final ResultFunctionFactory resultFactory = new ResultFunctionFactory();
    settingsMap.update(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH, "4096",
        resultFactory.create("Min"));

    assertEquals(2, settingsMap.entrySet().size());
    assertEquals("4096", settingsMap
        .get(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH));

    settingsMap.update(OperationalTextKey.FIRST_BURST_LENGTH, "8192",
        resultFactory.create("Min"));

    assertEquals(2, settingsMap.entrySet().size());
    assertEquals("4096", settingsMap.get(OperationalTextKey.FIRST_BURST_LENGTH));
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

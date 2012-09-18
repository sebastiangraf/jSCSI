/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jscsi.parser.datasegment;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;

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

    @BeforeMethod
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

    @Test(expectedExceptions = IllegalArgumentException.class)
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
        settingsMap.update(OperationalTextKey.IF_MARKER, "Yes", resultFactory.create("And"));

        assertEquals(2, settingsMap.entrySet().size());
        assertEquals("Yes", settingsMap.get(OperationalTextKey.IF_MARKER));

        settingsMap.update(OperationalTextKey.OF_MARKER, "No", resultFactory.create("And"));

        assertEquals(2, settingsMap.entrySet().size());
        assertEquals("No", settingsMap.get(OperationalTextKey.OF_MARKER));
    }

    @Test
    public final void testOrMerge() {

        settingsMap.add(OperationalTextKey.DATA_PDU_IN_ORDER, "Yes");
        settingsMap.add(OperationalTextKey.IMMEDIATE_DATA, "No");
        assertEquals(2, settingsMap.entrySet().size());

        final ResultFunctionFactory resultFactory = new ResultFunctionFactory();
        settingsMap.update(OperationalTextKey.DATA_PDU_IN_ORDER, "No", resultFactory.create("Or"));

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
        settingsMap.update(OperationalTextKey.DEFAULT_TIME_2_WAIT, "5", resultFactory.create("Max"));

        assertEquals(2, settingsMap.entrySet().size());
        assertEquals("5", settingsMap.get(OperationalTextKey.DEFAULT_TIME_2_WAIT));
    }

    @Test
    public final void testMinMerge() {

        settingsMap.add(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH, "16384");
        settingsMap.add(OperationalTextKey.FIRST_BURST_LENGTH, "4096");
        assertEquals(2, settingsMap.entrySet().size());

        final ResultFunctionFactory resultFactory = new ResultFunctionFactory();
        settingsMap.update(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH, "4096", resultFactory
            .create("Min"));

        assertEquals(2, settingsMap.entrySet().size());
        assertEquals("4096", settingsMap.get(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH));

        settingsMap.update(OperationalTextKey.FIRST_BURST_LENGTH, "8192", resultFactory.create("Min"));

        assertEquals(2, settingsMap.entrySet().size());
        assertEquals("4096", settingsMap.get(OperationalTextKey.FIRST_BURST_LENGTH));
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

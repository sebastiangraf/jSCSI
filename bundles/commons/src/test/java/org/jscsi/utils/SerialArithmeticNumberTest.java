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
package org.jscsi.utils;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;

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

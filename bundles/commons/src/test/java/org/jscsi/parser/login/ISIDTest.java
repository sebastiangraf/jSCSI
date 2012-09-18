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
package org.jscsi.parser.login;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.login.ISID.Format;

/**
 * Testing the correctness of the ISID.
 * 
 * @author Volker Wildi
 */
public class ISIDTest {

    /**
     * Valid Test Case with the following expected values. <blockquote> t: <code>OUI_FORMAT</code><br/>
     * a: <code>0x00</code><br/>
     * b: <code>0x0000</code><br/>
     * c: <code>0x00</code><br/>
     * d: <code>0xABCD</code> <br/>
     * </blockquote>
     */
    private static final long TEST_CASE = 0x00000000ABCD0000L;

    /**
     * This test case validates the parsing process.
     * 
     * @throws InternetSCSIException
     */
    @Test
    public void testDeserialize1() throws InternetSCSIException {

        ISID isid = new ISID();
        ISID expectedISID = new ISID(Format.OUI_FORMAT, (byte)0x00, (short)0x0000, (byte)0x00, (short)0xABCD);

        isid.deserialize(TEST_CASE);

        assertTrue(expectedISID.equals(isid));
    }

    /**
     * This test case validates the serialization process.
     * 
     * @throws InternetSCSIException
     */
    @Test
    public void testSerialize1() throws InternetSCSIException {

        ISID isid = new ISID(Format.OUI_FORMAT, (byte)0x00, (short)0x0000, (byte)0x00, (short)0xABCD);

        assertEquals(TEST_CASE, isid.serialize());
    }
}

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
package org.jscsi.parser.logout;

import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.io.IOException;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.ProtocolDataUnitTest;

/**
 * Testing the correctness of the LogoutRequestParser.
 * 
 * @author Volker Wildi
 */
public class LogoutRequestParserTest extends ProtocolDataUnitTest {

    /**
     * This Test Case violates the iSCSI Protocol Standard (RFC3720) and throws
     * an InternetSCSIException. The reason is, that the CID field must be zero,
     * if close session is requested.
     */
    private static final String TEST_CASE_1 = "46 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 01 a2 cc 00 01 00 00 00 01 a2 cc 00 01 a2 cd "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

    /**
     * This test case validates the recognition of an false Protocol Data Unit.
     * 
     * @throws IOException
     *             This exception should be never thrown.
     */
    @Test
    public void testDeserialize1() throws IOException, DigestException {

        try {
            super.setUp(TEST_CASE_1);
            // should never happen
            assertTrue(false);
        } catch (InternetSCSIException e) {
            assertTrue(true);
        }
    }

}

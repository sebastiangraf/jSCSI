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

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnitTest;
import org.jscsi.utils.WiresharkMessageParser;

/**
 * Testing the correctness of the LogoutResponseParser.
 * 
 * @author Volker Wildi
 */
public class LogoutResponseParserTest extends ProtocolDataUnitTest {

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>false</code><br/>
     * Operation Code = <code>LOGOUT_RESPONSE</code><br/>
     * Final Flag (Transit Flag) = <code>true</code><br/>
     * Continue Flag = <code>false</code><br/>
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x00000000</code><br/>
     * InitiatorTaskTag = <code>0x0001A2CC</code> <br/>
     * TSIH = <code>0x0000</code><br/>
     * CID = <code>0x0000</code><br/>
     * Response = <code>CONNECTION_CLOSED_SUCCESSFULLY</code><br/>
     * StatSN = <code>0x0001A2CD</code><br/>
     * ExpCmdSN = <code>0x0001A2CC</code><br/>
     * MaxCmdSN = <code>0x0001A2EC</code><br/>
     * <br/>
     * Time2Wait = <code>0x0000</code><br/>
     * Time2Retain = <code>0x0000</code><br/>
     * </blockquote>
     */
    private static final String TEST_CASE_1 = "26 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 01 a2 cc 00 00 00 00 00 01 a2 cd 00 01 a2 cc "
        + "00 01 a2 ec 00 00 00 00 00 00 00 00 00 00 00 00";

    /**
     * This test case validates the parsing process.
     * 
     * @throws IOException
     *             This exception should be never thrown.
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     */
    @Test
    public void testDeserialize1() throws IOException, InternetSCSIException, DigestException {

        super.setUp(TEST_CASE_1);
        super.testDeserialize(false, true, OperationCode.LOGOUT_RESPONSE, 0x00000000, 0x00000000, 0x0001A2CC);

        assertTrue(recognizedParser instanceof LogoutResponseParser);

        LogoutResponseParser parser = (LogoutResponseParser)recognizedParser;

        super.testDataSegment("");

        assertEquals(LogoutResponse.CONNECTION_CLOSED_SUCCESSFULLY, parser.getResponse());
        assertEquals(0x0001A2CD, parser.getStatusSequenceNumber());
        assertEquals(0x0001A2CC, parser.getExpectedCommandSequenceNumber());
        assertEquals(0x0001A2EC, parser.getMaximumCommandSequenceNumber());
        assertEquals((short)0x0000, parser.getTime2Wait());
        assertEquals((short)0x0000, parser.getTime2Retain());
    }

    /**
     * This test case validates the serialization process.
     * 
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     * @throws IOException
     *             This exception should be never thrown.
     */
    @Test
    public void testSerialize1() throws InternetSCSIException, IOException, DigestException {

        super.setUp(TEST_CASE_1);

        ByteBuffer expectedResult = WiresharkMessageParser.parseToByteBuffer(TEST_CASE_1);
        assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
    }

}

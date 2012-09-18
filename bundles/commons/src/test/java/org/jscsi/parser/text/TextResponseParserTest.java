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
package org.jscsi.parser.text;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnitTest;
import org.jscsi.parser.datasegment.OperationalTextKey;
import org.jscsi.parser.datasegment.SettingsMap;
import org.jscsi.utils.WiresharkMessageParser;

/**
 * Testing the correctness of the TextResponseParser.
 * 
 * @author Volker Wildi
 */
public class TextResponseParserTest extends ProtocolDataUnitTest {

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>false</code><br/>
     * Operation Code = <code>TEXT_RESPONSE</code> <br/>
     * Final Flag = <code>true</code><br/>
     * Continue Flag = <code>false</code><br/>
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x0000004E</code><br/>
     * InitiatorTaskTag = <code>0x03000000</code><br/>
     * TargetTaskTag = <code>0xFFFFFFFF</code><br/>
     * StatSN = <code>0x00000002</code><br/>
     * ExpCmdSN = <code>0x00000000</code> <br/>
     * MaxCmdSN = <code>0x00000000</code><br/>
     * <br/>
     * <b>Key-Values:</b><br/>
     * TARGET_NAME = <code>iqn.1987-05.com.cisco.00.58031e1d068ac226d385847592c0b670.IBM-Disk</code> <br/>
     * </blockquote>
     */
    private static final String TEST_CASE_1 = "24 80 00 00 00 00 00 4e 00 00 00 00 00 00 00 00 "
        + "03 00 00 00 ff ff ff ff 00 00 00 02 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "54 61 72 67 65 74 4e 61 6d 65 3d 69 71 6e 2e 31 "
        + "39 38 37 2d 30 35 2e 63 6f 6d 2e 63 69 73 63 6f "
        + "2e 30 30 2e 35 38 30 33 31 65 31 64 30 36 38 61 "
        + "63 32 32 36 64 33 38 35 38 34 37 35 39 32 63 30 "
        + "62 36 37 30 2e 49 42 4d 2d 44 69 73 6b 00 00 00";

    /**
     * This test case validates the parsing process.
     * 
     * @throws IOException
     *             This exception should be never thrown.
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     */
    @Test
    public void testDeserialize() throws IOException, InternetSCSIException, DigestException {

        SettingsMap expectedKeyValuePair = new SettingsMap();
        expectedKeyValuePair.add(OperationalTextKey.TARGET_NAME,
            "iqn.1987-05.com.cisco.00.58031e1d068ac226d385847592c0b670.IBM-Disk");

        super.setUp(TEST_CASE_1);
        super.testDeserialize(false, true, OperationCode.TEXT_RESPONSE, 0x00000000, 0x0000004E, 0x03000000);
        super.testDataSegment(expectedKeyValuePair);

        assertTrue(recognizedParser instanceof TextResponseParser);

        TextResponseParser parser = (TextResponseParser)recognizedParser;

        assertFalse(parser.isContinueFlag());
        assertEquals(0x0000000000000000L, parser.getLogicalUnitNumber());

        assertEquals(0xFFFFFFFF, parser.getTargetTransferTag());
        assertEquals(0x00000002, parser.getStatusSequenceNumber());
        assertEquals(0x00000000, parser.getExpectedCommandSequenceNumber());
        assertEquals(0x00000000, parser.getMaximumCommandSequenceNumber());
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
    public void testSerialize() throws InternetSCSIException, IOException, DigestException {

        super.setUp(TEST_CASE_1);

        ByteBuffer expectedResult = WiresharkMessageParser.parseToByteBuffer(TEST_CASE_1);
        assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
    }

}

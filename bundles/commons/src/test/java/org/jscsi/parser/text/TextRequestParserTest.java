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
 * Testing the correctness of the TextRequestParser.
 * 
 * @author Volker Wildi
 */
public class TextRequestParserTest extends ProtocolDataUnitTest {

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>true</code><br/>
     * Operation Code = <code>TEXT_REQUEST</code> <br/>
     * Final Flag = <code>true</code><br/>
     * Continue Flag = <code>false</code><br/>
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x00000010</code><br/>
     * InitiatorTaskTag = <code>0x03000000</code><br/>
     * TargetTaskTag = <code>0xFFFFFFFF</code><br/>
     * CmdSN = <code>0x00000000</code><br/>
     * ExpStatSN = <code>0x00000000</code> <br/>
     * <br/>
     * <b>Key-Values:</b><br/>
     * SEND_TARGETS = <code>all</code><br/>
     * </blockquote>
     */
    private static final String TEST_CASE_1 = "44 80 00 00 00 00 00 10 00 00 00 00 00 00 00 00 "
        + "03 00 00 00 ff ff ff ff 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "53 65 6e 64 54 61 72 67 65 74 73 3d 61 6c 6c 00";

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

        SettingsMap expectedKeyValuePair = new SettingsMap();
        expectedKeyValuePair.add(OperationalTextKey.SEND_TARGETS, "all");

        super.setUp(TEST_CASE_1);
        super.testDeserialize(true, true, OperationCode.TEXT_REQUEST, 0x00000000, 0x00000010, 0x03000000);
        super.testDataSegment(expectedKeyValuePair);

        assertTrue(recognizedParser instanceof TextRequestParser);

        TextRequestParser parser = (TextRequestParser)recognizedParser;

        assertFalse(parser.isContinueFlag());
        assertEquals(0x0000000000000000L, parser.getLogicalUnitNumber());

        assertEquals(0xFFFFFFFF, parser.getTargetTransferTag());
        assertEquals(0x00000000, parser.getCommandSequenceNumber());
        assertEquals(0x00000000, parser.getExpectedStatusSequenceNumber());
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

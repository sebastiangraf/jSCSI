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
package org.jscsi.parser.reject;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnitTest;
import org.jscsi.parser.reject.RejectParser.ReasonCode;
import org.jscsi.utils.WiresharkMessageParser;

/**
 * Testing the correctness of the RejectInParser.
 * 
 * @author Volker Wildi
 */
public class RejectParserTest extends ProtocolDataUnitTest {

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>false</code><br/>
     * Operation Code = <code>REJECT</code><br/>
     * Final Flag = <code>true</code><br/>
     * Reason = <code>PROTOCOL_ERROR</code> <br/>
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x00000030</code><br/>
     * LUN = <code>0x0000000000000000</code><br/>
     * InitiatorTaskTag = <code>0xFFFFFFFF</code><br/>
     * StatSN = <code>0x00000000</code><br/>
     * ExpCmdSN = <code>0x00000000</code><br/>
     * MaxCmdSN = <code>0x00000000</code><br/>
     * DataSN = <code>0x00000000</code> <br/>
     * DataSegment =
     * 
     * <code>C3 00 02 02 00 00 00 95 00 00 00 00 AB CD 00 00 A8 97 69 81 00 00 00 00 00 00 01 5D 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00</code>
     * <br/>
     * </blockquote>
     */
    private static final String TEST_CASE_1 = "3f 80 04 00 00 00 00 30 00 00 00 00 00 00 00 00 "
        + "ff ff ff ff 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "c3 00 02 02 00 00 00 95 00 00 00 00 ab cd 00 00 "
        + "a8 97 69 81 00 00 00 00 00 00 01 5d 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

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
        super.testDeserialize(false, true, OperationCode.REJECT, 0x00000000, 0x00000030, 0xFFFFFFFF);

        assertTrue(recognizedParser instanceof RejectParser);

        RejectParser parser = (RejectParser)recognizedParser;

        // test reject fields
        assertEquals(ReasonCode.PROTOCOL_ERROR, parser.getReasonCode());

        assertEquals(0x00000000, parser.getStatusSequenceNumber());
        assertEquals(0x00000000, parser.getExpectedCommandSequenceNumber());
        assertEquals(0x00000000, parser.getMaximumCommandSequenceNumber());
        assertEquals(0x00000000, parser.getDataSequenceNumber());
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

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
package org.jscsi.parser.r2t;

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
 * Testing the correctness of the Ready2TransferParser.
 * 
 * @author Volker Wildi
 */
public class Ready2TransferParserTest extends ProtocolDataUnitTest {

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>false</code><br/>
     * Operation Code = <code>R2T</code><br/>
     * Final Flag = <code>true</code><br/>
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x000000D9</code><br/>
     * LUN = <code>0x0000000000000000</code><br/>
     * TargetTranferTag = <code>0x00000001</code><br/>
     * StatSN = <code>0x000000DA</code><br/>
     * ExpCmdSN = <code>0x000000DA</code><br/>
     * MaxCmdSN = <code>0x000000FA</code><br/>
     * R2TSN = <code>0x00000000</code><br/>
     * BufferOffset = <code>0x00000000</code> <br/>
     * DesiredDataTransferLength = <code>0x00001000</code><br/>
     * <br/>
     * </blockquote>
     */
    private static final String TEST_CASE_1 = "31 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 d9 00 00 00 01 00 00 00 da 00 00 00 da "
        + "00 00 00 fa 00 00 00 00 00 00 00 00 00 00 10 00";

    /**
     * This test case validates the parsing process.
     * 
     * @throws IOException
     *             This exception should be never thrown.
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     * @throws DigestException
     */
    @Test
    public void testDeserialize1() throws IOException, InternetSCSIException, DigestException {

        super.setUp(TEST_CASE_1);
        super.testDeserialize(false, true, OperationCode.R2T, 0x00000000, 0x00000000, 0x000000D9);

        assertTrue(recognizedParser instanceof Ready2TransferParser);

        Ready2TransferParser parser = (Ready2TransferParser)recognizedParser;

        assertEquals(0x0000000000000000L, parser.getLogicalUnitNumber());

        assertEquals(0x00000001, parser.getTargetTransferTag());
        assertEquals(0x000000DA, parser.getExpectedCommandSequenceNumber());
        assertEquals(0x000000FA, parser.getMaximumCommandSequenceNumber());
        assertEquals(0x000000DA, parser.getStatusSequenceNumber());
        assertEquals(0x00000000, parser.getReady2TransferSequenceNumber());
        assertEquals(0x00000000, parser.getBufferOffset());
        assertEquals(0x00001000, parser.getDesiredDataTransferLength());
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

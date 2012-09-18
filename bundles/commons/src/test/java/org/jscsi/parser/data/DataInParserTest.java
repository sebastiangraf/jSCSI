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
package org.jscsi.parser.data;

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
import org.jscsi.utils.WiresharkMessageParser;

/**
 * Testing the correctness of the DataInParser.
 * 
 * @author Volker Wildi
 */
public class DataInParserTest extends ProtocolDataUnitTest {

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>false</code><br/>
     * Operation Code = <code>SCSI_DATA_IN</code> <br/>
     * Final Flag = <code>true</code><br/>
     * Acknowledge Flag = <code>false</code><br/>
     * Read Residual Overflow = <code>false</code><br/>
     * ReadResidualUnderflow = <code>false</code><br/>
     * Residual Overflow = <code>false</code><br/>
     * Residual Underflow = <code>false</code><br/>
     * Status Flag = <code>true</code><br/>
     * <br/>
     * TotalAHSLength = <code>0x00000000</code> <br/>
     * DataSegmentLength = <code>0x00000010</code><br/>
     * LUN = <code>0x0000000000000000</code><br/>
     * InitiatorTaskTag = <code>0x00000001</code><br/>
     * TargetTranferTag = <code>0xFFFFFFFF</code> <br/>
     * StatSN = <code>0x00000002</code><br/>
     * ExpCmdSN = <code>0x00000002</code><br/>
     * MaxCmdSN = <code>0x00000022</code><br/>
     * DataSN = <code>0x00000000</code><br/>
     * BufferOffset = <code>0x00000000</code><br/>
     * ResidualCount = <code>0x00000000</code><br/>
     * <br/>
     * </blockquote>
     */
    private static final String TEST_CASE_1 = "25 81 00 00 00 00 00 10 00 00 00 00 00 00 00 00 "
        + "00 00 00 01 ff ff ff ff 00 00 00 02 00 00 00 02 "
        + "00 00 00 22 00 00 00 00 00 00 00 00 00 00 00 00 ";

    /** Data segment to the TEST_CASE_1. */
    private static final String TEST_CASE_1_DATA_SEGMENT = "00 00 00 18 00 00 00 00 00 06 00 00 00 00 00 00";

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>false</code><br/>
     * Operation Code = <code>SCSI_DATA_IN</code> <br/>
     * Final Flag = <code>true</code><br/>
     * Acknowledge Flag = <code>false</code><br/>
     * Read Residual Overflow = <code>false</code><br/>
     * ReadResidualUnderflow = <code>false</code><br/>
     * Residual Overflow = <code>false</code><br/>
     * Residual Underflow = <code>false</code><br/>
     * Status Flag = <code>false</code><br/>
     * <br/>
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x00000008</code> <br/>
     * LUN = <code>0x0000000000000000</code><br/>
     * InitiatorTaskTag = <code>0x1B000000</code><br/>
     * TargetTransferTag = <code>0x00000000</code> <br/>
     * StatSN = <code>0x00000000</code><br/>
     * ExpCmdSN = <code>0x00000009</code><br/>
     * MaxCmdSN = <code>0x00000018</code><br/>
     * DataSN = <code>0x00000000</code><br/>
     * BufferOffset = <code>0x00000000</code><br/>
     * ResidualCount = <code>0x00000000</code><br/>
     * </blockquote>
     */
    private static final String TEST_CASE_2 = "25 80 00 00 00 00 00 08 00 00 00 00 00 00 00 00 "
        + "1b 00 00 00 00 00 00 00 00 00 00 00 00 00 00 09 "
        + "00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 00 " + "02 22 ee 55 00 00 02 00";

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

        super.setUp(TEST_CASE_1 + TEST_CASE_1_DATA_SEGMENT);
        super.testDeserialize(false, true, OperationCode.SCSI_DATA_IN, 0x00000000, 0x00000010, 0x00000001);

        assertTrue(recognizedParser instanceof DataInParser);

        DataInParser parser = (DataInParser)recognizedParser;

        assertFalse(parser.isAcknowledgeFlag());
        assertFalse(parser.isBidirectionalReadResidualOverflow());
        assertFalse(parser.isBidirectionalReadResidualUnderflow());
        assertTrue(parser.isStatusFlag());

        assertEquals(0x0000000000000000L, parser.getLogicalUnitNumber());

        assertEquals(0xFFFFFFFF, parser.getTargetTaskTag());
        assertEquals(0x00000002, parser.getStatusSequenceNumber());
        assertEquals(0x00000002, parser.getExpectedCommandSequenceNumber());
        assertEquals(0x00000022, parser.getMaximumCommandSequenceNumber());
        assertEquals(0x00000000, parser.getDataSequenceNumber());
        assertEquals(0x00000000, parser.getBufferOffset());
        assertEquals(0x00000000, parser.getResidualCount());

        super.testDataSegment(TEST_CASE_1_DATA_SEGMENT);
    }

    /**
     * This test case validates the parsing process.
     * 
     * @throws IOException
     *             This exception should be never thrown.
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     */
    @Test
    public void testDeserialize2() throws IOException, InternetSCSIException, DigestException {

        super.setUp(TEST_CASE_2);
        super.testDeserialize(false, true, OperationCode.SCSI_DATA_IN, 0x00000000, 0x00000008, 0x1B000000);

        assertTrue(recognizedParser instanceof DataInParser);

        DataInParser parser = (DataInParser)recognizedParser;

        assertFalse(parser.isAcknowledgeFlag());
        assertFalse(parser.isBidirectionalReadResidualOverflow());
        assertFalse(parser.isBidirectionalReadResidualUnderflow());
        assertFalse(parser.isStatusFlag());

        assertEquals(0x0000000000000000L, parser.getLogicalUnitNumber());

        assertEquals(0x00000000, parser.getTargetTaskTag());
        assertEquals(0x00000000, parser.getStatusSequenceNumber());
        assertEquals(0x00000009, parser.getExpectedCommandSequenceNumber());
        assertEquals(0x00000018, parser.getMaximumCommandSequenceNumber());
        assertEquals(0x00000000, parser.getDataSequenceNumber());
        assertEquals(0x00000000, parser.getBufferOffset());
        assertEquals(0x00000000, parser.getResidualCount());
    }

    /**
     * This test case validates the serialization process.
     * 
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     * @throws IOException
     *             This exception should be never thrown.
     * @throws DigestException
     * @throws DigestException
     *             This exception should be never thrown.
     */
    @Test
    public void testSerialize1() throws InternetSCSIException, IOException, DigestException {

        super.setUp(TEST_CASE_1 + TEST_CASE_1_DATA_SEGMENT);

        ByteBuffer expectedResult =
            WiresharkMessageParser.parseToByteBuffer(TEST_CASE_1 + TEST_CASE_1_DATA_SEGMENT);
        assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
    }

    /**
     * This test case validates the serialization process.
     * 
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     * @throws IOException
     *             This exception should be never thrown.
     * @throws DigestException
     *             This exception should be never thrown.
     */
    @Test
    public void testSerialize2() throws InternetSCSIException, IOException, DigestException {

        super.setUp(TEST_CASE_2);

        ByteBuffer expectedResult = WiresharkMessageParser.parseToByteBuffer(TEST_CASE_2);
        assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
    }
}

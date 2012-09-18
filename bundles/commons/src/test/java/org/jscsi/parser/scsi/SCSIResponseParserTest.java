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
package org.jscsi.parser.scsi;

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
import org.jscsi.parser.scsi.SCSIResponseParser.ServiceResponse;
import org.jscsi.utils.WiresharkMessageParser;

/**
 * Testing the correctness of the SCSIResponseParser.
 * 
 * @author Volker Wildi
 */
public class SCSIResponseParserTest extends ProtocolDataUnitTest {

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>false</code><br/>
     * Operation Code = <code>SCSI_RESPONSE</code> <br/>
     * Final Flag = <code>true</code><br/>
     * BidirectionalReadResidualOverflowFlag = <code>false</code><br/>
     * BidirectionalReadResidualUnderflowFlag = <code>false</code><br/>
     * ResidualOverflowFlag = <code>false</code><br/>
     * ResidualUnderflowFlag = <code>false</code><br/>
     * Response = <code>COMMAND_COMPLETED_AT_TARGET</code> <br/>
     * Status = <code>GOOD</code><br/>
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x00000000</code> <br/>
     * LUN = <code>0x0000000000000000</code><br/>
     * InitiatorTaskTag = <code>0x34000000</code><br/>
     * StatSN = <code>0x0000035</code><br/>
     * ExpCmdSN = <code>0x00000035</code><br/>
     * MaxCmdSN = <code>0x00000055</code><br/>
     * ExpDataSN = <code>0x00000035</code><br/>
     * Bidirectional Read Residual Count = <code>0x00000000</code><br/>
     * ResidualCount = <code>0x00000000</code><br/>
     * </blockquote>
     */
    private static final String TEST_CASE_1 = "21 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "34 00 00 00 00 00 00 00 00 00 00 35 00 00 00 35 "
        + "00 00 00 55 00 00 00 00 00 00 00 00 00 00 00 00";

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>false</code><br/>
     * Operation Code = <code>SCSI_RESPONSE</code> <br/>
     * Final Flag = <code>true</code><br/>
     * BidirectionalReadResidualOverflowFlag = <code>false</code><br/>
     * BidirectionalReadResidualUnderflowFlag = <code>false</code><br/>
     * ResidualOverflowFlag = <code>false</code><br/>
     * ResidualUnderflowFlag = <code>false</code><br/>
     * Response = <code>COMMAND_COMPLETED_AT_TARGET</code> </br/> Status = <code>GOOD</code><br/>
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x00000000</code> <br/>
     * InitiatorTaskTag = <code>0x00000001</code><br/>
     * StatSN = <code>0x0000013</code><br/>
     * ExpCmdSN = <code>0x00000013</code><br/>
     * MaxCmdSN = <code>0x00000033</code><br/>
     * ExpDataSN = <code>0x00000000</code> <br/>
     * Bidirectional Read Residual Count = <code>0x00000000</code><br/>
     * ResidualCount = <code>0x00000000</code><br/>
     * <p>
     * ResponseDataSegment:<br/>
     * </blockquote>
     */
    private static final String TEST_CASE_2 = "21 80 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 01 00 00 00 00 00 00 00 13 00 00 00 13 "
        + "00 00 00 33 00 00 00 00 00 00 00 00 00 00 00 00";

    /**
     * This test case validates the parsing process.
     * 
     * @throws IOException
     *             This exception should be never thrown.
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     * @throws DigestException
     *             This exception should be never thrown.
     */
    @Test
    public void testDeserialize1() throws IOException, InternetSCSIException, DigestException {

        super.setUp(TEST_CASE_1);
        super.testDeserialize(false, true, OperationCode.SCSI_RESPONSE, 0x00000000, 0x00000000, 0x34000000);

        assertTrue(recognizedParser instanceof SCSIResponseParser);

        SCSIResponseParser parser = (SCSIResponseParser)recognizedParser;

        // test SCSI response fields
        assertFalse(parser.isBidirectionalReadResidualOverflow());
        assertFalse(parser.isBidirectionalReadResidualUnderflow());
        assertFalse(parser.isResidualOverflow());
        assertFalse(parser.isResidualUnderflow());

        assertEquals(ServiceResponse.COMMAND_COMPLETED_AT_TARGET, parser.getResponse());
        assertEquals(SCSIStatus.GOOD, parser.getStatus());
        assertEquals(0x00000035, parser.getStatusSequenceNumber());
        assertEquals(0x00000035, parser.getExpectedCommandSequenceNumber());
        assertEquals(0x00000055, parser.getMaximumCommandSequenceNumber());
        assertEquals(0x00000000, parser.getExpectedDataSequenceNumber());
        assertEquals(0x00000000, parser.getBidirectionalReadResidualCount());
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
     *             This exception should be never thrown
     */
    @Test
    public void testSerialize1() throws InternetSCSIException, IOException, DigestException {

        super.setUp(TEST_CASE_1);

        ByteBuffer expectedResult = WiresharkMessageParser.parseToByteBuffer(TEST_CASE_1);
        assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
    }

    /**
     * This test case validates the parsing process.
     * 
     * @throws IOException
     *             This exception should be never thrown.
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     * @throws DigestException
     *             This exception should be never thrown.
     */
    @Test
    public void testDeserialize2() throws IOException, InternetSCSIException, DigestException {

        super.setUp(TEST_CASE_2);
        super.testDeserialize(false, true, OperationCode.SCSI_RESPONSE, 0x00000000, 0x00000000, 0x00000001);

        assertTrue(recognizedParser instanceof SCSIResponseParser);

        SCSIResponseParser parser = (SCSIResponseParser)recognizedParser;

        // test SCSI response fields
        assertFalse(parser.isBidirectionalReadResidualOverflow());
        assertFalse(parser.isBidirectionalReadResidualUnderflow());
        assertFalse(parser.isResidualOverflow());
        assertFalse(parser.isResidualUnderflow());

        assertEquals(ServiceResponse.COMMAND_COMPLETED_AT_TARGET, parser.getResponse());
        assertEquals(SCSIStatus.GOOD, parser.getStatus());
        assertEquals(0x00000013, parser.getStatusSequenceNumber());
        assertEquals(0x00000013, parser.getExpectedCommandSequenceNumber());
        assertEquals(0x00000033, parser.getMaximumCommandSequenceNumber());
        assertEquals(0x00000000, parser.getExpectedDataSequenceNumber());
        assertEquals(0x00000000, parser.getBidirectionalReadResidualCount());
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
     *             This exception should be never thrown.
     */
    @Test
    public void testSerialize2() throws InternetSCSIException, IOException, DigestException {

        super.setUp(TEST_CASE_2);

        ByteBuffer expectedResult = WiresharkMessageParser.parseToByteBuffer(TEST_CASE_2);
        assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
    }
}

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
import org.jscsi.parser.login.ISID.Format;
import org.jscsi.utils.WiresharkMessageParser;

/**
 * Testing the correctness of the LoginRequestParser.
 * 
 * @author Volker Wildi
 */
public class LoginRequestParserTest extends ProtocolDataUnitTest {

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>true</code><br/>
     * Operation Code = <code>LOGIN_REQUEST</code> <br/>
     * Final Flag (Transit Flag) = <code>false</code><br/>
     * Continue Flag = <code>false</code><br/>
     * CSG = <code>LoginStage.SECURITY_NEGOTIATION</code> <br/>
     * NSG = <code>LoginStage.SECURITY_NEGOTIATION</code><br>
     * minVersion = <code>0x0000</code><br/>
     * maxVersion = <code>0x0000</code> <br/>
     * <br/>
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x00000095</code><br/>
     * <b>ISID:</b><br/>
     * <blockquote> t = <code>ISID.OUI_FORMAT</code><br/>
     * a = <code>0x00</code><br/>
     * b = <code>0x0000</code><br/>
     * c = <code>0x00</code><br/>
     * d = <code>0xABCD</code> <br/>
     * <br/>
     * </blockquote> InitiatorTaskTag = <code>0xC8D04B81</code><br/>
     * TSIH = <code>0x0000</code><br/>
     * CID = <code>0x0000</code><br/>
     * CmdSN = <code>0x00000001</code><br/>
     * ExpStatSN = <code>0x00000000</code><br/>
     * <br/>
     * <b>Key-Values:</b><br/>
     * INITIATOR_NAME = <code>iqn.com.ibm.k-machine</code> <br/>
     * TARGET_NAME = <code>iqn.1987-05.com.cisco.00.58031e1d068ac226d385847592c0b670.IBM-Disk</code> <br/>
     * AUTH_METHOD = <code>none</code><br/>
     * SESSION_TYPE = <code>normal</code><br/>
     * </blockquote>
     */
    private static final String TEST_CASE_1 = "43 00 00 00 00 00 00 95 00 00 00 00 ab cd 00 00 "
        + "c8 d0 4b 81 00 00 00 00 00 00 00 01 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "49 6e 69 74 69 61 74 6f 72 4e 61 6d 65 3d 69 71 "
        + "6e 2e 63 6f 6d 2e 69 62 6d 2e 6b 2d 6d 61 63 68 "
        + "69 6e 65 00 54 61 72 67 65 74 4e 61 6d 65 3d 69 "
        + "71 6e 2e 31 39 38 37 2d 30 35 2e 63 6f 6d 2e 63 "
        + "69 73 63 6f 2e 30 30 2e 35 38 30 33 31 65 31 64 "
        + "30 36 38 61 63 32 32 36 64 33 38 35 38 34 37 35 "
        + "39 32 63 30 62 36 37 30 2e 49 42 4d 2d 44 69 73 "
        + "6b 00 41 75 74 68 4d 65 74 68 6f 64 3d 6e 6f 6e "
        + "65 00 53 65 73 73 69 6f 6e 54 79 70 65 3d 6e 6f " + "72 6d 61 6c 00 00 00 00";

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>true</code><br/>
     * Operation Code = <code>LOGIN_REQUEST</code> <br/>
     * Final Flag (Transit Flag) = <code>true</code><br/>
     * Continue Flag = <code>false</code><br/>
     * CSG = <code>LoginStage.LOGIN_OPERATIONAL_NEGOTIATION</code><br/>
     * NSG = <code>LoginStage.FULL_FEATURE_PHASE</code><br>
     * minVersion = <code>0x0000</code><br/>
     * maxVersion = <code>0x0000</code> <br/>
     * <br/>
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x000001AE</code><br/>
     * <b>ISID:</b><br/>
     * <blockquote> t = <code>ISID.OUI_FORMAT</code><br/>
     * a = <code>0x00</code><br/>
     * b = <code>0x023D</code><br/>
     * c = <code>0x00</code><br/>
     * d = <code>0x0000</code> <br/>
     * <br/>
     * </blockquote> InitiatorTaskTag = <code>0x000A0000</code><br/>
     * TSIH = <code>0x0000</code><br/>
     * CID = <code>0x0000</code><br/>
     * CmdSN = <code>0x00000000</code><br/>
     * ExpStatSN = <code>0x00000000</code><br/>
     * <br/>
     * <b>Key-Values:</b><br/>
     * INITIATOR_NAME = <code>iqn.1987-05.com.cisco:01.ac24953f3f9</code><br/>
     * INITIATOR_ALIAS, "Initiator" TARGET_NAME = <code>disk1</code><br/>
     * SESSION_TYPE = <code>Normal</code><br/>
     * HEADER_DIGEST = <code>None,CRC32CDigest</code> <br/>
     * DATA_DIGEST = <code>None</code><br/>
     * DEFAULT_TIME_2_WAIT = <code>0</code><br/>
     * DEFAULT_TIME_2_RETAIN = <code>0</code><br/>
     * ERROR_RECOVERY_LEVEL = <code>0</code><br/>
     * INITIAL_R2T = <code>No</code> <br/>
     * IMMEDIATE_DATA = <code>Yes</code><br/>
     * MAX_BURST_LENGTH = <code>16776192</code><br/>
     * FIRST_BURST_LENGTH = <code>262144</code><br/>
     * MAX_OUTSTANDING_R2T = <code>1</code><br/>
     * MAX_CONNECTIONS = <code>1</code> <br/>
     * DATA_PDU_IN_ORDER = <code>Yes</code><br/>
     * DATA_SEQUENCE_IN_ORDER = <code>Yes</code><br/>
     * MAX_RECV_DATA_SEGMENT_LENGTH = <code>131072</code> <br/>
     * IF_MARKER = <code>No</code><br/>
     * OF_MARKER = <code>No</code><br/>
     * </blockquote>
     */

    private static final String TEST_CASE_2 = "43 87 00 00 00 00 01 ae 00 02 3d 00 00 00 00 00 "
        + "00 0a 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "49 6e 69 74 69 61 74 6f 72 4e 61 6d 65 3d 69 71 "
        + "6e 2e 31 39 38 37 2d 30 35 2e 63 6f 6d 2e 63 69 "
        + "73 63 6f 3a 30 31 2e 61 63 32 34 39 35 33 66 33 "
        + "66 39 00 49 6e 69 74 69 61 74 6f 72 41 6c 69 61 "
        + "73 3d 49 6e 69 74 69 61 74 6f 72 00 54 61 72 67 "
        + "65 74 4e 61 6d 65 3d 64 69 73 6b 31 00 53 65 73 "
        + "73 69 6f 6e 54 79 70 65 3d 4e 6f 72 6d 61 6c 00 "
        + "48 65 61 64 65 72 44 69 67 65 73 74 3d 4e 6f 6e "
        + "65 2c 43 52 43 33 32 43 00 44 61 74 61 44 69 67 "
        + "65 73 74 3d 4e 6f 6e 65 00 44 65 66 61 75 6c 74 "
        + "54 69 6d 65 32 57 61 69 74 3d 30 00 44 65 66 61 "
        + "75 6c 74 54 69 6d 65 32 52 65 74 61 69 6e 3d 30 "
        + "00 49 46 4d 61 72 6b 65 72 3d 4e 6f 00 4f 46 4d "
        + "61 72 6b 65 72 3d 4e 6f 00 45 72 72 6f 72 52 65 "
        + "63 6f 76 65 72 79 4c 65 76 65 6c 3d 30 00 49 6e "
        + "69 74 69 61 6c 52 32 54 3d 4e 6f 00 49 6d 6d 65 "
        + "64 69 61 74 65 44 61 74 61 3d 59 65 73 00 4d 61 "
        + "78 42 75 72 73 74 4c 65 6e 67 74 68 3d 31 36 37 "
        + "37 36 31 39 32 00 46 69 72 73 74 42 75 72 73 74 "
        + "4c 65 6e 67 74 68 3d 32 36 32 31 34 34 00 4d 61 "
        + "78 4f 75 74 73 74 61 6e 64 69 6e 67 52 32 54 3d "
        + "31 00 4d 61 78 43 6f 6e 6e 65 63 74 69 6f 6e 73 "
        + "3d 31 00 44 61 74 61 50 44 55 49 6e 4f 72 64 65 "
        + "72 3d 59 65 73 00 44 61 74 61 53 65 71 75 65 6e "
        + "63 65 49 6e 4f 72 64 65 72 3d 59 65 73 00 4d 61 "
        + "78 52 65 63 76 44 61 74 61 53 65 67 6d 65 6e 74 "
        + "4c 65 6e 67 74 68 3d 31 33 31 30 37 32 00 00 00 ";

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
        expectedKeyValuePair.add(OperationalTextKey.INITIATOR_NAME, "iqn.com.ibm.k-machine");
        expectedKeyValuePair.add(OperationalTextKey.TARGET_NAME,
            "iqn.1987-05.com.cisco.00.58031e1d068ac226d385847592c0b670.IBM-Disk");
        expectedKeyValuePair.add(OperationalTextKey.AUTH_METHOD, "none");
        expectedKeyValuePair.add(OperationalTextKey.SESSION_TYPE, "normal");

        super.setUp(TEST_CASE_1);
        super.testDeserialize(true, false, OperationCode.LOGIN_REQUEST, 0x00000000, 0x00000095, 0xC8D04B81);
        super.testDataSegment(expectedKeyValuePair);

        assertTrue(recognizedParser instanceof LoginRequestParser);

        LoginRequestParser parser = (LoginRequestParser)recognizedParser;

        // test login request fields
        assertFalse(parser.isContinueFlag());
        assertEquals(LoginStage.SECURITY_NEGOTIATION, parser.getCurrentStageNumber());
        assertEquals(LoginStage.SECURITY_NEGOTIATION, parser.getNextStageNumber());
        assertEquals((byte)0x00, parser.getMinVersion());
        assertEquals((byte)0x00, parser.getMaxVersion());

        // test ISID fields
        ISID expectedISID = new ISID(Format.OUI_FORMAT, (byte)0x00, (short)0x0000, (byte)0x00, (short)0xABCD);
        assertTrue(expectedISID.equals(parser.getInitiatorSessionID()));

        assertEquals((short)0x0000, parser.getTargetSessionIdentifyingHandle());
        assertEquals(0x00000000, parser.getConnectionID());
        assertEquals(0x00000001, parser.getCommandSequenceNumber());
        assertEquals(0x00000000, parser.getExpectedStatusSequenceNumber());
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

        SettingsMap expectedKeyValuePair = new SettingsMap();
        expectedKeyValuePair.add(OperationalTextKey.INITIATOR_NAME, "iqn.1987-05.com.cisco:01.ac24953f3f9");
        expectedKeyValuePair.add(OperationalTextKey.INITIATOR_ALIAS, "Initiator");
        expectedKeyValuePair.add(OperationalTextKey.TARGET_NAME, "disk1");
        expectedKeyValuePair.add(OperationalTextKey.SESSION_TYPE, "Normal");
        expectedKeyValuePair.add(OperationalTextKey.HEADER_DIGEST, "None,CRC32CDigest");
        expectedKeyValuePair.add(OperationalTextKey.DATA_DIGEST, "None");
        expectedKeyValuePair.add(OperationalTextKey.DEFAULT_TIME_2_WAIT, "0");
        expectedKeyValuePair.add(OperationalTextKey.DEFAULT_TIME_2_RETAIN, "0");
        expectedKeyValuePair.add(OperationalTextKey.ERROR_RECOVERY_LEVEL, "0");
        expectedKeyValuePair.add(OperationalTextKey.INITIAL_R2T, "No");
        expectedKeyValuePair.add(OperationalTextKey.IMMEDIATE_DATA, "Yes");
        expectedKeyValuePair.add(OperationalTextKey.MAX_BURST_LENGTH, "16776192");
        expectedKeyValuePair.add(OperationalTextKey.FIRST_BURST_LENGTH, "262144");
        expectedKeyValuePair.add(OperationalTextKey.MAX_OUTSTANDING_R2T, "1");
        expectedKeyValuePair.add(OperationalTextKey.MAX_CONNECTIONS, "1");
        expectedKeyValuePair.add(OperationalTextKey.DATA_PDU_IN_ORDER, "Yes");
        expectedKeyValuePair.add(OperationalTextKey.DATA_SEQUENCE_IN_ORDER, "Yes");
        expectedKeyValuePair.add(OperationalTextKey.MAX_RECV_DATA_SEGMENT_LENGTH, "131072");
        expectedKeyValuePair.add(OperationalTextKey.IF_MARKER, "No");
        expectedKeyValuePair.add(OperationalTextKey.OF_MARKER, "No");

        super.setUp(TEST_CASE_2);
        super.testDeserialize(true, true, OperationCode.LOGIN_REQUEST, 0x00000000, 0x000001AE, 0x000A0000);
        super.testDataSegment(expectedKeyValuePair);

        assertTrue(recognizedParser instanceof LoginRequestParser);

        LoginRequestParser parser = (LoginRequestParser)recognizedParser;

        // test login request fields
        assertFalse(parser.isContinueFlag());
        assertEquals(LoginStage.LOGIN_OPERATIONAL_NEGOTIATION, parser.getCurrentStageNumber());
        assertEquals(LoginStage.FULL_FEATURE_PHASE, parser.getNextStageNumber());
        assertEquals((byte)0x00, parser.getMinVersion());
        assertEquals((byte)0x00, parser.getMaxVersion());

        // test ISID fields
        ISID expectedISID = new ISID(Format.OUI_FORMAT, (byte)0x00, (short)0x23D, (byte)0x00, (short)0x0000);
        assertTrue(expectedISID.equals(parser.getInitiatorSessionID()));

        assertEquals((short)0x0000, parser.getTargetSessionIdentifyingHandle());
        assertEquals(0x00000000, parser.getConnectionID());
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

    /**
     * This test case validates the serialization process.
     * 
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     * @throws IOException
     *             This exception should be never thrown.
     */
    @Test
    public void testSerialize2() throws InternetSCSIException, IOException, DigestException {

        super.setUp(TEST_CASE_2);

        ByteBuffer expectedResult = WiresharkMessageParser.parseToByteBuffer(TEST_CASE_2);
        assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
    }

}

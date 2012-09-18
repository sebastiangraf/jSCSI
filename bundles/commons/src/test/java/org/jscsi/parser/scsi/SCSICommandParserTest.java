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
import org.jscsi.parser.scsi.SCSICommandParser.TaskAttributes;
import org.jscsi.utils.WiresharkMessageParser;

/**
 * Testing the correctness of the SCSICommandParser.
 * 
 * @author Volker Wildi
 */
public class SCSICommandParserTest extends ProtocolDataUnitTest {

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>false</code><br/>
     * Operation Code = <code>SCSI_COMMAND</code> <br/>
     * Final Flag = <code>true</code><br/>
     * ReadExpectedFlag = <code>true</code><br/>
     * WriteExpectedFlag = <code>false</code><br/>
     * TaskAttributes = <code>TaskAttributes.SIMPLE</code>><br/>
     * 
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x00000000</code><br/>
     * LUN = <code>0x0000000000000000</code><br/>
     * InitiatorTranferTag = <code>0x22000000</code><br/>
     * ExpectedDataTransferLength = <code>0x00000400</code> CmdSN = <code>0x00000022</code><br/>
     * ExpStatSN = <code>0x00000023</code><br/>
     * CDB = <code>28 00 00 00 00 3f 00 00 02 00 00 00 00 00 00 00</code><br/>
     * </blockquote>
     */
    private static final String TEST_CASE_1 = "01 c1 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "22 00 00 00 00 00 04 00 00 00 00 22 00 00 00 23 "
        + "28 00 00 00 00 3f 00 00 02 00 00 00 00 00 00 00";

    private static final String TEST_CASE_1_CDB = "28 00 00 00 00 3f 00 00 02 00 00 00 00 00 00 00";

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>false</code><br/>
     * Operation Code = <code>SCSI_COMMAND</code> <br/>
     * Final Flag = <code>true</code><br/>
     * ReadExpectedFlag = <code>false</code><br/>
     * WriteExpectedFlag = <code>true</code><br/>
     * TaskAttributes = <code>TaskAttributes.SIMPLE</code>><br/>
     * 
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x00001000</code><br/>
     * LUN = <code>0x0000000000000000</code><br/>
     * InitiatorTranferTag = <code>0x32000000</code><br/>
     * ExpectedDataTransferLength = <code>0x00001000</code> CmdSN = <code>0x00000032</code><br/>
     * ExpStatSN = <code>0x0000033</code><br/>
     * </blockquote>
     */
    private static final String TEST_CASE_2 = "01 a1 00 00 00 00 10 00 00 00 00 00 00 00 00 00 "
        + "32 00 00 00 00 00 10 00 00 00 00 32 00 00 00 33 "
        + "2a 00 00 00 10 85 00 00 08 00 00 00 00 00 00 00 ";

    private static final String TEST_CASE_2_CDB = "2a 00 00 00 10 85 00 00 08 00 00 00 00 00 00 00";

    /** Data segment to the TEST_CASE_2. */
    private static final String TEST_CASE_2_DATA_SEGMENT = "c0 3b 39 98 00 00 00 04 00 00 00 00 00 00 10 00 "
        + "00 00 10 00 00 00 00 01 00 00 00 0a 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "21 e3 bf ca 87 04 4a 2f 96 27 f9 45 0a 27 d1 58 "
        + "00 00 00 01 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ";

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
        super.testDeserialize(false, true, OperationCode.SCSI_COMMAND, 0x00000000, 0x00000000, 0x22000000);

        assertTrue(recognizedParser instanceof SCSICommandParser);

        SCSICommandParser parser = (SCSICommandParser)recognizedParser;

        // test SCSI command fields
        assertTrue(parser.isReadExpectedFlag());
        assertFalse(parser.isWriteExpectedFlag());
        assertEquals(TaskAttributes.SIMPLE, parser.getTaskAttributes());

        testCommandDescriptorBlock(parser.getCDB(), TEST_CASE_1_CDB);

        assertEquals(0x00000400, parser.getExpectedDataTransferLength());
        assertEquals(0x00000022, parser.getCommandSequenceNumber());
        assertEquals(0x00000023, parser.getExpectedStatusSequenceNumber());
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
     * This test case validates the parsing process.
     * 
     * @throws IOException
     *             This exception should be never thrown.
     * @throws InternetSCSIException
     *             This exception should be never thrown.
     */
    @Test
    public void testDeserialize2() throws IOException, InternetSCSIException, DigestException {

        super.setUp(TEST_CASE_2 + TEST_CASE_2_DATA_SEGMENT);
        super.testDeserialize(false, true, OperationCode.SCSI_COMMAND, 0x00000000, 0x00001000, 0x32000000);

        assertTrue(recognizedParser instanceof SCSICommandParser);

        SCSICommandParser parser = (SCSICommandParser)recognizedParser;

        // test SCSI command fields
        assertFalse(parser.isReadExpectedFlag());
        assertTrue(parser.isWriteExpectedFlag());
        assertEquals(TaskAttributes.SIMPLE, parser.getTaskAttributes());

        testCommandDescriptorBlock(parser.getCDB(), TEST_CASE_2_CDB);

        assertEquals(0x00001000, parser.getExpectedDataTransferLength());
        assertEquals(0x00000032, parser.getCommandSequenceNumber());
        assertEquals(0x00000033, parser.getExpectedStatusSequenceNumber());

        super.testDataSegment(TEST_CASE_2_DATA_SEGMENT);
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

        super.setUp(TEST_CASE_2 + TEST_CASE_2_DATA_SEGMENT);

        ByteBuffer expectedResult =
            WiresharkMessageParser.parseToByteBuffer(TEST_CASE_2 + TEST_CASE_2_DATA_SEGMENT);
        assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
    }

    protected final void testCommandDescriptorBlock(final ByteBuffer testCDB, final String etherealLog) {

        ByteBuffer expectedCommandDescriptorBlock = WiresharkMessageParser.parseToByteBuffer(etherealLog);
        assertTrue(expectedCommandDescriptorBlock.equals(testCDB));
    }
}

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
 * Testing the correctness of the DataOutParser.
 * 
 * @author Volker Wildi
 */
public class DataOutParserTest extends ProtocolDataUnitTest {

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>false</code><br/>
     * Operation Code = <code>SCSI_DATA_OUT</code> <br/>
     * Final Flag = <code>true</code><br/>
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x00001000</code> <br/>
     * LUN = <code>0x0000000000000000</code><br/>
     * InitiatorTaskTag = <code>0xD9220000</code><br/>
     * TargetTranferTag = <code>0xFFFFFFFF</code> <br/>
     * ExpStatSN = <code>0x00000002</code><br/>
     * DataSN = <code>0x00000000</code><br/>
     * BufferOffset = <code>0x00000000</code><br/>
     * </blockquote>
     */
    private static final String TEST_CASE_1 = "05 80 00 00 00 00 10 00 00 00 00 00 00 00 00 00 "
        + "d9 22 00 00 ff ff ff ff 00 00 00 00 00 00 01 8c "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ";

    /** Data segment to the TEST_CASE_1. */
    private static final String TEST_CASE_1_DATA_SEGMENT = "52 43 52 44 28 00 09 00 00 80 04 00 00 00 00 00 "
        + "01 00 00 00 01 00 01 00 50 06 00 00 00 00 00 00 "
        + "b7 90 00 04 00 00 00 00 41 08 00 00 00 00 00 00 "
        + "03 00 00 00 c1 01 00 00 00 00 00 00 00 00 00 00 "
        + "08 90 00 04 00 00 00 00 ed 8f 00 04 00 00 00 00 "
        + "ed 8f 00 04 00 00 00 00 98 00 00 00 00 00 00 00 "
        + "01 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "14 00 14 00 28 00 38 00 60 00 38 00 44 00 01 00 "
        + "00 00 98 04 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "82 07 11 00 00 00 00 00 50 f3 b0 d9 e0 83 c1 01 "
        + "54 19 7a ff be 87 c1 01 2a 6d a0 db 55 88 c1 01 "
        + "44 15 a3 35 55 88 c1 01 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 26 00 00 10 00 00 00 00 "
        + "50 f3 b0 d9 e0 83 c1 01 54 19 7a ff be 87 c1 01 "
        + "dc 8b 99 35 55 88 c1 01 44 15 a3 35 55 88 c1 01 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "26 00 00 10 00 00 00 00 21 90 00 04 00 00 00 00 "
        + "08 90 00 04 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "28 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 1b 00 01 00 28 00 00 00 "
        + "28 00 08 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 04 d4 ef be 28 4c 2f e2 "
        + "2c 90 00 04 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 70 00 00 00 00 00 00 00 "
        + "01 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "06 00 05 00 28 00 00 00 28 00 48 00 18 00 01 00 "
        + "40 01 00 00 02 00 00 00 01 00 00 00 00 00 00 00 "
        + "05 00 00 00 00 00 00 00 80 00 00 00 48 00 00 00 "
        + "00 17 18 00 00 00 0d 00 00 00 00 00 48 00 00 00 "
        + "24 00 4d 00 6f 00 75 00 6e 00 74 00 4d 00 67 00 "
        + "72 00 52 00 65 00 6d 00 6f 00 74 00 65 00 44 00 "
        + "61 00 74 00 61 00 62 00 61 00 73 00 65 00 41 08 "
        + "40 90 00 04 00 00 00 00 2c 90 00 04 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 28 00 00 00 00 00 00 00 "
        + "01 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "1b 00 01 00 28 00 00 00 28 00 08 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "40 01 00 00 02 00 00 00 4b 90 00 04 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "98 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 14 00 14 00 28 00 38 00 "
        + "60 00 38 00 44 00 01 00 00 00 98 04 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 82 07 11 00 00 00 00 00 "
        + "50 f3 b0 d9 e0 83 c1 01 54 19 7a ff be 87 c1 01 "
        + "2a 6d a0 db 55 88 c1 01 44 15 a3 35 55 88 c1 01 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "26 00 00 10 00 00 00 00 50 f3 b0 d9 e0 83 c1 01 "
        + "54 19 7a ff be 87 c1 01 2a 6d a0 db 55 88 c1 01 "
        + "44 15 a3 35 55 88 c1 01 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 26 00 00 10 00 00 00 00 "
        + "64 90 00 04 00 00 00 00 4b 90 00 04 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 28 00 00 00 00 00 00 00 "
        + "01 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "1b 00 01 00 28 00 00 00 28 00 08 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 78 c3 98 04 78 c3 6f 90 00 04 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "a0 01 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 1d 00 00 00 28 00 78 01 "
        + "a0 01 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 50 b9 04 f2 ec c1 eb bf "
        + "2c 00 08 00 02 00 00 00 00 00 00 00 ff ff ff ff "
        + "70 00 00 00 4c 01 00 00 ff ff ff ff 18 00 00 00 "
        + "00 00 00 00 00 00 01 00 a2 8f 00 04 00 00 41 08 "
        + "00 00 00 00 80 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 ff ff ff ff 40 00 00 00 05 00 00 00 "
        + "00 00 05 00 da 8f 00 04 00 00 00 00 00 00 00 00 "
        + "a0 00 00 00 00 00 00 00 00 00 00 00 00 10 00 00 "
        + "9c 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 c8 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 f4 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 20 01 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "4c 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 a9 90 00 04 00 00 00 00 "
        + "6f 90 00 04 00 00 00 00 6f 90 00 04 00 00 00 00 "
        + "40 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 1e 00 00 00 28 00 12 00 "
        + "40 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 50 b9 04 f2 ec c1 eb bf "
        + "44 00 08 00 24 00 49 00 33 00 30 00 00 00 00 00 "
        + "00 00 4c 45 2a 00 03 00 b7 90 00 04 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "68 00 00 00 00 00 00 00 02 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "64 90 00 04 00 00 00 00 6f 90 00 04 00 00 41 08 "
        + "a9 90 00 04 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 78 01 00 00 12 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "84 8f 00 04 00 00 00 00 00 10 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "58 00 00 00 18 00 01 00 61 00 00 00 00 00 02 00 "
        + "fe d3 72 2d bb 87 c1 01 fe d3 72 2d bb 87 c1 01 "
        + "fe d3 72 2d bb 87 c1 01 fe d3 72 2d bb 87 c1 01 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "20 00 00 00 00 00 00 00 0b 03 50 00 4b 00 47 00 "
        + "49 00 4e 00 53 00 54 00 2e 00 45 00 58 00 45 00 "
        + "80 00 00 00 18 00 00 00 00 00 18 00 00 00 01 00 "
        + "00 00 00 00 18 00 00 00 ff ff ff ff 82 79 47 11 "
        + "da 90 00 03 00 00 00 00 ab 90 00 03 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 28 00 00 00 00 00 00 00 "
        + "01 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "1b 00 01 00 28 00 00 00 28 00 08 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "8c 57 7e be 02 00 00 00 e5 90 00 03 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "40 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 06 00 05 00 28 00 00 00 "
        + "28 00 18 00 18 00 01 00 00 01 00 00 02 00 00 00 "
        + "1d 00 00 00 00 00 00 00 21 00 00 00 00 00 00 00 "
        + "80 00 00 00 18 00 00 00 00 00 18 00 00 00 01 00 "
        + "00 00 00 00 18 00 00 00 f3 90 00 03 00 00 00 00 "
        + "e5 90 00 03 00 00 00 00 e5 90 00 03 00 00 00 00 "
        + "70 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 05 00 06 00 28 00 48 00 "
        + "70 00 00 00 18 00 01 00 00 01 00 00 02 00 00 00 "
        + "1d 00 00 00 00 00 00 00 21 00 00 00 00 00 00 00 "
        + "80 00 00 00 48 00 00 00 01 00 00 00 00 00 41 08 "
        + "00 00 00 00 00 00 00 00 ff ff ff ff ff ff ff ff "
        + "40 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 99 27 e1 f0 90 c4 85 07 91 00 03 00 00 00 00 "
        + "f3 90 00 03 00 00 00 00 f3 90 00 03 00 00 00 00 "
        + "30 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 15 00 16 00 28 00 08 00 "
        + "28 00 08 00 4c 01 01 00 00 00 00 00 00 00 00 00 "
        + "08 00 00 00 00 00 00 00 8b 07 11 00 00 00 00 00 "
        + "7d 62 00 00 80 00 00 00 13 91 00 03 00 00 00 00 "
        + "07 91 00 03 00 00 00 00 07 91 00 03 00 00 00 00 "
        + "58 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 0b 00 0b 00 28 00 18 00 "
        + "40 00 18 00 18 00 01 00 00 01 00 00 02 00 00 00 "
        + "1d 00 00 00 00 00 00 00 21 00 00 00 00 00 00 00 "
        + "00 00 08 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "24 91 00 03 00 00 00 00 13 91 00 03 00 00 00 00 "
        + "13 91 00 03 00 00 00 00 38 00 00 00 00 00 00 00 "
        + "01 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "09 00 09 00 28 00 08 00 30 00 08 00 18 00 01 00 "
        + "00 01 40 00 02 00 00 00 1d 00 00 00 00 00 00 00 "
        + "21 00 00 00 00 00 00 00 32 80 00 7d 62 04 00 00 "
        + "00 99 27 e1 f0 90 c4 85 31 91 00 03 00 00 00 00 "
        + "24 91 00 03 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "28 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 1b 00 01 00 28 00 00 00 "
        + "28 00 08 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 b8 36 08 e2 08 00 b3 85 "
        + "3c 91 00 03 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 58 00 00 00 00 00 41 08 "
        + "01 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "0b 00 0b 00 28 00 18 00 40 00 18 00 18 00 01 00 "
        + "00 01 00 00 02 00 00 00 1d 00 00 00 00 00 00 00 "
        + "21 00 00 00 00 00 00 00 00 00 08 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 7a 00 00 00 00 00 00 "
        + "00 00 08 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 4d 91 00 03 00 00 00 00 "
        + "3c 91 00 03 00 00 00 00 3c 91 00 03 00 00 00 00 "
        + "a8 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 07 00 07 00 28 00 40 00 "
        + "68 00 40 00 18 00 01 00 30 00 20 00 02 00 00 00 "
        + "1d 00 00 00 00 00 00 00 21 00 00 00 00 00 00 00 "
        + "74 84 83 2d bb 87 c1 01 74 84 83 2d bb 87 c1 01 "
        + "74 84 83 2d bb 87 c1 01 20 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 03 01 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "fe d3 72 2d bb 87 c1 01 fe d3 72 2d bb 87 c1 01 "
        + "fe d3 72 2d bb 87 c1 01 20 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 03 01 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "68 91 00 03 00 00 00 00 4d 91 00 03 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 28 00 00 00 00 00 00 00 "
        + "01 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "1b 00 01 00 28 00 00 00 28 00 08 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 0b 4c 41 80 73 91 00 03 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "98 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 14 00 14 00 28 00 38 00 "
        + "60 00 38 00 fc 01 01 00 00 00 b0 05 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 df 48 04 00 00 00 00 00 "
        + "fe d3 72 2d bb 87 c1 01 74 84 83 2d bb 87 41 08 "
        + "74 84 83 2d bb 87 c1 01 74 84 83 2d bb 87 c1 01 "
        + "00 00 08 00 00 00 00 00 00 7a 00 00 00 00 00 00 "
        + "20 00 00 00 00 00 00 00 fe d3 72 2d bb 87 c1 01 "
        + "fe d3 72 2d bb 87 c1 01 fe d3 72 2d bb 87 c1 01 "
        + "fe d3 72 2d bb 87 c1 01 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 20 00 00 00 00 00 00 00 "
        + "8c 91 00 03 00 00 00 00 73 91 00 03 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 28 00 00 00 00 00 00 00 "
        + "01 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "1b 00 01 00 28 00 00 00 28 00 08 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 97 91 00 03 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "30 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 16 00 15 00 28 00 08 00 "
        + "28 00 08 00 4c 01 01 00 00 00 00 00 00 00 00 00 "
        + "08 00 00 00 00 00 00 00 8b 07 11 00 00 00 00 00 "
        + "85 62 00 00 78 00 00 00 a3 91 00 03 00 00 00 00 "
        + "97 91 00 03 00 00 00 00 97 91 00 03 00 00 00 00 "
        + "38 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 09 00 09 00 28 00 08 00 "
        + "30 00 08 00 18 00 01 00 00 01 40 00 02 00 00 00 "
        + "1d 00 00 00 00 00 00 00 21 00 00 00 00 00 00 00 "
        + "31 08 7d 62 04 00 c4 85 32 80 00 7d 62 04 00 00 "
        + "b0 91 00 03 00 00 00 00 a3 91 00 03 00 00 00 00 "
        + "a3 91 00 03 00 00 00 00 58 00 00 00 00 00 00 00 "
        + "01 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "0b 00 0b 00 28 00 18 00 40 00 18 00 18 00 01 00 "
        + "00 01 00 00 02 00 00 00 1d 00 00 00 00 00 00 00 "
        + "21 00 00 00 00 00 00 00 00 80 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 7a 00 00 00 00 00 00 "
        + "00 00 08 00 00 00 00 00 00 00 00 00 00 00 41 08 "
        + "00 7a 00 00 00 00 00 00 c1 91 00 03 00 00 00 00 "
        + "b0 91 00 03 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "28 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 1b 00 01 00 28 00 00 00 "
        + "28 00 08 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 0b 4c 41 80 "
        + "cc 91 00 03 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 98 00 00 00 00 00 00 00 "
        + "01 00 00 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "14 00 14 00 28 00 38 00 60 00 38 00 fc 01 01 00 "
        + "00 00 b0 05 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "df 48 04 00 00 00 00 00 fe d3 72 2d bb 87 c1 01 "
        + "74 84 83 2d bb 87 c1 01 74 84 83 2d bb 87 c1 01 "
        + "74 84 83 2d bb 87 c1 01 00 80 00 00 00 00 00 00 "
        + "00 7a 00 00 00 00 00 00 20 00 00 00 00 00 00 00 "
        + "fe d3 72 2d bb 87 c1 01 74 84 83 2d bb 87 c1 01 "
        + "74 84 83 2d bb 87 c1 01 74 84 83 2d bb 87 c1 01 "
        + "00 00 08 00 00 00 00 00 00 7a 00 00 00 00 00 00 "
        + "20 00 00 00 00 00 00 00 e5 91 00 03 00 00 00 00 "
        + "cc 91 00 03 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "28 00 00 00 00 00 00 00 01 00 00 00 18 00 00 00 "
        + "00 00 00 00 00 00 00 00 1b 00 01 00 28 00 00 00 "
        + "28 00 08 00 18 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "f0 91 00 03 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 a8 00 00 00 00 00 00 00 "
        + "01 00 00 00 18 00 00 00 01 00 00 00 00 00 00 00 "
        + "07 00 07 00 28 00 40 00 68 00 40 00 18 00 01 00 "
        + "30 00 20 00 02 00 00 00 1d 00 00 00 00 00 00 00 "
        + "21 00 00 00 00 00 00 00 00 81 d6 a2 77 6f bd 01 "
        + "74 84 83 2d bb 87 c1 01 74 84 83 2d bb 87 c1 01 "
        + "20 00 00 00 00 00 00 00 00 00 00 00 00 00 41 08 ";

    /**
     * Valid Test Case with the following expected values. <blockquote>
     * Immediate Flag = <code>false</code><br/>
     * Operation Code = <code>SCSI_DATA_OUT</code> <br/>
     * Final Flag = <code>true</code><br/>
     * TotalAHSLength = <code>0x00000000</code><br/>
     * DataSegmentLength = <code>0x00000200</code> <br/>
     * LUN = <code>0x0006000000000000</code><br/>
     * InitiatorTaskTag = <code>0x0001A27F</code><br/>
     * TargetTranferTag = <code>0x0000004F</code> <br/>
     * ExpStatSN = <code>0x0001A280</code><br/>
     * DataSN = <code>0x00000000</code><br/>
     * BufferOffset = <code>0x00000000</code><br/>
     * </blockquote>
     */
    private static final String TEST_CASE_2 = "05 80 00 00 00 00 02 00 00 06 00 00 00 00 00 00 "
        + "00 01 a2 7f 00 00 00 4f 00 00 00 00 00 01 a2 80 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 ";

    /** Data segment to the TEST_CASE_2. */
    private static final String TEST_CASE_2_DATA_SEGMENT = "f0 12 03 f8 00 01 00 00 00 00 00 00 00 00 00 00 "
        + "7c 00 00 00 01 00 04 80 48 00 00 00 58 00 00 00 "
        + "00 00 00 00 14 00 00 00 02 00 34 00 02 00 00 00 "
        + "00 00 14 00 89 00 12 00 01 01 00 00 00 00 00 05 "
        + "12 00 00 00 00 00 18 00 89 00 12 00 01 02 00 00 "
        + "00 00 00 05 20 00 00 00 20 02 00 00 01 02 00 00 "
        + "00 00 00 05 20 00 00 00 20 02 00 00 01 02 00 00 "
        + "00 00 00 05 20 00 00 00 20 02 00 00 00 00 00 00 "
        + "51 24 b3 00 01 01 00 00 80 00 00 00 00 00 00 00 "
        + "7c 00 00 00 01 00 04 80 48 00 00 00 58 00 00 00 "
        + "00 00 00 00 14 00 00 00 02 00 34 00 02 00 00 00 "
        + "00 00 14 00 9f 01 12 00 01 01 00 00 00 00 00 05 "
        + "12 00 00 00 00 00 18 00 9f 01 12 00 01 02 00 00 "
        + "00 00 00 05 20 00 00 00 20 02 00 00 01 02 00 00 "
        + "00 00 00 05 20 00 00 00 20 02 00 00 01 02 00 00 "
        + "00 00 00 05 20 00 00 00 20 02 00 00 00 00 00 00 "
        + "62 9b 9f 0a 02 01 00 00 00 01 00 00 00 00 00 00 "
        + "60 00 00 00 01 00 04 80 30 00 00 00 40 00 00 00 "
        + "00 00 00 00 14 00 00 00 02 00 1c 00 01 00 00 00 "
        + "00 03 14 00 ff 01 1f 00 01 01 00 00 00 00 00 05 "
        + "12 00 00 00 01 02 00 00 00 00 00 05 20 00 00 00 "
        + "20 02 00 00 01 01 00 00 00 00 00 05 12 00 00 00 "
        + "62 95 9f 0a 03 01 00 00 60 01 00 00 00 00 00 00 "
        + "60 00 00 00 01 00 04 80 30 00 00 00 40 00 00 00 "
        + "00 00 00 00 14 00 00 00 02 00 1c 00 01 00 00 00 "
        + "00 00 14 00 ff 01 1f 00 01 01 00 00 00 00 00 05 "
        + "12 00 00 00 01 02 00 00 00 00 00 05 20 00 00 00 "
        + "20 02 00 00 01 01 00 00 00 00 00 05 12 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 "
        + "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00";

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

        super.setUp(TEST_CASE_1 + TEST_CASE_1_DATA_SEGMENT);
        super.testDeserialize(false, true, OperationCode.SCSI_DATA_OUT, 0x00000000, 0x00001000, 0xD9220000);

        assertTrue(recognizedParser instanceof DataOutParser);

        DataOutParser parser = (DataOutParser)recognizedParser;

        assertEquals(0x0000000000000000L, parser.getLogicalUnitNumber());

        assertEquals(0xFFFFFFFF, parser.getTargetTransferTag());
        assertEquals(0x0000018C, parser.getExpectedStatusSequenceNumber());
        assertEquals(0x00000000, parser.getDataSequenceNumber());
        assertEquals(0x00000000, parser.getBufferOffset());

        super.testDataSegment(TEST_CASE_1_DATA_SEGMENT);
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
    public void testSerialize1() throws InternetSCSIException, IOException, DigestException {

        super.setUp(TEST_CASE_1 + TEST_CASE_1_DATA_SEGMENT);

        ByteBuffer expectedResult =
            WiresharkMessageParser.parseToByteBuffer(TEST_CASE_1 + TEST_CASE_1_DATA_SEGMENT);
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

        super.setUp(TEST_CASE_2 + TEST_CASE_2_DATA_SEGMENT);
        super.testDeserialize(false, true, OperationCode.SCSI_DATA_OUT, 0x00000000, 0x00000200, 0x0001A27F);

        assertTrue(recognizedParser instanceof DataOutParser);

        DataOutParser parser = (DataOutParser)recognizedParser;

        assertEquals(0x0006000000000000L, parser.getLogicalUnitNumber());

        assertEquals(0x0000004F, parser.getTargetTransferTag());
        assertEquals(0x0001A280, parser.getExpectedStatusSequenceNumber());
        assertEquals(0x00000000, parser.getDataSequenceNumber());
        assertEquals(0x00000000, parser.getBufferOffset());

        super.testDataSegment(TEST_CASE_2_DATA_SEGMENT);
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

        super.setUp(TEST_CASE_2 + TEST_CASE_2_DATA_SEGMENT);

        ByteBuffer expectedResult =
            WiresharkMessageParser.parseToByteBuffer(TEST_CASE_2 + TEST_CASE_2_DATA_SEGMENT);
        assertTrue(expectedResult.equals(protocolDataUnit.serialize()));
    }
}

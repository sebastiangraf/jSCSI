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
// /*
// * Copyright 2007 Marc Kramis
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// *
// * $Id: SCSIDataSegmentTest.java 2500 2007-03-05 13:29:08Z lemke $
// *
// */
// package org.jscsi.parser.datasegment;
//
// import java.io.IOException;
// import java.nio.ByteBuffer;
//
// import junit.framework.TestCase;
// import org.jscsi.parser.Constants;
//
// /**
// * Testing the correctness of the SCSIResponseDataSegment.
// *
// * @author Volker Wildi
// *
// */
// public class SCSIDataSegmentTest {
//
// // private SCSIResponseDataSegment responseDataSegment = new
// SCSIResponseDataSegment();
//
// /**
// * Valid Test Case with the following expected values.
// *
// * <blockquote>
// *
// * Sense Length = <code>0x0012</code><br/>
// *
// * Sense Data = <code>{ 0x0000f000, 0x05000000, 0x000a0000,
// 0x00002400, 0x00c00100 }</code><br/>
// *
// * Response Data = <code>null</code><br/>
// *
// * </blockquote>
// */
// private static final byte[] TEST_CASE_1 = { 0x00, 0x12, (byte) 0xf0, 0x00,
// 0x05, 0x00, 0x00, 0x00, 0x00, 0x0a, 0x00,
// 0x00, 0x00, 0x00, 0x24, 0x00, 0x00, (byte) 0xc0, 0x01, 0x00 };
//
// /** The expected sense data of <code>TEST_CASE_1</code>. */
// private static final int[] TEST_CASE_1_SENSE_DATA = { 0x0000f000, 0x05000000,
// 0x000a0000, 0x00002400, 0x00c00100 };
//
// /**
// * This test case validates the parsing process.
// *
// * @throws IOException
// * This exception should be never thrown.
// */
// public void testDeserialize1() throws IOException {
//
// ByteBuffer expectedResult = initilizeBuffer(TEST_CASE_1);
// responseDataSegment.deserialize(expectedResult, 0);
//
// ByteBuffer expectedSenseDataBuffer = initilizeBuffer(TEST_CASE_1_SENSE_DATA);
// expectedSenseDataBuffer.position(2);
//
// assertEquals(0x0012, responseDataSegment.getSenseLength());
// assertTrue(expectedSenseDataBuffer.equals(responseDataSegment.getSenseData()))
// ;
// assertNull(responseDataSegment.getResponseData());
// }
//
// /**
// * This test case validates the serialization process.
// *
// * @throws IOException
// * This exception should be never thrown.
// */
// public void testSerialize1() throws IOException {
//
// ByteBuffer expectedResult = initilizeBuffer(TEST_CASE_1);
// responseDataSegment.deserialize(expectedResult, 0);
//
// ByteBuffer result = ByteBuffer.allocate(responseDataSegment.getLength());
// responseDataSegment.serialize(result, 0);
//
// assertTrue(expectedResult.equals(result));
// }
//
// /**
// * Creates a new <code>ByteBuffer</code> instance, which is initilizes with
// * the given <code>byte</code> array.
// *
// * @param inits
// * The <code>byte</code> array, which is used for the
// * initialization.
// * @return The initialized <code>ByteBuffer</code> instance.
// */
// private ByteBuffer initilizeBuffer(final byte[] inits) {
//
// ByteBuffer buffer = ByteBuffer.allocate(inits.length);
//
// for (byte i : inits) {
// buffer.put(i);
// }
//
// return (ByteBuffer) buffer.rewind();
// }
//
// /**
// * Creates a new <code>ByteBuffer</code> instance, which is initilizes with
// * the given <code>int</code> array.
// *
// * @param inits
// * The <code>int</code> array, which is used for the
// * initialization.
// * @return The initialized <code>ByteBuffer</code> instance.
// */
// private ByteBuffer initilizeBuffer(final int[] inits) {
//
// ByteBuffer buffer = ByteBuffer.allocate(inits.length *
// Constants.BYTES_PER_INT);
//
// for (int i : inits) {
// buffer.putInt(i);
// }
//
// return (ByteBuffer) buffer.rewind();
// }
//
// }

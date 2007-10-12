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
// * $Id: SCSIResponseDataSegment.java 2498 2007-03-05 12:32:43Z lemke $
// *
// */
// package org.jscsi.parser.datasegment;
//
// import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;
// import java.nio.ByteBuffer;
//
// /**
// * <h1>SCSIResponseDataSegment</h1>
// * <p>
// * This class parses a data segment of a SCSI Response message defined in the
// * iSCSI Standard (RFC3720).
// *
// * @author Volker Wildi
// */
// public final class SCSIResponseDataSegment extends DataSegment {
//
// private static final int SENSE_DATA_OFFSET = 2;
//
// /** The length of the sense data. */
// private short senseLength;
//
// /** The sense data <code>ByteBuffer</code>. */
// private ByteBuffer senseData;
//
// /** The response data <code>ByteBuffer</code>. */
// private ByteBuffer responseData;
//
// /**
// * Constructor to create a new, empty <code>SCSIResponseDataSegment</code>
// * object with the given length.
// *
// * @param initLength
// * The length for this <code>SCSIResponseDataSegment</code> object.
// */
// public SCSIResponseDataSegment(final int initLength, final int initChunkSIze)
// {
//
// super(initLength, initChunkSIze);
// }
//
// /** {@inheritDoc}*/@Override/
// public final int deserialize(final ByteBuffer src, final int offset, final
// int length) {
//
// src.position(offset);
//
// senseLength = src.getShort();
//
// senseData = ByteBuffer.allocate(senseLength);
// senseData.putShort(src.getShort());
//
// while (senseData.hasRemaining()) {
// senseData.put(src.get());
// }
//
// int resLen = src.limit() - senseLength - SENSE_DATA_OFFSET - offset;
//
// if (resLen > 0) {
// responseData = ByteBuffer.allocate(resLen);
//
// while (responseData.hasRemaining()) {
// responseData.put(src.get());
// }
// } else {
// responseData = null;
// }
//
// update();
//
// return src.position() - offset;
// }
//
// /** {@inheritDoc}*/@Override/ public final int serialize(final ByteBuffer
// dst, final int offset) {
//
// dst.position(offset);
// update();
//
// // calculate needed size (2 bytes are needed for the senseLength variable).
// int length = SENSE_DATA_OFFSET;
// if (senseData != null) {
// length += senseLength;
// }
// if (responseData != null) {
// length += responseData.limit();
// }
//
// // the length contains only the length of the senseLength variable -> so
// // nothing has to be serialized.
// if (length == SENSE_DATA_OFFSET) {
// return 0;
// }
//
// if (dst.remaining() < length) {
// throw new IllegalArgumentException("Destination buffer is too small.");
// }
//
// dst.putShort(senseLength);
// if (getSenseData() != null) {
// dst.putShort(senseData.getShort());
//
// while (senseData.hasRemaining()) {
// dst.put(senseData.get());
// }
// }
//
// if (getResponseData() != null) {
// while (responseData.hasRemaining()) {
// dst.put(responseData.get());
// }
// }
//
// return dst.position() - offset;
// }
//
// /**
// * Returns the response data array of this
// * <code>SCSIResponseDataSegment</code> object.
// *
// * @return The response data <code>ByteBuffer</code> of this
// * <code>SCSIResponseDataSegment</code> object. Or <code>null</code>,
// * if there does not exist a data segment.
// */
// public final ByteBuffer getResponseData() {
//
// if (responseData == null) {
// return null;
// }
// return (ByteBuffer) responseData.rewind();
// }
//
// /**
// * The Sense Data contains detailed information about a check condition and
// * [SPC3] specifies the format and content of the Sense Data.
// * <p>
// * Certain iSCSI conditions result in the command being terminated at the
// * target (response Command Completed at Target) with a SCSI Check Condition
// * Status as outlined in the next table:
// * <p>
// * <table border="1">
// * <tr>
// * <th>iSCSI Condition</th>
// * <th>Sense Key</th>
// * <th>Additional Sense Code &amp; Qualifier</th>
// * </tr>
// * <tr>
// * <td> Unexpected unsolicited data</td>
// * <td>Aborted Command-0B</td>
// * <td>ASC = 0x0c ASCQ = 0x0c<br/>Write Error</td>
// * </tr>
// * <tr>
// * <td> Incorrect amount of data</td>
// * <td>Aborted Command-0B</td>
// * <td>ASC = 0x0c ASCQ = 0x0d<br/>Write Error</td>
// * </tr>
// * <tr>
// * <td>Protocol Service CRC error</td>
// * <td>Aborted Command-0B</td>
// * <td>ASC = 0x47 ASCQ = 0x05<br/>CRC Error Detected</td>
// * </tr>
// * <tr>
// * <td>SNACK rejected</td>
// * <td>Aborted Command-0B</td>
// * <td>ASC = 0x11 ASCQ = 0x13<br/>Read Error</td>
// * </tr>
// * </table>
// * <p>
// * The target reports the "Incorrect amount of data" condition if during data
// * output the total data length to output is greater than FirstBurstLength and
// * the initiator sent unsolicited non-immediate data but the total amount of
// * unsolicited data is different than FirstBurstLength. The target reports the
// * same error when the amount of data sent as a reply to an R2T does not match
// * the amount requested.
// *
// * @return The sense data <code>ByteBuffer</code> of this
// * <code>SCSIResponseDataSegment</code> object.
// */
// public final ByteBuffer getSenseData() {
//
// return (ByteBuffer) senseData.rewind();
// }
//
// /**
// * Length of Sense Data.
// *
// * @return The length of the sense data of this
// * <code>SCSIResponseDataSegment</code> object.
// */
// public final short getSenseLength() {
//
// return senseLength;
// }
//
// /** {@inheritDoc}*/@Override/ public DataSegmentFormat getFormat() {
//
// return DataSegmentFormat.SCSI_RESPONSE;
// }
//
// /** {@inheritDoc}*/@Override/ public void update() {
//
// super.update();
//
// length = SENSE_DATA_OFFSET;
// length += getSenseLength();
//
// if (responseData != null) {
// length += responseData.limit();
// }
// }
//
// }

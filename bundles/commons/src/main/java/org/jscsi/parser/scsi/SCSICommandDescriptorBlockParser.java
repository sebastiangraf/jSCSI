/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
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

import java.nio.ByteBuffer;

/**
 * <h1>SCSICommandDescriptorBlockParser</h1> <p/> This class generates a SCSI
 * Command Descriptor Block.
 * 
 * @author Volker Wildi
 */
public final class SCSICommandDescriptorBlockParser {

  /**
   * @author Volker Wildi
   */
  // private static enum GroupCode {
  // /** */
  // SIX_BYTE_COMMAND((byte) 0x06),
  // /** */
  // TEN_BYTE_COMMAND((byte) 0x0A),
  // /** */
  // TWELVE_BYTE_COMMAND((byte) 0x0C),
  // /** */
  // SIXTEEN_BYTE_COMMAND((byte) 0x10),
  // /** */
  // VENDOR_SPECIFIC((byte) 0x00);
  //
  // private byte value;
  //
  // private static Map<Byte, GroupCode> mapping;
  //
  // private GroupCode(final byte newValue) {
  //
  // if (GroupCode.mapping == null) {
  // GroupCode.mapping = new HashMap<Byte, GroupCode>();
  // }
  //
  // GroupCode.mapping.put(newValue, this);
  // value = newValue;
  // }
  //
  // /**
  // * Returns the value of this enumeration.
  // *
  // * @return The value of this enumeration.
  // */
  // public final byte value() {
  //
  // return value;
  // }
  //
  // /**
  // * Returns the constant defined for the given <code>value</code>.
  // *
  // * @param value
  // * The value to search for.
  // * @return The constant defined for the given <code>value</code>. Or
  // * <code>null</code>, if this value is not defined by this
  // * enumeration.
  // */
  // public static final GroupCode valueOf(final byte value) {
  //
  // return GroupCode.mapping.get(value);
  // }
  //
  // }
  //
  // //
  // --------------------------------------------------------------------------
  // //
  // --------------------------------------------------------------------------
  /** The default or minimal length of a CDB is <code>16</code> bytes. */
  private static final int DEFAULT_CDB_LENGTH = 16;

  // private static final int STANDARD_BYTES = 2;
  //
  // private static final int GROUP_CODE_SHIFT = 5;
  //
  // private static final byte GROUP_CODE_MASK = (byte) 0xE0;
  //
  // private static final byte COMMAND_CODE_MASK = 0x1F;
  //
  // private static final byte NACA_FLAG_MASK = 0x04;
  //
  // private static final byte LINK_FLAG_MASK = 0x01;
  //
  // //
  // --------------------------------------------------------------------------
  // //
  // --------------------------------------------------------------------------
  //
  // private byte opCode;
  //
  // private ByteBuffer specificParameters;
  //
  // private byte control;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  private SCSICommandDescriptorBlockParser() {

    super();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  // public final int serialize(final ByteBuffer dst, final int offset) {
  //
  // dst.position(offset);
  //
  // if (dst.remaining() < getTotalLength()) {
  // throw new IllegalArgumentException("Destination buffer is too small.");
  // }
  //
  // dst.put(serializeOperationCodeByte());
  //
  // for (byte b = 0; b < getLength().value(); b++) {
  // dst.put(serializeSpecificParameters());
  // }
  //
  // dst.put(serializeControlByte());
  //
  // return dst.position() - offset;
  // }
  //
  // public final int deserialize(final ByteBuffer src, final int offset) {
  //
  // src.position(offset);
  //
  // if (src.remaining() < DEFAULT_CDB_LENGTH) {
  // throw new IllegalArgumentException("Source buffer is too small.");
  // }
  //
  // deserializeOperationCodeByte(src.get());
  //
  // specificParameters = ByteBuffer.allocate(getLength().value());
  // for (byte b = 0; b < getLength().value(); b++) {
  // deserializeSpecificParameters(src.get());
  // }
  //
  // deserializeControlByte(src.get());
  //
  // return src.position() - offset;
  // }
  //
  // //
  // --------------------------------------------------------------------------
  // //
  // --------------------------------------------------------------------------
  //
  // public final byte getGroupCode() {
  //
  // return (byte) (opCode & GROUP_CODE_MASK);
  // }
  //
  // public final byte getCommandCode() {
  //
  // return (byte) (opCode & COMMAND_CODE_MASK);
  // }
  //
  // public final boolean isLinkFlag() {
  //
  // return Utils.isBitSet(control & LINK_FLAG_MASK);
  // }
  //
  // public final void setLinkFlag(final boolean newLinkFlag) {
  //
  // if (newLinkFlag) {
  // control |= LINK_FLAG_MASK;
  // } else {
  // control &= ~LINK_FLAG_MASK;
  // }
  // }
  //
  // public final boolean isNACAFlag() {
  //
  // return Utils.isBitSet(control & NACA_FLAG_MASK);
  // }
  //
  // public final void setNacaFlag(final boolean newNACAFlag) {
  //
  // if (newNACAFlag) {
  // control |= NACA_FLAG_MASK;
  // } else {
  // control &= ~NACA_FLAG_MASK;
  // }
  // }
  //
  // public final GroupCode getLength() {
  //
  // GroupCode groupCode;
  //
  // switch (getGroupCode()) {
  // case 0x00:
  // groupCode = GroupCode.SIX_BYTE_COMMAND;
  // break;
  //
  // case 0x01:
  // case 0x02:
  // groupCode = GroupCode.TEN_BYTE_COMMAND;
  // break;
  //
  // case 0x04:
  // groupCode = GroupCode.SIXTEEN_BYTE_COMMAND;
  // break;
  //
  // case 0x05:
  // groupCode = GroupCode.TWELVE_BYTE_COMMAND;
  // break;
  //
  // case 0x06:
  // case 0x07:
  // groupCode = GroupCode.VENDOR_SPECIFIC;
  //
  // default:
  // groupCode = null;
  // }
  //
  // return groupCode;
  // }
  //
  // // public final void setLength(final GroupCode groupCode) {
  // // WRONG
  // // opCode |= groupCode.value() << GROUP_CODE_SHIFT;
  // // }
  //
  // public final int getTotalLength() {
  //
  // return getLength().value() + STANDARD_BYTES;
  // }
  //
  // //
  // --------------------------------------------------------------------------
  // //
  // --------------------------------------------------------------------------
  //
  // protected void deserializeOperationCodeByte(final byte b) {
  //
  // opCode = b;
  // }
  //
  // protected void deserializeSpecificParameters(final byte b) {
  //
  // specificParameters.put(b);
  // }
  //
  // protected void deserializeControlByte(final byte b) {
  //
  // control = b;
  // }
  //
  // protected byte serializeOperationCodeByte() {
  //
  // return opCode;
  // }
  //
  // protected byte serializeSpecificParameters() {
  //
  // return specificParameters.get();
  // }
  //
  // protected byte serializeControlByte() {
  //
  // return control;
  // }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The Read Operation Code. */
  private static final byte READ_OP_CODE = 0x28;

  /** The Read Capacity Operation Code. */
  private static final byte READ_CAPACITY_OP_CODE = 0x25;

  /** The Write Operation Code. */
  private static final byte WRITE_OP_CODE = 0x2A;

  /** The byte index of the logical block address information. */
  private static final int LOGICAL_BLOCK_ADDRESS_OFFSET = 2;

  /** The byte index of the transfer length information. */
  private static final int TRANSFER_LENGTH_OFFSET = 7;

  /**
   * Creates the Command Descriptor Block for a Read Message.
   * 
   * @param logicalBlockAddress
   *          The Logical Block Address to begin the read operation.
   * @param transferLength
   *          The transfer length field specifies the number of contiguous
   *          logical blocks of data to be transferred. A transfer length of
   *          zero indicates that <code>256</code> logical blocks shall be
   *          transferred. Any other value indicates the number of logical
   *          blocks that shall be transferred.
   * @return A <code>ByteBuffer</code> object with the above data.
   */
  public static final ByteBuffer createReadMessage(
      final int logicalBlockAddress, final short transferLength) {

    return createReadWriteMessage(READ_OP_CODE, logicalBlockAddress,
        transferLength);
  }

  /**
   * Creates the Command Descriptor Block for a Write Message.
   * 
   * @param logicalBlockAddress
   *          The Logical Block Address to begin the read operation.
   * @param transferLength
   *          The transfer length field specifies the number of contiguous
   *          logical blocks of data to be transferred. A transfer length of
   *          zero indicates that 256 logical blocks shall be transferred. Any
   *          other value indicates the number of logical blocks that shall be
   *          transferred.
   * @return A <code>ByteBuffer</code> object with the above data.
   */
  public static final ByteBuffer createWriteMessage(
      final int logicalBlockAddress, final short transferLength) {

    return createReadWriteMessage(WRITE_OP_CODE, logicalBlockAddress,
        transferLength);
  }

  /**
   * Creates the Command Descriptor Block for a given Operation Message.
   * 
   * @param opCode
   *          The Operation Code.
   * @param logicalBlockAddress
   *          The Logical Block Address to begin the read operation.
   * @param transferLength
   *          The transfer length field specifies the number of contiguous
   *          logical blocks of data to be transferred. A transfer length of
   *          zero indicates that 256 logical blocks shall be transferred. Any
   *          other value indicates the number of logical blocks that shall be
   *          transferred.
   * @return A <code>ByteBuffer</code> object with the above data.
   */
  private static final ByteBuffer createReadWriteMessage(final byte opCode,
      final int logicalBlockAddress, final short transferLength) {

    ByteBuffer cdb = ByteBuffer.allocate(DEFAULT_CDB_LENGTH);
    // operation code
    cdb.put(opCode);

    // logical block address
    cdb.position(LOGICAL_BLOCK_ADDRESS_OFFSET);
    cdb.putInt(logicalBlockAddress);

    // set transfer length
    cdb.position(TRANSFER_LENGTH_OFFSET);
    cdb.putShort(transferLength);
    cdb.rewind();

    return cdb;

  }

  /**
   * Creates the Command Descriptor Block for a Read Capacity Message.
   * 
   * @return A <code>ByteBuffer</code> object with the above data.
   */
  public static final ByteBuffer createReadCapacityMessage() {

    ByteBuffer cdb = ByteBuffer.allocate(DEFAULT_CDB_LENGTH);
    // operation code
    cdb.put(READ_CAPACITY_OP_CODE);

    cdb.rewind();

    return cdb;
  }

}

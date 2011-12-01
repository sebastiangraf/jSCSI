package org.jscsi.target.scsi.modeSense;

import java.nio.ByteBuffer;

import org.jscsi.target.util.BitManip;
import org.jscsi.target.util.ReadWrite;

/**
 * A {@link ModeParameterHeader} sub-class. Instances of this class are
 * sent in response to <code>MODE SENSE (10)</code> SCSI commands and
 * have a serialized length of 8 bytes.
 * @see LongLogicalBlockDescriptor
 * @author Andreas Ergenzinger
 */
public final class ModeParameterHeader10 extends ModeParameterHeader {
	
	/**
	 * The length of this object when serialized.
	 */
	static final int SIZE = 8;
	
	/**
	 * The length in bytes of the MODE DATA LENGTH field.
	 */
	static final int MODE_DATA_LENGTH_FIELD_SIZE = 2;
	
	/**
	 * If the Long LBA (LONGLBA) bit is set to zero, the MODE PARAMETER
	 * BLOCK DESCRIPTOR(s), if any, are each eight bytes long and have
	 * the format described in SPC-3. If the LONGLBA bit is set to one,
	 * the mode parameter block descriptor(s), if any, are each sixteen
	 * bytes long and have a format described in a non-general command
	 * standard.
	 */
	private final boolean longLba;
	
	
	/**
	 * The constructor.
	 * @param modeDataLength the total length in bytes of all
	 * MODE DATA list elements
	 * @param blockDescriptorLength the total length in bytes of all
	 * BLOCK DESCRIPTOR list elements
	 * @param longLba if <code>true</code> then the LONG LBA MODE
	 * PAREMETER LOGICAL BLOCK DESCRIPTOR format will be used
	 * @see LongLogicalBlockDescriptor
	 * @see ShortLogicalBlockDescriptor
	 */
	public ModeParameterHeader10(final int modeDataLength,
			final int blockDescriptorLength, final boolean longLba) {
		super(modeDataLength, blockDescriptorLength);
		this.longLba = longLba;
	}

	public void serialize(final ByteBuffer byteBuffer, final int index) {
		ReadWrite.writeTwoByteInt(byteBuffer,//buffer
				modeDataLength,//value
				index);//index
		byteBuffer.position(index + 2);
		byteBuffer.put(mediumType);
		byteBuffer.put(deviceSpecificParameter);
		final byte zeroByte = 0;
		byteBuffer.put(BitManip.getByteWithBitSet(zeroByte, 0, longLba));
		byteBuffer.put(zeroByte);
		ReadWrite.writeTwoByteInt(byteBuffer,//buffer
				blockDescriptorLength,//value
				index + 6);//index
	}

	public int size() {
		return SIZE;
	}

}

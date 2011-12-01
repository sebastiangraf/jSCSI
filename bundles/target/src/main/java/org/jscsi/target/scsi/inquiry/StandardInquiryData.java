package org.jscsi.target.scsi.inquiry;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.IResponseData;
import org.jscsi.target.scsi.cdb.ScsiOperationCode;

/**
 * The standard inquiry data, sent as a response to an
 * {@link ScsiOperationCode#INQUIRY} command.
 * <p>
 * This class uses the singleton pattern since the returned
 * standard inquiry data will always be the same.
 * <p>
 * Not all fields in the serialized form of the singleton have
 * a corresponding member variable, only those fields containing
 * ASCII information.
 * @author Andreas Ergenzinger
 */
public final class StandardInquiryData implements IResponseData { 
	
	/**
	 * The total length of the serialized Standard Inquiry Data.
	 */
	private static final int SIZE = 36;
	
	private static final String VENDOR_ID = "disyUKon";
	private static final int VENDOR_ID_FIELD_POSITION = 8;
	private static final int VENDOR_ID_FIELD_LENGTH = 8;
	
	private static final String PRODUCT_ID = "jSCSI Target";
	private static final int PRODUCT_ID_FIELD_POSITION = 16;
	private static final int PRODUCT_ID_FIELD_LENGTH = 16;
	
	private static final String PRODUCT_REVISION_LEVEL = "1.00";
	private static final int PRODUCT_REVISION_LEVEL_FIELD_POSITION = 32;
	private static final int PRODUCT_REVISION_LEVEL_FIELD_LENGTH = 4;
	
	/**
	 * The singleton.
	 */
	private static StandardInquiryData instance;
	
	private StandardInquiryData() {
		//singleton pattern
	}
	
	/**
	 * Returns the one and only {@link StandardInquiryData} object.
	 * @return the one and only {@link StandardInquiryData} object
	 */
	public static StandardInquiryData getInstance() {
		if (instance == null)
			instance = new StandardInquiryData();
		return instance;
	}
	
	public void serialize(ByteBuffer byteBuffer, int index) {
		
		//*** byte 0 ****
		/*
		 * Peripheral qualifier (3 most significant bits of byte 0):
		 * 
		 * 000b
		 * 
		 * A peripheral device having the specified peripheral device type is connected
		 * to this logical unit. If the device server is unable to determine whether or
		 * not a peripheral device is connected, it also shall use this peripheral qualifier.
		 * This peripheral qualifier does not mean that the peripheral device connected to
		 * the logical unit is ready for access.
		 * 
		 * Peripheral Device Type (5 least significant bits of byte 0):
		 * 
		 * 00000b
		 * 
		 * direct access block device
		 * 
		 * Therefore no need to change anything.
		 */
		byteBuffer.position(index);
		byteBuffer.put((byte)0);

		//*** byte 1 ***
		/*
		 * RMB bit (bit 7):
		 * 
		 * 0
		 * 
		 * A removable medium (RMB) bit set to zero indicates that the medium is not removable.
		 * 
		 * (remaining bits are RESERVED)
		 */
		byteBuffer.put((byte)0);
		
		//*** byte 2 ***
		/*
		 * version:
		 * 
		 * 0x05
		 * 
		 * The device complies to the SPC3 and respective ANSI standard
		 */
		byteBuffer.put((byte)0x05);
		
		//*** byte 3 - flags ***
		/*
		 * (bits 7 and 6 are obsolete)
		 * 
		 * NORMACA (bit 5):
		 * 
		 * 0
		 * 
		 * A NORMACA (normal auto contingency allegiance) bit set to zero indicates that the
		 * device server does not support a NACA bit set to one and does not support the ACA
		 * task attribute.
		 * 
		 * HISUP (bit 4):
		 * 
		 * 0
		 * 
		 * A hierarchical support (HISUP) bit set to zero indicates the SCSI target device
		 * does not use the hierarchical addressing model to assign LUNs to logical units.
		 * 
		 * RESPONSE DATA FORMAT (bits 3-0):
		 * 
		 * 0010b
		 *  
		 * RESPONSE DATA FORMAT: SPC3
		 */
		byteBuffer.put((byte)2);//0000 0010b
		
		//*** byte 4 ***
		/* 
		 * n - 4 = 35 - 4 = 31
		 * 
		 * The ADDITIONAL LENGTH field indicates the length in bytes of the remaining standard
		 * INQUIRY data.
		 */
		byteBuffer.put((byte)31);
		
		//*** byte 5 ***
		/*
		 * SSCS (bit 7):
		 * 
		 * 0
		 * 
		 * An SCC Supported bit set to zero indicates that the SCSI target device does not
		 * contain an embedded storage array controller component.
		 * 
		 * ACC (bit 6):
		 * 
		 * 0
		 * 
		 * An ACC bit set to zero indicates that no access controls coordinator may be addressed
		 * through this logical unit.
		 * 
		 * TGPS field (bits 5 and 4):
		 * 
		 * 00b
		 * 
		 * The contents of the target port group support (TPGS) indicate the support for
		 * asymmetric logical unit access.
		 * The SCSI target device does not support asymmetric logical unit access or supports a
		 * form of asymmetric access that is vendor specific. Neither the REPORT TARGET GROUPS
		 * nor the SET TARGET GROUPS commands is supported.
		 * 
		 * 3PC (bit 3):
		 * 
		 * 0
		 * 
		 * A Third-Party Copy (3PC) bit set to zero indicates that the SCSI target device does not
		 * support third-party copy commands such as the EXTENDED COPY command.
		 * 
		 * (bits 2 and 1 are RESERVED)
		 * 
		 * PROTECT (bit 0):
		 * 
		 * 0
		 * 
		 * A PROTECT bit set to zero indicates that the logical unit does not support protection
		 * information.
		 */
		byteBuffer.put((byte)0);
		
		//*** byte 6 ***
		/* 
		 * BQUE (bit 7):
		 * 
		 * The CMDQUE bit and BQUE bit indicate whether the logical unit supports the full task
		 * management model.
		 * 
		 * 1 (and 0)
		 * 
		 * Basic task management model supported.
		 * 
		 * ENCSERV (bit 6):
		 * 
		 * 0
		 * 
		 * An ENCSERV bit set to zero indicates that the SCSI target device does not
		 * contain an embedded enclosure services component.
		 * 
		 * (bit 5 is VENDOR SPECIFIC i.e. 0)
		 * 
		 * MULTIP (bit 4):
		 * 
		 * 0
		 * 
		 * A Multi Port (MULTIP) bit set to zero indicates that this is not a multi-
		 * port (two or more ports) SCSI target device and therefore does not conform
		 * to the SCSI multi-port device requirements found in the applicable
		 * standards (e.g., SAM-3, a SCSI transport protocol standard and possibly
		 * provisions of a command standard).
		 * 
		 * MCHNGR (bit 3):
		 * 
		 * 0
		 * 
		 * The MCHNGR bit is valid only when the RMB bit is equal to one. A MCHNGR bit set to
		 * zero indicates that the SCSI target device does not support commands to control an
		 * attached media changer.
		 * 
		 * (bits 2 and 1 are RESERVED)
		 * 
		 * ADDR16 (bit 0):
		 * 
		 * 0
		 * 
		 * RESERVED since the SCSI Parallel Interface transport protocol is not used.
		 */
		byteBuffer.put((byte)128);//1000 0000b
		
		//*** byte 7 ***
		/* 
		 * (bits 7, 6, and 2 are OBSOLETE, bit 0 is VENDOR SPECIFIC)
		 * 
		 * WBUS and SYNC (bits 5 and 4):
		 * 
		 * 00b
		 * 
		 * RESERVED since the SCSI Parallel Interface transport protocol is not used.
		 * 
		 * 
		 * LINKED (bit 3):
		 * 
		 * 0
		 * 
		 * A LINKED bit set to zero indicates the device server does not support linked commands.
		 * 
		 * CMDQUE (bit 1):
		 * 
		 * 0
		 * 
		 * The CMDQUE bit and BQUE bit indicate whether the logical unit supports the full task
		 * management model.
		 * 
		 * (see BQUE ,byte 6)
		 */
		byteBuffer.put((byte)0);
		
		//*** bytes 8 to 15 ***
		/*
		 * T10 VENDOR IDENTIFICATION:
		 * 
		 * The T10 VENDOR IDENTIFICATION field contains eight bytes of left-aligned ASCII data
		 * identifying the vendor of the product.
		 * 
		 * The T10 vendor identification shall be one assigned by INCITS, but obviously that is
		 * not the case here.
		 * disyUKon
		 */
		putString(byteBuffer,
				VENDOR_ID,
				index + VENDOR_ID_FIELD_POSITION,
				VENDOR_ID_FIELD_LENGTH);
		
		//*** bytes 16 to 31
		/*
		 * PRODUCT IDENTIFICATION:
		 * 
		 * The PRODUCT IDENTIFICATION field contains sixteen bytes of left-aligned ASCII data
		 * defined by the vendor.
		 */
		putString(byteBuffer,
				PRODUCT_ID,
				index + PRODUCT_ID_FIELD_POSITION,
				PRODUCT_ID_FIELD_LENGTH);
		
		//*** bytes 32 to 35 ***
		/*
		 * PRODUCT REVISION LEVEL:
		 * 
		 * The PRODUCT REVISION LEVEL field contains four bytes of left-aligned ASCII data
		 * defined by the vendor.
		 */
		putString(byteBuffer,
				PRODUCT_REVISION_LEVEL,
				index + PRODUCT_REVISION_LEVEL_FIELD_POSITION,
				PRODUCT_REVISION_LEVEL_FIELD_LENGTH);
	}
	
	/**
	 * Puts up to <i>fieldLength</i> of the passed {@link String} into
	 * a {@link ByteBuffer} and fills the remaining bytes with zeros.
	 * @param byteBuffer where the {@link String}'s characters will be
	 * copied
	 * @param string contains the characters to copy
	 * @param position where the first character of the {@link String}
	 * will be put
	 * @param fieldLength the number of bytes in the {@link ByteBuffer}
	 * that will be overwritten
	 */
	private void putString(final ByteBuffer byteBuffer, final String string,
			final int position, final int fieldLength) {
		//set position
		byteBuffer.position(position);
		//put string characters
		final int stringLength = Math.min(string.length(), fieldLength);
		for (int i = 0; i < stringLength; ++i)
			byteBuffer.put((byte)string.charAt(i));
		//clear remaining remaining bytes
		for (int i = stringLength; i < fieldLength; ++i)
			byteBuffer.put((byte)0);
	}
	
	public int size() {
		return SIZE;
	}
}

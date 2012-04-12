package org.jscsi.target.scsi.inquiry;

/**
 * The IDENTIFIER TYPE field indicates the format and assignment authority for
 * the IDENTIFIER.
 * <p>
 * The IDENTIFIER TYPE field has a length of four bits.
 * 
 * @see Identifier
 * @author Andreas Ergenzinger
 */
public enum IdentifierType {

    VENDOR_SPECIFIC((byte)0x0), T10_VENDOR_ID_BASED((byte)0x1), EUI_64_BASED((byte)0x2), NAA((byte)0x3),
        RELATIVE_TARGET_PORT_IDENTIFIERT((byte)0x4), TARGET_PORT_GROUP((byte)0x5), LOGICAL_UNIT_GROUP(
            (byte)0x6), MD5_LOGICAL_UNIT_IDENTIFIER((byte)0x7), SCSI_NAME_STRING((byte)0x8);
    // 0x9-0xf are RESERVED

    private final byte value;

    private IdentifierType(byte value) {
        this.value = value;
    }

    public final byte getValue() {
        return value;
    }
}

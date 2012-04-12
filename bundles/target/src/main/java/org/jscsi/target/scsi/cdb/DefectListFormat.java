package org.jscsi.target.scsi.cdb;

/**
 * If the {@link FormatUnitCDB#formatData} bit is set to one, then the DEFECT
 * LIST FORMAT field specifies the format of the address descriptors in the
 * defect list.
 * <p>
 * The DEFECT LIST FORMAT field has a length of 3 bits.
 * 
 * @author Andreas Ergenzinger
 */
public enum DefectListFormat {
    SHORT_BLOCK((byte)0), LONG_BLOCK((byte)3), BYTES_FROM_INDEX((byte)4), PHYSICAL_SECTOR((byte)5),
        VENDOR_SPECIFIC((byte)6);
    // all other values are RESERVED

    private final byte value;

    private DefectListFormat(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public static DefectListFormat valueOf(int value) {
        DefectListFormat[] values = values();
        for (int i = 0; i < values.length; ++i)
            if (values[i].getValue() == value)
                return values[i];
        return null;
    }
}

package org.jscsi.target.scsi.cdb;

/**
 * The {@link CdbType} determines the length of a serialized {@link CommandDescriptorBlock} (CDB) object.
 * <p>
 * The first 3 bytes (the group code) of the CDB's {@link ScsiOperationCode} field specify the {@link CdbType}.
 * 
 * @author Andreas Ergenzinger
 */
public enum CdbType {
    /**
     * The CDB is 6 bytes long.
     */
    SIX_BYTE_COMMANDS,
    /**
     * The CDB is 10 bytes long.
     */
    TEN_BYTE_COMMANDS,
    /**
     * The CDB is 12 bytes long.
     */
    TWELVE_BYTE_COMMANDS,
    /**
     * The CDB is 16 bytes long.
     */
    SIXTEEN_BYTE_COMMANDS,
    /**
     * The CDB length is not determined by the CDB's {@link ScsiOperationCode} field.
     */
    VARIABLE_LENGTH_COMMANDS,
    /**
     * The CDB length is vendor-specific.
     */
    VENDOR_SPECIFIC;

    /**
     * Returns the {@link CdbType} for the passed {@link ScsiOperationCode}.
     * 
     * @param scsiOpCode
     *            determines CDB length
     * @return the appropriate {@link CdbType}
     */
    public static CdbType getCdbType(ScsiOperationCode scsiOpCode) {
        final int groupCode = scsiOpCode.getGroupCode();

        switch (groupCode) {
        case 0:// 000b
            return SIX_BYTE_COMMANDS;
        case 1:// 001b
        case 2:// 010b
            return TEN_BYTE_COMMANDS;
        case 3:// 011b
            return VARIABLE_LENGTH_COMMANDS;
        case 4:// 100b
            return SIXTEEN_BYTE_COMMANDS;
        case 5:// 101b
            return TWELVE_BYTE_COMMANDS;
        case 6:// 110b
        case 7:// 111b
            return VENDOR_SPECIFIC;
        default:
            return null;// this really should not happen!
        }
    }

}

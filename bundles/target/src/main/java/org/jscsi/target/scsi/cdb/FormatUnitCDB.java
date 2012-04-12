package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.target.util.BitManip;

/**
 * This class represents Command Descriptor Blocks for the <code>FORMAT UNIT</code> SCSI command.
 * 
 * @author Andreas Ergenzinger
 */
public final class FormatUnitCDB extends CommandDescriptorBlock {

    /**
     * The format protection information (FMTPINFO) field in combination with
     * the PROTECTION FIELD USAGE field
     */
    private byte formatProtectionInformation;

    /**
     * A LONGLIST bit set to zero specifies that the parameter list, if any,
     * contains a short parameter list header as defined in SBC-3. A LONGLIST
     * bit set to one specifies that the parameter list, if any, contains a long
     * parameter list header.
     * <p>
     * If the FMTDATA bit is set to zero, then the LONGLIST bit shall be ignored.
     */
    private boolean longList;

    /**
     * A format data (FMTDATA) bit set to zero specifies that no parameter list
     * be transferred from the data-out buffer (i.e. the PDU's data segment is
     * empty).
     */
    private boolean formatData;

    /**
     * A complete list (CMPLST) bit set to zero specifies that the defect list
     * included in the FORMAT UNIT parameter list shall be used in an addition
     * to the existing list of defects.
     */
    private boolean completeList;

    /**
     * If the FMTDATA bit is set to one, then the DEFECT LIST FORMAT field
     * specifies the format of the address descriptors in the defect list.
     */
    private DefectListFormat defectListFormat;

    public FormatUnitCDB(ByteBuffer buffer) {
        super(buffer);

        byte b = buffer.get(1);

        // format protection information
        formatProtectionInformation = (byte)(b >>> 6);

        // long list
        longList = BitManip.getBit(b, 5);

        // format data
        formatData = BitManip.getBit(b, 4);

        // complete list
        completeList = BitManip.getBit(b, 3);

        // defect list format
        defectListFormat = DefectListFormat.valueOf(b & 7);
        if (!formatData) {
            if (defectListFormat != DefectListFormat.SHORT_BLOCK)
                addIllegalFieldPointer(1, 2);// illegal defect list format
            /*
             * If the FMTDATA bit is set to zero and the FMTPINFO field is not
             * set to zero, then the device server shall terminate the command
             * with CHECK CONDITION status with the sense key set to ILLEGAL
             * REQUEST and the additional sense code set to INVALID FIELD IN
             * CDB.
             */
            if (formatProtectionInformation != 0)
                addIllegalFieldPointer(1, 7);
        } else if (formatData)
            addIllegalFieldPointer(1, 4);// no support for format data
    }

    public final boolean getCompleteList() {
        return completeList;
    }

    public final int getFormatProtectionInformation() {
        return formatProtectionInformation;
    }

    public final boolean getLongList() {
        return longList;
    }
}

package org.jscsi.target.scsi.inquiry;

import java.nio.ByteBuffer;

/**
 * The null-terminated, null-padded (see 4.4.2) SCSI NAME STRING field contains
 * a UTF-8 format string. The number of bytes in the SCSI NAME STRING field
 * (i.e., the value in the IDENTIFIER LENGTH field) shall be no larger than 256
 * and shall be a multiple of four.
 * 
 * @author Andreas Ergenzinger
 */
public class ScsiNameStringIdentifier extends Identifier {

    /**
     * The maximum {@link #size()} of this {@link Identifier}.
     */
    private static final int MAX_SIZE = 256;

    /**
     * The identifying string.
     */
    private final String nameString;

    /**
     * The logical unit name extension is a UTF-8 string containing no more than
     * 16 hexadecimal digits. The logical unit name extension is assigned by the
     * SCSI target device vendor and shall be assigned so the logical unit name
     * is worldwide unique.
     * <p>
     * This Logical Unit Name Extension has been randomly generated.<br>
     * (0x493f51ba986f9800 = 5278027150164727808)
     */
    private static final String logicalUnitNameExtension = "493f51ba986f9800";

    public ScsiNameStringIdentifier(String targetName) {

        /*
         * The SCSI NAME STRING field starts with either:<br> ...<br> c) The
         * four UTF-8 characters "iqn." concatenated with an iSCSI Name for an
         * iSCSI-name based identifier (see iSCSI).
         * 
         * If the ASSOCIATION field is set to 00b (i.e., logical unit) and the
         * SCSI NAME STRING field starts with the four UTF-8 characters "iqn.",
         * the SCSI NAME STRING field ends with the five UTF-8 characters
         * ",L,0x" concatenated with 16 hexadecimal digits for the logical unit
         * name extension.
         */
        nameString = targetName + ",L,0x" + logicalUnitNameExtension;
    }

    public void serialize(ByteBuffer byteBuffer, int index) {
        byteBuffer.position(index);
        final int size = size();// this many bytes will be written
        int stringLength = Math.min(nameString.length(), MAX_SIZE);
        if (stringLength == MAX_SIZE)
            --stringLength;// at least one null character as padding
        // copy string characters
        for (int i = 0; i < stringLength; ++i)
            byteBuffer.put((byte)nameString.charAt(i));
        // add padding
        for (int i = 0; i < size - stringLength; ++i)
            byteBuffer.put((byte)0);
    }

    public int size() {
        return Math.min(nameString.length() + getNullTerminatedPaddingLength(), MAX_SIZE);
    }

    /**
     * Returns the number of null-character padding bytes that have to be
     * appended to the {@link #nameString} when used in serialized form.
     * 
     * @return the required number of null-character padding bytes
     */
    private int getNullTerminatedPaddingLength() {
        return 4 - (nameString.length() % 4);
    }
}

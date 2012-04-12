package org.jscsi.target.scsi.lun;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.IResponseData;
import org.jscsi.target.util.ReadWrite;

/**
 * Instances of this class are sent to the initiator in response to <code>REPORT LUNS</code> SCSI commands.
 * They contain a zero or more logical
 * unit numbers (LUNs) that identify some or all of the target's logical units,
 * depending on the <code>REPORT
 * LUNS</code> parameters.
 * 
 * @author Andreas Ergenzinger
 */
public final class ReportLunsParameterData implements IResponseData {

    /**
     * The length of the mandatory header fields in bytes.
     */
    private static final int HEADER_LENGTH = 8;

    /**
     * The length in bytes of the list of logical unit numbers.
     */
    private int lunListLength = 0;

    /**
     * LUNs identifying all reported logical units.
     */
    private LogicalUnitNumber[] luns;

    /**
     * The constructor.
     * 
     * @param luns
     *            the LUNs to report
     */
    public ReportLunsParameterData(LogicalUnitNumber... luns) {
        this.luns = luns;
        if (luns != null)
            lunListLength = LogicalUnitNumber.SIZE * luns.length;
    }

    public void serialize(ByteBuffer byteBuffer, int index) {

        // LUN list length
        ReadWrite.writeInt(lunListLength, byteBuffer, index);

        // LUN list
        int lunIndex = index + HEADER_LENGTH;
        for (int i = 0; i < luns.length; ++i) {
            luns[i].serialize(byteBuffer, lunIndex);
            lunIndex += luns[i].size();
        }
    }

    public int size() {
        return HEADER_LENGTH + lunListLength;
    }
}

package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.target.util.ReadWrite;

/**
 * This class represents Command Descriptor Blocks for the <code>REPORT LUNS</code> SCSI command.
 * 
 * @author Andreas Ergenzinger
 */
public class ReportLunsCDB extends CommandDescriptorBlock {

    /**
     * The SELECT REPORT field specifies the types of logical unit addresses
     * that shall be reported.
     * 
     * @see SelectReport
     */
    private final SelectReport selectReport;

    /**
     * The ALLOCATION LENGTH field specifies the maximum number of bytes that an
     * application client has allocated in the Data-In Buffer. An allocation
     * length of zero specifies that no data shall be transferred. This
     * condition shall not be considered as an error. The device server shall
     * terminate transfers to the Data-In Buffer when the number of bytes
     * specified by the ALLOCATION LENGTH field have been transferred or when
     * all available data have been transferred, whichever is less.
     * <p>
     * The allocation length is used to limit the maximum amount of variable length data (e.g., mode data, log
     * data, diagnostic data) returned to an application client. If the information being transferred to the
     * Data-In Buffer includes fields containing counts of the number of bytes in some or all of the data,
     * then the contents of these fields shall not be altered to reflect the truncation, if any, that results
     * from an insufficient ALLOCATION LENGTH value, unless the standard that describes the Data-In Buffer
     * format states otherwise.
     * <p>
     * If the amount of information to be transferred exceeds the maximum value that the ALLOCATION LENGTH
     * field is capable of specifying, the device server shall transfer no data and terminate the command with
     * CHECK CONDITION status, with the sense key set to ILLEGAL REQUEST, and the additional sense code set to
     * INVALID FIELD IN CDB.
     * <p>
     * If EVPD is set to zero, the allocation length should be at least five, so that the ADDITIONAL LENGTH
     * field in the parameter data is returned. If EVPD is set to one, the allocation length should be should
     * be at least four, so that the PAGE LENGTH field in the parameter data is returned.
     */
    private final int allocationLength;

    public ReportLunsCDB(ByteBuffer buffer) {
        super(buffer);

        // select report
        selectReport = SelectReport.getValue(buffer.get(2));
        if (selectReport == null)
            addIllegalFieldPointer(2);// reserved select report value

        // allocation length
        allocationLength = ReadWrite.readFourByteInt(buffer, 6);
        if (allocationLength < 16)
            addIllegalFieldPointer(6);// The allocation length should be at
                                      // least 16.
    }

    public final int getAllocationLength() {
        return allocationLength;
    }

    public final SelectReport getSelectReport() {
        return selectReport;
    }
}

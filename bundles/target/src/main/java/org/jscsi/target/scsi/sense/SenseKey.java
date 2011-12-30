package org.jscsi.target.scsi.sense;

/**
 * The SENSE KEY field indicates generic information describing an error or
 * exception condition. The field's length is 4 bits.
 * 
 * @author Andreas Ergenzinger
 */
public enum SenseKey {
    /**
     * Indicates that there is no specific sense key information to be reported.
     * This may occur for a successful command or for a command that receives
     * CHECK CONDITION status because one of the FILEMARK, EOM, or ILI bits is
     * set to one.
     */
    NO_SENSE(0x00),
    /**
     * Indicates that the command completed successfully, with some recovery
     * action performed by the device server. Details may be determined by
     * examining the additional sense bytes and the INFORMATION field. When
     * multiple recovered errors occur during one command, the choice of which
     * error to report (e.g., first, last, most severe) is vendor specific.
     */
    RECOVERED_ERROR(0x1),
    /**
     * Indicates that the logical unit is not accessible. Operator intervention
     * may be required to correct this condition.
     */
    NOT_READY(0x2),
    /**
     * Indicates that the command terminated with a non-recovered error
     * condition that may have been caused by a flaw in the medium or an error
     * in the recorded data. This sense key may also be returned if the device
     * server is unable to distinguish between a flaw in the medium and a
     * specific hardware failure (i.e., sense key 4h {@link #HARDWARE_ERROR}).
     */
    MEDIUM_ERROR(0x3),
    /**
     * Indicates that the device server detected a non-recoverable hardware
     * failure (e.g., controller failure, device failure, or parity error) while
     * performing the command or during a self test.
     */
    HARDWARE_ERROR(0x4),
    /**
     * Indicates that:
     * 
     * a) The command was addressed to an incorrect logical unit number (see
     * SAM-3); b) The command had an invalid task attribute (see SAM-3); c) The
     * command was addressed to a logical unit whose current configuration
     * prohibits processing the command; d) There was an illegal parameter in
     * the CDB; or e) There was an illegal parameter in the additional
     * parameters supplied as data for some commands (e.g., PERSISTENT RESERVE
     * OUT).
     * 
     * If the device server detects an invalid parameter in the CDB, it shall
     * terminate the command without altering the medium. If the device server
     * detects an invalid parameter in the additional parameters supplied as
     * data, the device server may have already altered the medium.
     */
    ILLEGAL_REQUEST(0x5),
    /**
     * Indicates that a unit attention condition has been established (e.g., the
     * removable medium may have been changed, a logical unit reset occurred).
     * See SAM-3.
     */
    UNIT_ATTENTION(0x6),
    /**
     * Indicates that a command that reads or writes the medium was attempted on
     * a block that is protected. The read or write operation is not performed.
     */
    DATA_PROTECT(0x7),
    /**
     * Indicates that a write-once device or a sequential-access device
     * encountered blank medium or format-defined end-of-data indication while
     * reading or that a write-once device encountered a non-blank medium while
     * writing.
     */
    BLANK_CHECK(0x8),
    /**
     * This sense key is available for reporting vendor specific conditions.
     */
    VENDOR_SPECIFIC(0x9),
    /**
     * Indicates an EXTENDED COPY command was aborted due to an error condition
     * on the source device, the destination device, or both (see "errors
     * detected during processing of segment descriptors").
     */
    COPY_ABORTED(0xa),
    /**
     * Indicates that the device server aborted the command. The application
     * client may be able to recover by trying the command again.
     */
    ABORTED_COMMAND(0xb),
    /*
     * 0x0c is obsolete.
     */
    /**
     * Indicates that a buffered SCSI device has reached the end-of-partition
     * and data may remain in the buffer that has not been written to the
     * medium. One or more RECOVER BUFFERED DATA command(s) may be issued to
     * read the unwritten data from the buffer. (See SSC-2.)
     */
    VOLUME_OVERFLOW(0xd),
    /**
     * Indicates that the source data did not match the data read from the
     * medium.
     */
    MISCOMPARE(0xe);
    /*
     * 0xf is reserved.
     */

    /**
     * Look-up array that maps sense key values to instances of this
     * enumeration.
     */
    private static SenseKey[] mapping = new SenseKey[16];

    static {// initialize mapping
        final SenseKey[] keys = values();
        for (int i = 0; i < keys.length; ++i)
            mapping[keys[i].value] = keys[i];
        // some will remain initialized to null
    }

    /**
     * Returns the {@link SenseKey} instance representing the passed
     * <i>value</i>.
     * 
     * @param value
     *            a sense key value
     * @return the {@link SenseKey} instance representing the passed
     *         <i>value</i>
     */
    public static SenseKey valueOf(final int value) {
        final int index = 15 & value;// keep only the last four bits
        if (0 < index || index >= mapping.length)
            return null;
        return mapping[index];
    }

    /**
     * The serialized value of the {@link SenseKey} instance.
     */
    private final int value;

    /**
     * The constructor.
     * 
     * @param value
     *            the serialized value of the object.
     */
    private SenseKey(int value) {
        this.value = value;
    }

    /**
     * The serialized value of the instance.
     * 
     * @return serialized value of the instance
     */
    public int getValue() {
        return value;
    }
}

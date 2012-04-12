package org.jscsi.target.scsi.modeSense;

import org.jscsi.target.scsi.ISerializable;

/**
 * Instances {@link ModeParameterHeader} are part of {@link ModeParameterList} objects and specify the layout
 * and length of the following non-header fields.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class ModeParameterHeader implements ISerializable {

    /**
     * When using the MODE SENSE command, the MODE DATA LENGTH field indicates
     * the length in bytes of the following data that is available to be
     * transferred. The mode data length does not include the number of bytes in
     * the MODE DATA LENGTH field.
     * <p>
     * When using the MODE SELECT command, this field is reserved.
     * <p>
     * Logical units that support more than 256 bytes of block descriptors and mode pages may need to
     * implement ten-byte mode commands. The mode data length field in the six-byte CDB header limits the
     * returned data to 256 bytes.
     */
    protected final int modeDataLength;

    /**
     * The contents of the MEDIUM TYPE field are unique for each device type.
     * For direct-access block devices, the value must be 0x00.
     */
    protected final byte mediumType = (byte)0x00;

    /**
     * The DEVICE-SPECIFIC PARAMETER field is unique for each device type. For
     * direct-access block devices the byte looks like this:
     * <p>
     * <code>
     * +---+---+---+---+---+---+---+---+---+<br/>
     * |bit| 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 |<br/>
     * +---+---+---+---+---+---+---+---+---+<br/>
     * |___|WP_|RESERV-|DPO|___RESERVED____|<br/>
     * |___|___|__ED___|FUA|_______________|<br/>
     * +---+---+---+---+---+---+---+---+---+
     * </code>
     * <p>
     * When used with the MODE SELECT command, the write protect (WP) bit is not defined. When used with the
     * MODE SENSE command, a WP bit set to one indicates that the medium is write-protected. A WP bit set to
     * zero indicates that the medium is not write-protected. When the software write protect (SWP) bit in the
     * Control mode page (see SPC-4) is set to one, the WP bit shall be set to one.
     * <p>
     * When the SWP bit in the Control mode page is set to zero, the WP bit shall be set to one if the medium
     * is write-protected (e.g., due to mechanisms outside the scope of this standard) or zero if the medium
     * is not write-protected.
     * <p>
     * When used with the MODE SELECT command, the DPOFUA bit is reserved. When used with the MODE SENSE
     * command, a DPOFUA bit set to zero indicates that the device server does not support the DPO and FUA
     * bits. When used with the MODE SENSE command, a DPOFUA bit set to one indicates that the device server
     * supports the DPO and FUA bits.
     * <p>
     * So, the simulated logical unit of the jSCSI Target must always use a value of 0x00.
     */
    protected final byte deviceSpecificParameter = (byte)0x00;

    /**
     * The BLOCK DESCRIPTOR LENGTH field contains the length in bytes of all the
     * block descriptors. It is equal to the number of block descriptors times
     * eight if the LONGLBA bit is set to zero or times sixteen if the LONGLBA
     * bit is set to one, and does not include mode pages or vendor specific
     * parameters (e.g., page code set to zero), if any, that may follow the
     * last block descriptor. A block descriptor length of zero indicates that
     * no block descriptors are included in the mode parameter list. This
     * condition shall not be considered an error.
     */
    protected final int blockDescriptorLength;

    /**
     * The abstract constructor.
     * 
     * @param modeDataLength
     *            the length in bytes of all MODE DATA list elements
     * @param blockDescriptorLength
     *            the length in bytes of all BLOCK DESCRIPTOR list elements
     */
    public ModeParameterHeader(final int modeDataLength, final int blockDescriptorLength) {
        this.modeDataLength = modeDataLength;
        this.blockDescriptorLength = blockDescriptorLength;
    }
}

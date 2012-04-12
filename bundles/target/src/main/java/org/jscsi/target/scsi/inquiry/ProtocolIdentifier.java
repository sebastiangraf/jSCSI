package org.jscsi.target.scsi.inquiry;

/**
 * The PROTOCOL IDENTIFIER field is part of IDENTIFICATION DESCRIPTORS and may
 * indicate the SCSI transport protocol to which the identification descriptor
 * applies. If the ASSOCIATION field contains a value other than 01b (i.e.,
 * target port) or 10b (i.e., SCSI target device) or the PIV bit is set to zero,
 * then the PROTOCOL IDENTIFIER field contents are reserved. If the ASSOCIATION
 * field contains a value of 01b or 10b and the PIV bit is set to one, then the
 * PROTOCOL IDENTIFIER field shall contain one of the values defined in the
 * following table to indicate the SCSI transport protocol to which the
 * identification descriptor applies.
 * 
 * <table border="1">
 * <tr>
 * <th>Protocol<br>
 * Identifier</th>
 * <th>Description</th>
 * <th>Protocol<br>
 * Standard</th>
 * </tr>
 * <tr>
 * <td>0x0</td>
 * <td>Fibre Channel</td>
 * <td>FCP-2</td>
 * </tr>
 * <tr>
 * <td>0x1</td>
 * <td>Parallel SCSI</td>
 * <td>SPI-5</td>
 * </tr>
 * <tr>
 * <td>0x2</td>
 * <td>SSA</td>
 * <td>SSA-S3P</td>
 * </tr>
 * <tr>
 * <td>0x3</td>
 * <td>IEEE 1394</td>
 * <td>SBP-3</td>
 * </tr>
 * <tr>
 * <td>0x4</td>
 * <td>SCSI Remote Direct Memory Access Protocol</td>
 * <td>SRP</td>
 * </tr>
 * <tr>
 * <td>0x5</td>
 * <td>Internet SCSI (iSCSI)</td>
 * <td>iSCSI</td>
 * </tr>
 * <tr>
 * <td>0x6</td>
 * <td>SAS Serial SCSI Protocol</td>
 * <td>SAS</td>
 * </tr>
 * <tr>
 * <td>0x7</td>
 * <td>Automation/Drive Interface Transport Protocol</td>
 * <td>ADT</td>
 * </tr>
 * <tr>
 * <td>0x8</td>
 * <td>AT Attachment Interface (ATA/ATAPI)</td>
 * <td>ATA/ATAPI-7</td>
 * </tr>
 * <tr>
 * <td>0x9-0xe</td>
 * <td>RESERVED</td>
 * <td></td>
 * </tr>
 * <tr>
 * <td>0xf</td>
 * <td>No speficif protocol</td>
 * <td></td>
 * </tr>
 * </table>
 * 
 * The PROTOCOL IDENTIFIER field has a length of four bits.
 * 
 * @see IdentificationDescriptor
 * @author Andreas Ergenzinger
 */
public enum ProtocolIdentifier {
    FIBRE_CHANNEL((byte)0x0), PARALLEL_SCSI((byte)0x1), SSA((byte)0x2), IEEE1394((byte)0x3),
        SCSI_REMOTE_DIRECT_MEMORY_ACCESS_PROTOCOL((byte)0x4), INTERNET_SCSI((byte)0x5),
        SAS_SERIAL_SCSI_PROTOCOL((byte)0x6), AUTOMATION_DRIVE_INTERFACE_TRANSPORT_PROTOCOL((byte)0x7),
        AT_ATTACHEMENT_INTERFACE((byte)0x8),
        // RESERVED not implemented
        NO_SPECIFIC_PROTOCOL((byte)0xf);

    private ProtocolIdentifier(final byte value) {
        this.value = value;
    }

    private final byte value;

    public final byte getValue() {
        return value;
    }
}

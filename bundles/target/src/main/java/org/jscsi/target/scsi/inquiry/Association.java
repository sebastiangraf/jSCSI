package org.jscsi.target.scsi.inquiry;

/**
 * The ASSOCIATION field is part of DEVICE IDENTIFICATION VPD PAGEs and
 * IDENTIFICATION DESCRIPTORs. It indicates the entity with which the IDENTIFIER
 * field is associated, as described in the following table.
 * 
 * <table border="1">
 * <tr>
 * <th>Code</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>00b</td>
 * <td>The IDENTIFIER field is associated with the addressed logical unit.</td>
 * <tr>
 * <tr>
 * <td>01b</td>
 * <td>The IDENTIFIER field is associated with the target port that<br>
 * received the request.</td>
 * <tr>
 * <tr>
 * <td>10b</td>
 * <td>The IDENTIFIER field is associated with the SCSI target device<br>
 * that contains the addressed logical unit.</td>
 * <tr>
 * <tr>
 * <td>11b</td>
 * <td>Reserved</td>
 * <tr>
 * </table>
 * 
 * The ASSOCIATION field has a length of 2 bits.
 * 
 * @see IdentificationDescriptor
 * @see DeviceIdentificationVpdPage
 * @author Andreas Ergenzinger
 */
public enum Association {

    LOGICAL_UNIT((byte)0), TARGET_PORT((byte)1), SCSI_TARGET_DEVICE((byte)2), RESERVED((byte)3);

    /**
     * The serialized value of this object.
     */
    private final byte value;

    private Association(final byte value) {
        this.value = value;
    }

    public final byte getValue() {
        return value;
    }
}

package org.jscsi.target.scsi.inquiry;

/**
 * The CODE SET field indicates the code set used for the IDENTIFIER field.
 * 
 * <table border="1">
 * <tr>
 * <th>Code</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>0x0</td>
 * <td>RESERVED</td>
 * </tr>
 * <tr>
 * <td>0x1</td>
 * <td>The IDENTIFIER field shall contain binary values.</td>
 * </tr>
 * <tr>
 * <td>0x2</td>
 * <td>The IDENTIFIER field shall contain ASCII printable characters<br>
 * (i.e., code values 20h through 7Eh)</td>
 * </tr>
 * <tr>
 * <td>0x3</td>
 * <td>The IDENTIFIER field shall contain ISO/IEC 10646-1 (UTF-8) codes</td>
 * </tr>
 * <tr>
 * <td>0x4 - 0xf</td>
 * <td>RESERVED</td>
 * </tr>
 * </table>
 * 
 * The CODE SET field has a length of four bits.
 * 
 * @see IdentificationDescriptor
 * @author Andreas Ergenzinger
 */
public enum CodeSet {
    /**
     * This value is reserved.
     */
    RESERVED((byte)0x0),
    /**
     * The IDENTIFIER field shall contain binary values.
     */
    BINARY_VALUES((byte)0x1),
    /**
     * The IDENTIFIER field shall contain ASCII printable characters.
     */
    ASCII_PRINTABLE_VALUES((byte)0x2),
    /**
     * The IDENTIFIER field shall contain ISO/IEC 10646-1 (UTF-8) codes:
     */
    UTF8_CODES((byte)0x3);

    private CodeSet(final byte value) {
        this.value = value;
    }

    /**
     * The serialized value of this object.
     */
    private final byte value;

    /**
     * Returns serialized value of this object.
     * 
     * @return serialized value of this object
     */
    public final byte getValue() {
        return value;
    }
}

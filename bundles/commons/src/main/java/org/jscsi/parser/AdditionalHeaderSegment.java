/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jscsi.parser;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.utils.Utils;

/**
 * <h1>AdditionalHeaderSegment</h1>
 * <p>
 * This class encapsulate an Additional Header Segment (AHS) defined in iSCSI
 * Protocol (RFC3720).
 * <p>
 * It provides all methods to serialize and deserialize such an AHS. Further
 * there are getter methods to access the specific data, which is contained in
 * this AHS.
 * 
 * @author Volker Wildi
 */
final class AdditionalHeaderSegment {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This enumeration defines all valid types of additional header segments,
     * which are defined by the iSCSI standard (RFC3720).
     * <p>
     * <table border="1">
     * <tr>
     * <th>Value</th>
     * <th>Meaning</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>Reserved</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>Extended CDB</td>
     * </tr>
     * <tr>
     * <td>2</td>
     * <td>Expected Bidirectional Read Data Length</td>
     * </tr>
     * <tr>
     * <td>3 - 63</td>
     * <td>Reserved</td>
     * </tr>
     * </table>
     */
    enum AdditionalHeaderSegmentType {
        /**
         * This type of AHS MUST NOT be used if the <code>CDBLength</code> is
         * less than <code>17</code>. The length includes the reserved byte
         * <code>3</code>.
         */
        EXTENDED_CDB((byte) 1),

        /**
         * The Expected Bidirectional Read Data Length. But this is not good
         * documented in the iSCSI Protocol (RFC3720).
         */
        EXPECTED_BIDIRECTIONAL_READ_DATA_LENGTH((byte) 2);

        private final byte value;

        private static Map<Byte, AdditionalHeaderSegmentType> mapping;

        static {
            AdditionalHeaderSegmentType.mapping = new HashMap<Byte, AdditionalHeaderSegmentType>();
            for (AdditionalHeaderSegmentType s : values()) {
                AdditionalHeaderSegmentType.mapping.put(s.value, s);
            }
        }

        private AdditionalHeaderSegmentType(final byte newValue) {

            value = newValue;
        }

        /**
         * Returns the value of this enumeration.
         * 
         * @return The value of this enumeration.
         */
        private final byte value() {

            return value;
        }

        /**
         * Returns the constant defined for the given <code>value</code>.
         * 
         * @param value
         *            The value to search for.
         * @return The constant defined for the given <code>value</code>. Or
         *         <code>null</code>, if this value is not defined by this
         *         enumeration.
         */
        private static final AdditionalHeaderSegmentType valueOf(
                final byte value) {

            return AdditionalHeaderSegmentType.mapping.get(value);
        }

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Factor, which must be muliplied with the <code>totalAHSLength</code>
     * contained in a <code>BasicHeaderSegment</code> object.
     */
    static final int AHS_FACTOR = 4;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Offset of the first complete line in the AHS specific field. */
    private static final int EXTENDED_CDB_OFFSET = 1;

    /**
     * The length of AHS, if the type of the AHS is the Bidirectional Expected
     * Read-Data Length.
     */
    private static final int EXPECTED_BIDIRECTIONAL_LENGTH = 0x0005;

    /**
     * Length of the specific field <code>ByteBuffer</code>, which is expected,
     * if the AHS type is the
     * <code>AdditionalHeaderSegmentType.EXPECTED_BIDIRECTIONAL_READ_DATA_LENGTH</code>
     * .
     */
    private static final int EXPECTED_BIDIRECTIONAL_SPECIFIC_FIELD_LENGTH = 5;

    /**
     * This is the size (in bytes) of the <code>AHSLength</code> and the
     * <code>AHSType</code>, which are also included in the serialized AHS form
     * of this object.
     */
    private static final int FIX_SIZE_OVERHEAD = 3;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This field contains the effective length in bytes of the AHS excluding
     * AHSType and AHSLength and padding, if any. The AHS is padded to the
     * smallest integer number of 4 byte words (i.e., from 0 up to 3 padding
     * bytes).
     */
    private short length;

    /**
     * The type of this AHS. <br/>
     * <br/>
     * <table border="1">
     * <tr>
     * <th>Bits</th>
     * <th>Meaning</th>
     * </tr>
     * <tr>
     * <td>0-1</td>
     * <td>Reserved</td>
     * </tr>
     * <tr>
     * <td>2-7</td>
     * <td>AHS code</td>
     * </tr>
     * </table>
     * <br/>
     * 
     * @see AdditionalHeaderSegmentType
     */
    private AdditionalHeaderSegmentType type;

    /**
     * This array contains the informations, which are type specific fields.
     */
    private ByteBuffer specificField;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty AdditionalHeaderSegment object.
     */
    AdditionalHeaderSegment() {

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This method serializes the informations of this AHS object to the byte
     * representation defined by the iSCSI Standard.
     * 
     * @param dst
     *            The destination array to write in.
     * @param offset
     *            The start offset in <code>dst</code>.
     * @return The length of used integers of the serialized form of this AHS
     *         object.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     */
    final int serialize(final ByteBuffer dst, final int offset)
            throws InternetSCSIException {

        dst.position(offset);
        if (dst.remaining() < length) {
            throw new IllegalArgumentException(
                    "Destination array is too small.");
        }

        dst.putShort(length);
        dst.put(type.value());
        dst.put(specificField.get());

        while (specificField.hasRemaining()) {
            dst.putInt(specificField.getInt());
        }

        return length + FIX_SIZE_OVERHEAD;
    }

    /**
     * Extract the informations given by the int array to this Additional Header
     * Segment object.
     * 
     * @param pdu
     *            The Protocol Data Unit to be parsed.
     * @param offset
     *            The offset, where to start in the pdu.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     */
    final void deserialize(final ByteBuffer pdu, final int offset)
            throws InternetSCSIException {

        pdu.position(offset);
        length = pdu.getShort();
        type = AdditionalHeaderSegmentType.valueOf(pdu.get());

        // allocate the needed memory
        specificField = ByteBuffer.allocate(length);
        specificField.put(pdu.get());

        // deserialize the type specific fields
        while (specificField.hasRemaining()) {
            specificField.putInt(pdu.getInt());
        }

        checkIntegrity();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the length of this AHS object. Expected values are greater than
     * <code>0</code> and a maximum of <code>65536</code>
     * 
     * @return The length of this AHS object.
     */
    final short getLength() {

        return length;
    }

    /**
     * Returns an array with the type specific fields of this AHS object.
     * 
     * @return The type specific fields.
     */
    final ByteBuffer getSpecificField() {

        return (ByteBuffer) specificField.rewind();
    }

    /**
     * Returns the type of this AHS object. Expected values are defined as
     * constants in the class AdditionalHeaderSegmentTypes.
     * 
     * @return The value of this AHS object.
     */
    final AdditionalHeaderSegmentType getType() {

        return type;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Creates a string object with all values for easy debugging.
     * 
     * @return The string with all informations of this AHS.
     */
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        sb.append("--------------------------\n");
        sb.append("Additional Header Segment:\n");
        Utils.printField(sb, "Length", length, 1);
        Utils.printField(sb, "Type", type.value(), 1);

        getSpecificField().position(EXTENDED_CDB_OFFSET);

        switch (type) {
        case EXTENDED_CDB:

            while (specificField.hasRemaining()) {
                Utils.printField(sb, "Extended CDB", specificField.getInt(), 1);
            }
            break;

        case EXPECTED_BIDIRECTIONAL_READ_DATA_LENGTH:
            Utils.printField(sb, "Expected Data Length",
                    specificField.getInt(), 1);
            break;

        default:
            // do nothing
        }

        sb.append("--------------------------\n");

        specificField.rewind();

        return sb.toString();
    }

    /**
     * Clears all the stored content of this
     * <code>AdditionalHeaderSegment</code> object.
     */
    final void clear() {

        specificField = null;
        length = 0;
        type = null;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This method checks the integrity of the this Additional Header Segment
     * object to garantee a valid specification.
     * 
     * @throws InternetSCSIException
     *             If the fields are not valid for this AHS type.
     */
    private final void checkIntegrity() throws InternetSCSIException {

        switch (type) {
        case EXTENDED_CDB:
        case EXPECTED_BIDIRECTIONAL_READ_DATA_LENGTH:
            break;

        default:
            throw new InternetSCSIException("AHS Package is not valid.");
        }

        // this field is AHSType independent
        specificField.rewind();
        Utils.isReserved(specificField.get());

        switch (type) {
        case EXTENDED_CDB:
            break;

        case EXPECTED_BIDIRECTIONAL_READ_DATA_LENGTH:
            Utils.isExpected(specificField.limit(),
                    EXPECTED_BIDIRECTIONAL_SPECIFIC_FIELD_LENGTH);
            Utils.isExpected(length, EXPECTED_BIDIRECTIONAL_LENGTH);
            break;

        default:
            throw new InternetSCSIException(
                    "Unknown additional header segment type.");
        }

        specificField.rewind();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

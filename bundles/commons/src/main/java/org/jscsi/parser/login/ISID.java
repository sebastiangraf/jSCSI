/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
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
package org.jscsi.parser.login;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.Constants;
import org.jscsi.utils.Utils;

/**
 * <h1>ISID</h1>
 * <p>
 * This is an initiator-defined component of the session identifier and is structured as follows (see
 * [RFC3721] and Section 9.1.1 Conservative Reuse of ISIDs for details):
 * <p>
 * <table border="1">
 * <tr>
 * <th>Byte</th>
 * <th colspan="8">0</th>
 * <th colspan="8">1</th>
 * <th * colspan="8">2</th>
 * <th colspan="8">3</th>
 * </tr>
 * <tr>
 * <th>Bits</th>
 * <td>0</td>
 * <td>1</td>
 * <td>2</td>
 * <td>3</td>
 * <td>4</td>
 * <td>5</td>
 * <td>6</td>
 * <td>7</td>
 * <td>0</td>
 * <td>1</td>
 * <td>2</td>
 * <td>3</td>
 * <td>4</td>
 * <td>5</td>
 * <td>6</td>
 * <td>7</td>
 * <td>0</td>
 * <td>1</td>
 * <td>2</td>
 * <td>3</td>
 * <td>4</td>
 * <td>5</td>
 * <td>6</td>
 * <td>7</td>
 * <td>0</td>
 * <td>1</td>
 * <td>2</td>
 * <td>3</td>
 * <td>4</td>
 * <td>5</td>
 * <td>6</td>
 * <td>7</td>
 * </tr>
 * <tr>
 * <td>8</td>
 * <td colspan="2"><center>T</center></td>
 * <td colspan="6"><center>A</center></td>
 * <td colspan="16"><center>B</center></td>
 * <td colspan="8"><center>C</center></td>
 * </tr>
 * <tr>
 * <td>12</td>
 * <td colspan="16"><center>D</center></td>
 * <td colspan="16"/>
 * </tr>
 * </table>
 * <p>
 * The T field identifies the format and usage of A, B, C, and D as indicated below:
 * <p>
 * <table border="1">
 * <tr>
 * <th>T</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>00b</td>
 * <td>OUI-Format<br/>
 * A&B are a <code>22</code> bit OUI (the I/G & U/L bits are omitted)<br/>
 * C&D 24 bit qualifier</td>
 * </tr>
 * <tr>
 * <td>01b</td>
 * <td>EN - Format (IANA Enterprise Number)<br/>
 * A - Reserved<br/>
 * B&C EN (IANA Enterprise Number)<br/>
 * D - Qualifier</td>
 * </tr>
 * <tr>
 * <td>10b</td>
 * <td>"Random"<br/>
 * A - Reserved<br/>
 * B&C Random<br/>
 * D - Qualifier</td>
 * </tr>
 * <tr>
 * <td>11b</td>
 * <td>A,B,C&D Reserved</td>
 * </tr>
 * </table>
 * <p>
 * For the <code>T</code> field values <code>00b</code> and <code>01b</code>, a combination of <code>A</code>
 * and <code>B</code> (for <code>00b</code>) or <code>B</code> and <code>C</code> (for <code>01b</code>)
 * identifies the vendor or organization whose component (software or hardware) generates this ISID. A vendor
 * or organization with one or more OUIs, or one or more Enterprise Numbers, MUST use at least one of these
 * numbers and select the appropriate value for the <code>T</code> field when its components generate ISIDs.
 * An <code>OUI</code> or <code>EN</code> MUST be set in the corresponding fields in network byte order (byte
 * big-endian).
 * <p>
 * If the <code>T</code> field is <code>10b</code>, <code>B</code> and <code>C</code> are set to a random
 * <code>24</code>-bit unsigned integer value in network byte order (byte big-endian). See [RFC3721] for how
 * this affects the principle of "conservative reuse".
 * <p>
 * The Qualifier field is a <code>16</code> or <code>24</code>-bit unsigned integer value that provides a
 * range of possible values for the ISID within the selected namespace. It may be set to any value within the
 * constraints specified in the iSCSI protocol (see Section 3.4.3 Consequences of the Model and Section 9.1.1
 * Conservative Reuse of ISIDs).
 * <p>
 * The <code>T</code> field value of <code>11b</code> is reserved.
 * <p>
 * If the ISID is derived from something assigned to a hardware adapter or interface by a vendor, as a preset
 * default value, it MUST be configurable to a value assigned according to the SCSI port behavior desired by
 * the system in which it is installed (see Section 9.1.1 Conservative Reuse of ISIDs and Section 9.1.2 iSCSI
 * Name, ISID, and TPGT Use). The resultant ISID MUST also be persistent over power cycles, reboot, card swap,
 * etc. For details have a look in the [RFC3721].
 * 
 * @author Volker Wildi
 */
public final class ISID {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Enumerations of all valid formats defined in the T field.
     */
    static enum Format {
        /** ISID is in the Organization Unique Identifier Format. */
        OUI_FORMAT((byte)0),
        /** ISID is in the EN-Format (IANA Enterprise Number). */
        IANA_ENTERPRISE_NUMBER((byte)1),
        /** ISID is in the "Random" Format. */
        RANDOM((byte)2),
        /** ISID is in the Reserved. */
        RESERVED((byte)3);

        private final byte value;

        private static Map<Byte, Format> mapping;

        static {
            Format.mapping = new HashMap<Byte, Format>();
            for (Format s : values()) {
                Format.mapping.put(s.value, s);
            }
        }

        private Format(final byte newValue) {

            value = newValue;
        }

        /**
         * Returns the value of this enumeration.
         * 
         * @return The value of this enumeration.
         */
        public final byte value() {

            return value;
        }

        /**
         * Returns the constant defined for the given <code>value</code>.
         * 
         * @param value
         *            The value to search for.
         * @return The constant defined for the given <code>value</code>. Or <code>null</code>, if this value
         *         is not defined by this
         *         enumeration.
         */
        public static final Format valueOf(final byte value) {

            return Format.mapping.get(value);
        }

    }

    /** Bit mask to extract the first int out from a long. */
    private static final long FIRST_LINE_FLAG_MASK = 0xFFFFFFFF00000000L;

    /** Bit flag mask to get the field <code>A</code> in this ISID. */
    private static final int A_FIELD_FLAG_MASK = 0x3F000000;

    /** Number of bits to shift to get the field <code>T</code> in this ISID. */
    private static final int T_FIELD_SHIFT = 30;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The field <code>T</code> defined in the RFC 3720. */
    private Format t;

    /** The field <code>A</code> defined in the RFC 3720. */
    private byte a;

    /** The field <code>B</code> defined in the RFC 3720. */
    private short b;

    /** The field <code>C</code> defined in the RFC 3720. */
    private byte c;

    /** The field <code>D</code> defined in the RFC 3720. */
    private short d;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor, creates a new, empty ISID object.
     */
    public ISID() {

    }

    /**
     * This constructor creates a new ISID object with the given settings.
     * 
     * @param initT
     *            The new T-Value.
     * @param initA
     *            The new A-Value.
     * @param initB
     *            The new B-Value.
     * @param initC
     *            The new C-Value.
     * @param initD
     *            The new D-Value.
     */
    public ISID(final Format initT, final byte initA, final short initB, final byte initC, final short initD) {

        t = initT;
        a = initA;
        b = initB;
        c = initC;
        d = initD;
    }

    /**
     * This method creates an Initiator Session ID of the <code>Random</code> format defined in the iSCSI
     * Standard (RFC3720).
     * 
     * @param seed
     *            The initialization seed for random generator.
     * @return A instance of an <code>ISID</code>.
     */
    public static final ISID createRandom(final long seed) {

        // TODO: Implement Qualifier
        final Random rand = new Random(seed);

        final ISID isid =
            new ISID(Format.RANDOM, Constants.RESERVED_BYTE, (short)rand.nextInt(), (byte)rand.nextInt(),
                (short)0);

        return isid;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Serializes this ISID object ot its byte representation.
     * 
     * @return The byte representation of this ISID object.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     */
    public final long serialize() throws InternetSCSIException {

        checkIntegrity();

        long isid = 0;
        int firstLine = c;

        firstLine |= b << Constants.ONE_BYTE_SHIFT;
        firstLine |= a << Constants.THREE_BYTES_SHIFT;
        firstLine |= t.value() << T_FIELD_SHIFT;

        isid = Utils.getUnsignedLong(firstLine) << Constants.FOUR_BYTES_SHIFT;
        isid |= Utils.getUnsignedLong(d) << Constants.TWO_BYTES_SHIFT;

        return isid;
    }

    /**
     * Parses a given ISID in this ISID obejct.
     * 
     * @param isid
     *            The byte representation of a ISID to parse.
     * @throws InternetSCSIException
     *             If any violation of the iSCSI-Standard emerge.
     */
    final void deserialize(final long isid) throws InternetSCSIException {

        int line = (int)((isid & FIRST_LINE_FLAG_MASK) >>> Constants.FOUR_BYTES_SHIFT);

        t = Format.valueOf((byte)(line >>> T_FIELD_SHIFT));
        a = (byte)(line & A_FIELD_FLAG_MASK >>> Constants.THREE_BYTES_SHIFT);
        b = (short)((line & Constants.MIDDLE_TWO_BYTES_SHIFT) >>> Constants.ONE_BYTE_SHIFT);
        c = (byte)(line & Constants.FOURTH_BYTE_MASK);

        line = (int)(isid & Constants.LAST_FOUR_BYTES_MASK);
        d = (short)((line & Constants.FIRST_TWO_BYTES_MASK) >>> Constants.TWO_BYTES_SHIFT);

        checkIntegrity();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Creates a string with all fields of this ISID object.
     * 
     * @return The string representation.
     */
    public final String toString() {

        final StringBuilder sb = new StringBuilder(Constants.LOG_INITIAL_SIZE);

        sb.append(Utils.LOG_OUT_INDENT + "ISID:\n");
        Utils.printField(sb, "T", t.value(), 2);
        Utils.printField(sb, "A", a, 2);
        Utils.printField(sb, "B", b, 2);
        Utils.printField(sb, "C", c, 2);
        Utils.printField(sb, "D", d, 2);

        return sb.toString();
    }

    /**
     * This method compares a given ISID object with this object for value
     * equality.
     * 
     * @param isid
     *            The given ISID object to check.
     * @return <code>True</code>, if the values of the two ISID objects are
     *         equal. Else <code>false</code>.
     */
    public final boolean equals(final ISID isid) {

        do {
            if (t != isid.t) {
                break;
            }

            if (a != isid.a) {
                break;
            }

            if (b != isid.b) {
                break;
            }

            if (c != isid.c) {
                break;
            }

            if (d != isid.d) {
                break;
            }

            return true;
        } while (false);

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode() {

        return super.hashCode();
    }

    /**
     * This methods resets their attributes to the defaults.
     */
    public final void clear() {

        t = Format.OUI_FORMAT;
        a = 0;
        b = 0;
        c = 0;
        d = 0;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the value of the field <code>A</code>.
     * 
     * @return The value of the field <code>A</code>.
     */
    public final byte getA() {

        return a;
    }

    /**
     * Returns the value of the field <code>B</code>.
     * 
     * @return The value of the field <code>B</code>.
     */
    public final short getB() {

        return b;
    }

    /**
     * Returns the value of the field <code>C</code>.
     * 
     * @return The value of the field <code>C</code>.
     */
    public final byte getC() {

        return c;
    }

    /**
     * Returns the value of the field <code>D</code>.
     * 
     * @return The value of the field <code>D</code>.
     */
    public final short getD() {

        return d;
    }

    /**
     * Returns the value of the field <code>T</code>.
     * 
     * @return The value of the field <code>T</code>.
     */
    public final Format getT() {

        return t;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This method checks, if all fields are valid. In these cases an exception
     * will be thrown.
     * 
     * @throws InternetSCSIException
     *             If the integrity is violated.
     */
    protected final void checkIntegrity() throws InternetSCSIException {

        String exceptionMessage = "";
        switch (t) {
        case OUI_FORMAT:
            break;

        case IANA_ENTERPRISE_NUMBER:
            break;

        case RANDOM:
            if (d != 0) {
                exceptionMessage = "The D field is reserved in this ISID Format.";
            }
            break;

        case RESERVED:
            if (a != 0 && b != 0 && c != 0 && d != 0) {
                exceptionMessage = "This ISID is not valid. All";
            }
            break;

        default:
            exceptionMessage = "This format is not supported.";
        }

        if (exceptionMessage.length() > 0) {
            throw new InternetSCSIException(exceptionMessage);
        } else {
            // no error occured... Nice! :-)
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

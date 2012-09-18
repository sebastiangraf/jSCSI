/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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
package org.jscsi.utils;

import java.nio.ByteBuffer;

import org.jscsi.exception.InternetSCSIException;

/**
 * This class encapsulate all the needed common constants and methods with are
 * needed by the used classes for the parsing and logging process. There are
 * also common used bit masks for the extraction of the needed field in such a
 * iSCSI message.
 * 
 * @author Volker Wildi, University of Konstanz
 */
public final class Utils {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Indent for each line (dependent of the level of indent). */
    public static final String LOG_OUT_INDENT = "  ";

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Bit mask to extract the first byte of a <code>32</code> bit number. */
    public static final int FIRST_BYTE_MASK = 0xFF000000;

    /** Bit mask to extract the second byte of a <code>32</code> bit number. */
    public static final int SECOND_BYTE_MASK = 0x00FF0000;

    /** Bit mask to extract the third byte of a <code>32</code> bit number. */
    public static final int THIRD_BYTE_MASK = 0x0000FF00;

    /** Bit mask to extract the fourth byte of a <code>32</code> bit number. */
    public static final int FOURTH_BYTE_MASK = 0x000000FF;

    /** The flag mask to convert an integer number to a long number. */
    private static final long INT_FLAG_MASK_LONG = 0x00000000FFFFFFFFL;

    /** The flag mask to convert a short number to a long number. */
    private static final long SHORT_FLAG_MASK_LONG = 0x000000000000FFFFL;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Method to guarantee that a given field is not zero.
     * 
     * @param field
     *            Field to check
     * @throws InternetSCSIException
     *             If the field is not reserved, then throw an exception
     */
    public static final void isReserved(final long field) throws InternetSCSIException {

        if (field != 0) {
            throw new InternetSCSIException("Field is reserved, so it must be zero.");
        }
    }

    /**
     * Checks for equality of a given value with the expected value.
     * 
     * @param field
     *            This value should be equal to the expected value
     * @param expected
     *            This is what we expect
     * @throws InternetSCSIException
     *             If this comparison failed, this exception will be thrown
     */
    public static final void isExpected(final int field, final int expected) throws InternetSCSIException {

        if (field != expected) {
            throw new InternetSCSIException("This field does not contain the expected value.");
        }
    }

    /**
     * Checks with a given int is unequal to zero.
     * 
     * @param num
     *            Number to check
     * @return Returns true if the line unequal than zero
     */
    public static final boolean isBitSet(final int num) {

        return num != 0;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This methods creates an easy to use interface to print out a logging
     * message of a specific variable.
     * 
     * @param sb
     *            StringBuilder to directly write the logging messages in.
     * @param fieldName
     *            The name of the variable.
     * @param fieldValue
     *            The value of the given variable.
     * @param indent
     *            The level of indention.
     */
    public static final void printField(final StringBuilder sb, final String fieldName,
        final String fieldValue, final int indent) {

        indent(sb, indent);
        sb.append(fieldName);
        sb.append(": ");
        sb.append(fieldValue);
        sb.append("\n");
    }

    /**
     * This methods creates an easy to use interface to print out a logging
     * message of a specific variable.
     * 
     * @param sb
     *            StringBuilder to directly write the logging messages in.
     * @param fieldName
     *            The name of the variable.
     * @param fieldValue
     *            The value of the given variable.
     * @param indent
     *            The level of indention.
     */
    public static final void printField(final StringBuilder sb, final String fieldName, final int fieldValue,
        final int indent) {

        indent(sb, indent);
        sb.append(fieldName);
        sb.append(": 0x");
        sb.append(Integer.toHexString(fieldValue));
        sb.append("\n");
    }

    /**
     * This methods creates an easy to use interface to print out a logging
     * message of a specific variable.
     * 
     * @param sb
     *            StringBuilder to directly write the logging messages in.
     * @param fieldName
     *            The name of the variable.
     * @param fieldValue
     *            The value of the given variable.
     * @param indent
     *            The level of indention.
     */
    public static final void printField(final StringBuilder sb, final String fieldName,
        final long fieldValue, final int indent) {

        indent(sb, indent);
        sb.append(fieldName);
        sb.append(": 0x");
        sb.append(Long.toHexString(fieldValue));
        sb.append("\n");
    }

    /**
     * This methods creates an easy to use interface to print out a logging
     * message of a specific variable.
     * 
     * @param sb
     *            StringBuilder to directly write the logging messages in.
     * @param fieldName
     *            The name of the variable.
     * @param fieldValue
     *            The value of the given variable.
     * @param indent
     *            The level of indention.
     */
    public static final void printField(final StringBuilder sb, final String fieldName,
        final byte fieldValue, final int indent) {

        printField(sb, fieldName, (int)fieldValue, indent);
    }

    /**
     * This methods creates an easy to use interface to print out a logging
     * message of a specific variable.
     * 
     * @param sb
     *            StringBuilder to directly write the logging messages in.
     * @param fieldName
     *            The name of the variable.
     * @param fieldValue
     *            The value of the given variable.
     * @param indent
     *            The level of indention.
     */
    public static final void printField(final StringBuilder sb, final String fieldName,
        final boolean fieldValue, final int indent) {

        indent(sb, indent);
        sb.append(fieldName);
        sb.append(": ");
        sb.append(fieldValue);
        sb.append("\n");
    }

    /**
     * This methods creates an easy to use interface to print out a logging
     * message of a specific variable.
     * 
     * @param sb
     *            StringBuilder to directly write the logging messages in.
     * @param fieldName
     *            The name of the variable.
     * @param fieldValue
     *            The value of the given variable.
     * @param indent
     *            The level of indention.
     */
    public static final void printField(final StringBuilder sb, final String fieldName,
        final ByteBuffer fieldValue, final int indent) {

        fieldValue.rewind();
        indent(sb, indent);
        sb.append(fieldName);
        sb.append(": ");
        sb.append(fieldValue);
        sb.append("\n");
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    // --------------------------------------------------------------------------

    /**
     * Appends to a given StringBuilder the given indents depending on the
     * indent level.
     * 
     * @param sb
     *            StringBuilder to write in.
     * @param indent
     *            The number (level) of indents.
     */
    private static final void indent(final StringBuilder sb, final int indent) {

        for (int i = 0; i < indent; i++) {
            sb.append(LOG_OUT_INDENT);
        }
    }

    /**
     * This method converts a byte with the highest (sign) bit set, to an
     * unsigned int value.
     * 
     * @param b
     *            The signed <code>byte</code> number.
     * @return The unsigned <code>int</code> number.
     */
    public static final int getUnsignedInt(final byte b) {

        return b & FOURTH_BYTE_MASK;
    }

    /**
     * This method converts an integer with the highest (sign) bit set, to an
     * unsigned long value.
     * 
     * @param i
     *            The signed <code>int</code> number.
     * @return The unsigned <code>long</code> number.
     */
    public static final long getUnsignedLong(final int i) {

        return i & INT_FLAG_MASK_LONG;
    }

    /**
     * This method converts an short integer with the highest (sign) bit set, to
     * an unsigned long value.
     * 
     * @param i
     *            The signed integer number.
     * @return The unsigned long number.
     */
    public static final long getUnsignedLong(final short i) {

        return i & SHORT_FLAG_MASK_LONG;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * To disable the creation of such an object, declare the constructor as
     * private.
     */
    private Utils() {

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

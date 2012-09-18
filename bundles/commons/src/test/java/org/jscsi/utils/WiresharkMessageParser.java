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

import org.jscsi.parser.Constants;

/**
 * This class parses a given byte representation (in hexadecimal numbers) of an
 * ethereal trace log into an integer array.
 * 
 * @author Volker Wildi
 */
public final class WiresharkMessageParser {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The radix of hexadecimal numbers. */
    private static final int HEX_RADIX = 16;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * To disable the creation of such an object, declare the constructor as
     * private.
     */
    private WiresharkMessageParser() {

    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Parses a given iSCSI message from wireshark to an integer array.
     * 
     * @param str
     *            The given iSCSI message from wireshark
     * @return The parsed integer array
     */
    public static int[] parseToIntArray(final String str) {

        final String[] tokens = str.trim().split(" ");
        final int n = tokens.length;

        final int[] numbers = new int[n / Constants.BYTES_PER_INT];
        for (int i = 0; i < n; i++) {
            numbers[i / Constants.BYTES_PER_INT] <<= Constants.ONE_BYTE_SHIFT;
            numbers[i / Constants.BYTES_PER_INT] |= Integer.parseInt(tokens[i], HEX_RADIX);
        }

        return numbers;
    }

    /**
     * Parses a given iSCSI message from wireshark to a <code>ByteBuffer</code>.
     * 
     * @param str
     *            The given iSCSI message from wireshark
     * @return The parsed integer array
     */
    public static ByteBuffer parseToByteBuffer(final String str) {

        final String[] tokens = str.trim().split(" ");
        int n = tokens.length;

        if (str.equals("")) {
            n--;
        }

        final ByteBuffer numbers = ByteBuffer.allocate(n);
        short val = 0;
        for (int i = 0; i < n; i++) {
            val = Short.parseShort(tokens[i], HEX_RADIX);
            numbers.put((byte)val);
        }

        numbers.rewind();

        return numbers;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

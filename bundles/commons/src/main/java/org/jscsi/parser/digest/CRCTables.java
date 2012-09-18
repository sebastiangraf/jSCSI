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
package org.jscsi.parser.digest;

/**
 * <h1>CRCTables</h1>
 * <p>
 * This class generates tables for CRC algorithm. It is possible to change the generator polynom and the
 * offset. Usage:
 * <ol>
 * <li>Make new <code>CRCTables</code> object.</li>
 * <li>Put <code>generatorPolynom</code> in the constructor.</li>
 * <li>Invoke the method <code>getTable</code> with offset as an argument. The offset is an integer number,
 * which specifies the number of bits to shift, e.g. for <code>A</code> offset is <code>56</code>, for
 * <code>D</code> <code>32</code>.</li>
 * <li>You will have CRC table(<code>0-255</code>) for the requested polynom.</li>
 * </ol>
 * 
 * @author Stepan
 */
public final class CRCTables {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The size of a table. */
    private static final int SIZE_OF_TABLE = 256;

    /** Bit mask for throwing away the 33-rd bit. */
    private static final long BIN_MASK_33BIT = 0x00000000FFFFFFFFL;

    /** Bit mask with 1 only in the first position. */
    private static final long BIN_MASK_1FIRST_ONLY = 0x8000000000000000L;

    /** Bit mask with 1 only in the first position for int. */
    private static final int BIN_MASK_1FIRST_ONLY_INT = 0x80000000;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The generator polynom. */
    private final long generatorPolynom;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new, empty <code>CRCTables</code> object with the
     * initialized generator polynom.
     * 
     * @param initGeneratorPolynom
     *            The generator polynom to use.
     */
    public CRCTables(final long initGeneratorPolynom) {

        // FIXME: Really needed?
        generatorPolynom = initGeneratorPolynom;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns all remainders of the polynomial division for the given offset.
     * 
     * @param offset
     *            The numbers of bits to shift, e.g. for <code>A</code> offset
     *            is <code>56</code>, for <code>D</code> <code>32</code>.
     * @return The CRC table with all remainders for the given offset.
     */
    public final int[] getTable(final int offset) {

        final int[] table = new int[SIZE_OF_TABLE];
        long numberToCalculate;

        for (int number = 0; number < table.length; number++) {
            numberToCalculate = Integer.reverseBytes(Integer.reverse(number));
            table[number] = calculateCRC32(numberToCalculate << offset);
        }

        return table;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Method for CRC calculation for the just one value (type long) Algorithm:
     * Load the register with zero bits. Reverse message Augment the message by
     * appending W zero bits to the end of it. While (more message bits) Begin
     * Shift the register left by one bit, reading the next bit of the augmented
     * message into register bit position 0. If (a 1 bit popped out of the
     * register during step 3) Register = Register XOR Poly. End Reverse
     * register The register now contains the CRC. Notes: W=32, that's why we
     * have offsets 32, 40, 48 and 56 instead of 0,8,16,24 Size of register=W;
     * 
     * @param value
     *            A <code>long</code> value, for which the CRC should be
     *            calculated.
     * @return The remainder of the polynomial division --> CRC.
     */
    protected final int calculateCRC32(final long value) {

        long l = value;
        int register = 0;
        // We are going to throw the first bit away. Then making int from long.
        final int gxInt = (int)(generatorPolynom & BIN_MASK_33BIT);

        // the first bit before bit shifting in the input polynom
        int bit = 0;

        for (int i = 0; i < Long.SIZE; i++) {
            // is the highest bit set?
            if ((l & BIN_MASK_1FIRST_ONLY) == 0) {
                bit = 0;
            } else {
                bit = 1;
            }

            // is the highest bit set?
            if ((register & BIN_MASK_1FIRST_ONLY_INT) == 0) {
                register = register << 1;
                l = l << 1;
                register += bit;
            } else {
                register = register << 1;
                l = l << 1;
                register += bit;
                register = register ^ gxInt;
            }

        }

        return Integer.reverse(register);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

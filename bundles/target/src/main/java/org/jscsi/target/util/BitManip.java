package org.jscsi.target.util;

/**
 * A utility class with static methods for accessing individual bits of bytes.
 * <p>
 * The value of a single bit can be easily retrieved with the {@link #getBit(byte, int)} method.
 * <p>
 * Setting a bit is a little bit more complicated. The byte to be changed must be set to the return value of
 * {@link #getByteWithBitSet(byte, int, boolean)}.
 * 
 * @author Andreas Ergenzinger
 */
public final class BitManip {

    /**
     * Sets a single bit. If the <i>value</i> parameter is <code>true</code>,
     * the bit will be set to <code>one</code>, and to <code>zero</code> otherwise. All other bits will be
     * left unchanged.
     * <p>
     * The bits are numbered in big-endian format, from 0 (LSB) to 7 (MSB).
     * <p>
     * <code>          
     * +---+---+---+---+---+---+---+---+<br>
     * | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 | bit number<br> 
     * +---+---+---+---+---+---+---+---+<br>
     * </code>
     * 
     * @param b
     *            the original byte value
     * @param bitNumber
     *            the big-endian position of the bit to be changed, from 0 to 7
     * @param value
     *            <code>true</code> for <i>1</i>, <code>false</code> for
     *            <i>0</i>
     * @return the edited byte value
     */
    public static final byte getByteWithBitSet(final byte b, final int bitNumber, final boolean value) {

        int number = b;

        if (value) {
            // make sure bit is set to true
            int mask = 1;
            mask <<= bitNumber;
            number |= mask;
        } else {
            int mask = 1;
            mask <<= bitNumber;
            mask ^= 255;// flip bits
            number &= mask;
        }

        return (byte)number;
    }

    /**
     * Returns <code>true</code>, if the bit at the given position is set to <code>one</code> and
     * <code>false</code> if it is set to <code>zero</code> . The bits are numbered in big-endian format, from
     * 0 (LSB) to 7 (MSB).
     * <p>
     * <code>
     * +---+---+---+---+---+---+---+---+<br>
     * | 7 | 6 | 5 | 4 | 3 | 2 | 1 | 0 | bit number<br> 
     * +---+---+---+---+---+---+---+---+<br>
     * </code>
     * 
     * @param b
     *            the byte value in question
     * @param bitNumber
     *            the big-endian position of the bit to be changed, from 0 to 7
     * @return <code>true</code> if bit is set to <code>one</code>, else <code>false</code>
     */
    public static boolean getBit(final byte b, final int bitNumber) {
        int number = b;
        number >>>= bitNumber;
        number &= 1;
        if (number == 1)
            return true;
        return false;
    }
}

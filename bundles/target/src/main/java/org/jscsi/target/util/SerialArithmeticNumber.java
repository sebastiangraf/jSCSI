package org.jscsi.target.util;

/**
 * A class for serial number arithmetics, as defined in <code>[RFC 1982]</code>,
 * with <code>SERIAL_BITS = 32</code>.
 * <p>
 * Unlike the original algorithm, this implementation is based on unsigned subtraction, resolving the issue of
 * undefined comparisons, however introducing one additional oddity - each member of a pair of serial numbers
 * with a wrapped distance of <code>2^(SERIAL_BITS - 1)</code> will be considered less than (and greater than)
 * the other one.
 * 
 * @author Andreas Ergenzinger
 */
public final class SerialArithmeticNumber {

    /**
     * The signed integer representation of the serial arithmetic number.
     */
    private int value;

    /**
     * Creates a new {@link SerialArithmeticNumber} with a starting {@link #value} of zero.
     */
    public SerialArithmeticNumber() {
        this(0);
    }

    /**
     * Creates a new {@link SerialArithmeticNumber} with the specified starting {@link #value}.
     * 
     * @param value
     *            the initial {@link #value}
     */
    public SerialArithmeticNumber(final int value) {
        this.value = value;
    }

    /**
     * Increments the {@link SerialArithmeticNumber}'s {@link #value} by one.
     */
    public void increment() {
        ++value;
    }

    /**
     * Returns <code>true</code> if the parameter matches the {@link #value} and <code>false</code> if it does
     * not.
     * 
     * @param serialArithmeticNumber
     *            the serial arithmetic number to match
     * @return <code>true</code> if the parameter matches the {@link #value} and <code>false</code> if it does
     *         not
     */
    public boolean equals(final int serialArithmeticNumber) {
        return value == serialArithmeticNumber;
    }

    /**
     * Returns <code>true</code> if the parameter is less than the {@link #value} in serial number arithmetics
     * and <code>false</code> if it
     * is not.
     * 
     * @param serialArithmeticNumber
     *            the serial arithmetic number to match
     * @return <code>true</code> if the parameter is less than the {@link #value} in serial number arithmetics
     *         and <code>false</code> if it is not
     */
    public boolean lessThan(final int serialArithmeticNumber) {
        if (value - serialArithmeticNumber < 0)
            return true;
        return false;
    }

    /**
     * Returns <code>true</code> if the parameter is greater than the {@link #value} in serial number
     * arithmetics and <code>false</code> if it
     * is not.
     * 
     * @param serialArithmeticNumber
     *            the serial arithmetic number to match
     * @return <code>true</code> if the parameter is greater than the {@link #value} in serial number
     *         arithmetics and <code>false</code> if it is not
     */
    public boolean greaterThan(final int serialArithmeticNumber) {
        if (serialArithmeticNumber - value < 0)
            return true;
        return false;
    }

    /**
     * Returns the {@link SerialArithmeticNumber}'s {@link #value}.
     * 
     * @return the {@link #value}
     */
    public int getValue() {
        return value;
    }
}

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

/**
 * <h1>SerialArithmeticNumber</h1>
 * <p/>
 * This class encapsulate the behavior of how to compare and increment a number in Serial Number Arithmetic,
 * which is defined in [RFC1982].
 * 
 * @author Volker Wildi, University of Konstanz
 */
public final class SerialArithmeticNumber implements Comparable<Integer> {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This is the wrap around divisor (2**32) of the modulo operation used by
     * incrementing the sequence numbers. See [RFC1982] for details.
     */
    private static final long MAXIMUM_VALUE = 0xFFFFFFFFL;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** The current value. */
    private long serialNumber;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Default constructor to create a new, empty <code>SerialArithmeticNumber</code> instance, which is
     * initialized to <code>0</code>.
     */
    public SerialArithmeticNumber() {

    }

    /**
     * Constructor to create a new <code>SerialArithmeticNumber</code> instance,
     * which is initialized to <code>startValue</code>.
     * 
     * @param startValue
     *            The start value.
     */
    public SerialArithmeticNumber(final int startValue) {

        if (startValue < 0 || startValue > MAXIMUM_VALUE) {
            throw new IllegalArgumentException(startValue + " is out of range.");
        }

        serialNumber = startValue;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Same as the <code>compareTo</code> method only with the exception of
     * another parameter type.
     * 
     * @param anotherSerialNumber
     *            The number to compare with.
     * @return a negative integer, zero, or a positive integer as this <code>SerialArithmeticNumber</code> is
     *         less than, equal to, or
     *         greater than the given number.
     */
    public final int compareTo(final int anotherSerialNumber) {

        return compareTo(new Integer(anotherSerialNumber));
    }

    /** {@inheritDoc} */
    public final synchronized int compareTo(final Integer anotherSerialNumber) {

        long diff = serialNumber - Utils.getUnsignedLong(anotherSerialNumber.intValue());
        if (diff >= MAXIMUM_VALUE) {
            diff -= (MAXIMUM_VALUE + 1);
        } else if (diff < -MAXIMUM_VALUE) {
            diff += (MAXIMUM_VALUE + 1);
        }
        return (int)diff;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the current value of this <code>SerialArithmeticNumber</code> instance.
     * 
     * @return The current value of this <code>SerialArithmeticNumber</code> instance.
     */
    public final synchronized int getValue() {

        return (int)serialNumber;
    }

    /**
     * Increments the value of this <code>SerialArithmeticNumber</code> instance.
     */
    public final synchronized void increment() {

        if (serialNumber == MAXIMUM_VALUE) {
            serialNumber = 0;
        } else {
            serialNumber++;
        }
    }

    /**
     * Updates the value of this <code>SerialArithmeticNumber</code> instance to
     * the given one.
     * 
     * @param newValue
     *            The new value.
     */
    public final synchronized void setValue(final int newValue) {

        if (newValue < 0 || newValue > MAXIMUM_VALUE) {
            throw new IllegalArgumentException(newValue + " is out of range.");
        }

        serialNumber = newValue;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
}

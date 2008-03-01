/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id: SerialArithmeticNumber.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.core.utils;


/**
 * <h1>SerialArithmeticNumber</h1>
 * <p/>
 * 
 * This class encapsulate the behavior of how to compare and increment a number
 * in Serial Number Arithmetic, which is defined in [RFC1982].
 * 
 * @author Volker Wildi
 */
final public class SerialArithmeticNumber implements Comparable<Integer> {

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
   * Default constructor to create a new, empty
   * <code>SerialArithmeticNumber</code> instance, which is initialized to
   * <code>0</code>.
   */
  public SerialArithmeticNumber() {

  }

  /**
   * Constructor to create a new <code>SerialArithmeticNumber</code> instance,
   * which is initialized to <code>startValue</code>.
   * 
   * @param startValue
   *          The start value.
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
   *          The number to compare with.
   * @return a negative integer, zero, or a positive integer as this
   *         <code>SerialArithmeticNumber</code> is less than, equal to, or
   *         greater than the given number.
   */
  public final int compareTo(final int anotherSerialNumber) {

    return compareTo(new Integer(anotherSerialNumber));
  }

  /** {@inheritDoc} */
  public final synchronized int compareTo(final Integer anotherSerialNumber) {

    long diff = serialNumber
        - Utils.getUnsignedLong(anotherSerialNumber.intValue());
    if (diff >= MAXIMUM_VALUE) {
      diff -= (MAXIMUM_VALUE + 1);
    } else if (diff < -MAXIMUM_VALUE) {
      diff += (MAXIMUM_VALUE + 1);
    }
    return (int) diff;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Returns the current value of this <code>SerialArithmeticNumber</code>
   * instance.
   * 
   * @return The current value of this <code>SerialArithmeticNumber</code>
   *         instance.
   */
  public final synchronized int getValue() {

    return (int) serialNumber;
  }

  /**
   * Increments the value of this <code>SerialArithmeticNumber</code>
   * instance.
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
   *          The new value.
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

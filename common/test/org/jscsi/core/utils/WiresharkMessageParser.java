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
 * $Id: WiresharkMessageParser.java 2500 2007-03-05 13:29:08Z lemke $
 * 
 */

package org.jscsi.core.utils;

import java.nio.ByteBuffer;

import org.jscsi.parser.Constants;

/**
 * This class parses a given byte representation (in hexadecimal numbers) of an
 * ethereal trace log into an integer array.
 * 
 * @author Volker Wildi
 * 
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
   * 
   */
  private WiresharkMessageParser() {

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Parses a given iSCSI message from wireshark to an integer array.
   * 
   * @param str
   *          The given iSCSI message from wireshark
   * @return The parsed integer array
   */
  public static int[] parseToIntArray(final String str) {

    final String[] tokens = str.trim().split(" ");
    final int n = tokens.length;

    final int[] numbers = new int[n / Constants.BYTES_PER_INT];
    for (int i = 0; i < n; i++) {
      numbers[i / Constants.BYTES_PER_INT] <<= Constants.ONE_BYTE_SHIFT;
      numbers[i / Constants.BYTES_PER_INT] |= Integer.parseInt(tokens[i],
          HEX_RADIX);
    }

    return numbers;
  }

  /**
   * Parses a given iSCSI message from wireshark to a <code>ByteBuffer</code>.
   * 
   * @param str
   *          The given iSCSI message from wireshark
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
      numbers.put((byte) val);
    }

    numbers.rewind();

    return numbers;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

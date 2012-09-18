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
package org.jscsi.parser;

/**
 * <h1>Constants</h1>
 * <p>
 * This class defines all the constants needed by the parsing and serializing process.
 * 
 * @author Volker Wildi
 */
public final class Constants {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Number of bits in a byte. */
    private static final int BITS_PER_BYTE = 8;

    /** The java data type int need <code>4</code> bytes. */
    public static final int BYTES_PER_INT = Integer.SIZE / BITS_PER_BYTE;

    /** The intial size of the StringBuilder used for logging. */
    public static final int LOG_INITIAL_SIZE = 50;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Shift a given number by <code>8</code> bits or <code>1</code> byte.
     */
    public static final int ONE_BYTE_SHIFT = 8;

    /**
     * Shift a given number by <code>16</code> bits or <code>2</code> bytes.
     */
    public static final int TWO_BYTES_SHIFT = 16;

    /**
     * Shift a given number by <code>24</code> bits or <code>3</code> bytes.
     */
    public static final int THREE_BYTES_SHIFT = 24;

    /**
     * Shift a given number by <code>32</code> bits or <code>4</code> bytes.
     */
    public static final int FOUR_BYTES_SHIFT = 32;

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

    /**
     * Bit mask to extract the first two bytes of a <code>32</code> bit number.
     */
    public static final int FIRST_TWO_BYTES_MASK = 0xFFFF0000;

    /**
     * Bit mask to extract the second and third bytes of a <code>32</code> bit
     * number.
     */
    public static final int MIDDLE_TWO_BYTES_SHIFT = 0x00FFFF00;

    /** Bit mask to extract the last bytes of a <code>32</code> bit number. */
    public static final int LAST_TWO_BYTES_MASK = 0x0000FFFF;

    /**
     * Bit mask to extract the three last bytes of a <code>32</code> bit number.
     */
    public static final int LAST_THREE_BYTES_MASK = 0x00FFFFFF;

    /**
     * Bit mask to extract the last four bytes of a <code>64</code> bit number.
     */
    public static final long LAST_FOUR_BYTES_MASK = 0x00000000FFFFFFFFL;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Reserved fields are marked as zeros in a <code>byte</code> data type. */
    public static final byte RESERVED_BYTE = 0x00;

    /** Reserved fields are marked as zeros in a <code>short</code> data type. */
    public static final short RESERVED_SHORT = 0x0;

    /** Reserved fields are marked as zeros in a <code>integer</code> data type. */
    public static final int RESERVED_INT = 0x0;

    /** Reserved fields are marked as zeros in a <code>long</code> data type. */
    public static final long RESERVED_LONG = 0x0L;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** Bit mask to extract the continue flag of a <code>32</code> bit number. */
    public static final int CONTINUE_FLAG_MASK = 0x00400000;

    /**
     * Set for Residual Overflow. In this case, the Residual Count indicates the
     * number of bytes that were not transferred because the initiator's
     * Expected Data Transfer Length was not sufficient. For a bidirectional
     * operation, the Residual Count contains the residual for the write
     * operation.
     */
    public static final int RESIDUAL_OVERFLOW_FLAG_MASK = 0x00040000;

    /**
     * Set for Residual Underflow. In this case, the Residual Count indicates
     * the number of bytes that were not transferred out of the number of bytes
     * that were expected to be transferred. For a bidirectional operation, the
     * Residual Count contains the residual for the write operation.
     */
    public static final int RESIDUAL_UNDERFLOW_FLAG_MASK = 0x00020000;

    /**
     * Set for Bidirectional Read Residual Overflow. In this case, the
     * Bidirectional Read Residual Count indicates the number of bytes that were
     * not transferred to the initiator because the initiator’s Expected
     * Bidirectional Read Data Transfer Length was not sufficient.
     */
    public static final int READ_RESIDUAL_OVERFLOW_FLAG_MASK = 0x00080000;

    /**
     * Set for Bidirectional Read Residual Underflow. In this case, the
     * Bidirectional Read Residual Count indicates the number of bytes that were
     * not transferred to the initiator out of the number of bytes expected to
     * be transferred.
     */
    public static final int READ_RESIDUAL_UNDERFLOW_FLAG_MASK = 0x00100000;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

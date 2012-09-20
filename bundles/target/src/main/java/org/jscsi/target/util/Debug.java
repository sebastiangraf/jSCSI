package org.jscsi.target.util;

import java.nio.ByteBuffer;

/**
 * This class provides static methods for printing the bytes of {@link ByteBuffer} objects. The individual
 * values will be printed in
 * hexadecimal format in a tabular arrangement.
 * 
 * @author Andreas Ergenzinger, University of Konstanz
 */
public class Debug {

    /**
     * The number of bytes to print per line.
     */
    private static final int BYTES_PER_LINE = 4;

    /**
     * Prints the <i>buffer</i> content to <code>System.out</code>.
     * 
     * @param buffer
     *            contains the bytes to print
     */
    public static void printByteBuffer(final ByteBuffer buffer) {
        System.out.println(byteBufferToString(buffer));
    }

    /**
     * Returns a string containing the buffered values in the defined format.
     * 
     * @param buffer
     *            contains the bytes to return in the {@link String}
     * @return a {@link String} with the values in tabular arrangement
     */
    public static String byteBufferToString(final ByteBuffer buffer) {

        if (buffer == null)
            return "null";

        final int numberOfBytes = buffer.limit();

        final StringBuilder sb = new StringBuilder();
        buffer.position(0);
        int value;
        for (int i = 1; i <= numberOfBytes; ++i) {
            sb.append("0x");
            value = 255 & buffer.get();
            if (value < 16)
                sb.append("0");
            sb.append(Integer.toHexString(value));
            if (i % BYTES_PER_LINE == 0)
                sb.append("\n");
            else
                sb.append("   ");
        }

        return sb.toString();
    }

}

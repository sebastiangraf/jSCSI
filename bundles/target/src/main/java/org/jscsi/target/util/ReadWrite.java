package org.jscsi.target.util;

import java.nio.ByteBuffer;

/**
 * This utility class contains static methods for reading/writing integers of
 * various lengths and character strings from/to {@link ByteBuffer} objects and
 * byte arrays.
 * 
 * @author Andreas Ergenzinger
 */
public final class ReadWrite {

    /**
     * Reads a specified byte from a {@link ByteBuffer} and returns its value as
     * an unsigned integer.
     * 
     * @param buffer
     *            the {@link ByteBuffer} containing the byte
     * @param start
     *            the index position of the byte in the {@link ByteBuffer}
     * @return the unsigned integer value of the byte
     */
    public static final int readOneByteInt(ByteBuffer buffer, int start) {
        final byte b = buffer.get(start);
        return (b & 255);
    }

    /**
     * Reads a specified byte from a byte array and returns its value as an
     * unsigned integer.
     * 
     * @param array
     *            the array containing the byte
     * @param start
     *            the index position of the byte in the array
     * @return the unsigned integer value of the byte
     */
    public static final int readOneByteInt(byte[] array, int start) {
        return (array[start] & 255);
    }

    /**
     * Reads an unsigned 2-byte integer from two consecutive big-endian-ordered
     * bytes of a {@link ByteBuffer} object and returns the value.
     * 
     * @param buffer
     *            the {@link ByteBuffer} containing the bytes
     * @param start
     *            the index position of the most-significant byte
     * @return the unsigned value of the two-byte integer
     */
    public static final int readTwoByteInt(ByteBuffer buffer, int start) {
        final byte b1 = buffer.get(start);
        final byte b2 = buffer.get(start + 1);
        return ((b1 & 255) << 8) | (b2 & 255);
    }

    /**
     * Reads an unsigned 2-byte integer from two consecutive big-endian-ordered
     * bytes of a byte array and returns the value.
     * 
     * @param array
     *            the byte array containing the bytes
     * @param start
     *            the index position of the most-significant byte
     * @return the unsigned value of the two-byte integer
     */
    public static final int readTwoByteInt(byte[] array, int start) {
        return ((array[start] & 255) << 8) | (array[start + 1] & 255);
    }

    /**
     * Reads an unsigned 3-byte integer from three consecutive
     * big-endian-ordered bytes of a {@link ByteBuffer} object and returns the
     * value.
     * 
     * @param buffer
     *            the {@link ByteBuffer} containing the bytes
     * @param start
     *            the index position of the most-significant byte
     * @return the unsigned value of the three-byte integer
     */
    public static final int readThreeByteInt(ByteBuffer buffer, int start) {
        final byte b1 = buffer.get(start);
        final byte b2 = buffer.get(start + 1);
        final byte b3 = buffer.get(start + 2);
        return ((b1 & 255) << 16) | ((b2 & 255) << 8) | (b3 & 255);
    }

    /**
     * Reads an unsigned 3-byte integer from three consecutive
     * big-endian-ordered bytes of a byte array and returns the value.
     * 
     * @param array
     *            the byte array containing the bytes
     * @param start
     *            the index position of the most-significant byte
     * @return the unsigned value of the three-byte integer
     */
    public static final int readThreeByteInt(byte[] array, int start) {
        return ((array[start] & 255) << 16) | ((array[start + 1] & 255) << 8) | (array[start + 2] & 255);
    }

    /**
     * Reads a (signed) 4-byte integer from four consecutive big-endian-ordered
     * bytes of a {@link ByteBuffer} object and returns the value.
     * 
     * @param buffer
     *            the {@link ByteBuffer} containing the bytes
     * @param start
     *            the index position of the most-significant byte
     * @return the value of the four-byte integer
     */
    public static final int readFourByteInt(ByteBuffer buffer, int start) {
        final byte b1 = buffer.get(start);
        final byte b2 = buffer.get(start + 1);
        final byte b3 = buffer.get(start + 2);
        final byte b4 = buffer.get(start + 3);
        return ((b1 & 255) << 24) | ((b2 & 255) << 16) | ((b3 & 255) << 8) | (b4 & 255);
    }

    /**
     * Reads a (signed) 4-byte integer from four consecutive big-endian-ordered
     * bytes of a byte array and returns the value.
     * 
     * @param array
     *            the byte array containing the bytes
     * @param start
     *            the index position of the most-significant byte
     * @return the unsigned value of the four-byte integer
     */
    public static final int readFourByteInt(byte[] array, int start) {
        return ((array[start] & 255) << 24) | ((array[start + 1] & 255) << 16)
            | ((array[start + 2] & 255) << 8) | (array[start + 3] & 255);
    }

    /**
     * Puts the characters in the passed String into an array of one or more
     * ByteBuffers and returns it.
     * <p>
     * If the String does not end with a null character, one will be appended. If the String's length is
     * larger than the specified <i>bufferSize</i>, all but the last ByteBuffer will have <i>capacity() =
     * bufferSize</i>, the last one will contain the remaining String characters.
     * 
     * @param string
     *            the String to be put into the ByteBuffer
     * @param bufferSize
     *            the maximum size of the returned ByteBuffers
     * @return an array of ByteBuffers containing the passed String
     */
    public static ByteBuffer[] stringToTextDataSegments(String string, int bufferSize) {

        int numberOfParts = string.length() / bufferSize + 1;
        ByteBuffer[] segments = new ByteBuffer[numberOfParts];

        int bytesRemaining = string.length();
        for (int i = 0; i < numberOfParts; ++i) {
            if (bytesRemaining > bufferSize) {
                segments[i] = stringToByteBuffer(string.substring(i * bufferSize, (i + 1) * bufferSize));
            } else {
                segments[i] = stringToByteBuffer(string.substring(i * bufferSize));
            }
        }
        return segments;
    }

    /**
     * Puts the characters in the passed String into a ByteBuffer of equal
     * length and returns it.
     * 
     * @param string
     *            any String
     * @return a ByteBuffer containing the characters of the passed String
     */
    private static ByteBuffer stringToByteBuffer(final String string) {
        final ByteBuffer buffer = ByteBuffer.allocate(string.length());
        for (int i = 0; i < string.length(); ++i)
            buffer.put((byte)string.charAt(i));
        buffer.clear();
        return buffer;
    }

    /**
     * Appends the content of a {@link ProtocolDataUnit}'s (text) data segment
     * to a {@link StringBuilder};
     * 
     * @param byteBuffer
     *            the PDU's data segment
     * @param stringBuilder
     *            the {@link StringBuilder} that will be extended
     */
    public static final void appendTextDataSegmentToStringBuffer(final ByteBuffer byteBuffer,
        final StringBuilder stringBuilder) {
        final String s = new String(byteBuffer.array());
        stringBuilder.append(s);
    }

    /**
     * Writes the given <i>value</i> to the <i>buffer</i> in big-endian format,
     * with the index position of the most significant byte being <i>start</i>.
     * 
     * @param value
     *            the integer to write to the ByteBuffer
     * @param buffer
     *            where the integer will be stored
     * @param start
     *            index of the most significant byte of the stored <i>value</i>
     */
    public static final void writeInt(int value, final ByteBuffer buffer, int start) {
        buffer.position(start);
        buffer.put((byte)(value >>> 24));
        buffer.put((byte)(value >>> 16));
        buffer.put((byte)(value >>> 8));
        buffer.put((byte)value);
    }

    /**
     * Returns the bytes in a {@link ByteBuffer} as a UTF-8 encoded {@link String}.
     * 
     * @param buffer
     *            a {@link ByteBuffer} containing UTF-8 encoded characters.
     * @return a String representation of the <i>buffer</i>'s content
     */
    public static String byteBufferToString(final ByteBuffer buffer) {
        buffer.rewind();
        return new String(buffer.array());
    }

    /**
     * Splits the passed <i>value</i> into bytes and returns them in an array,
     * in big-endian format.
     * 
     * @param value
     *            the long to split
     * @return byte representation of the parameter
     */
    public static byte[] longToBytes(final long value) {
        final byte[] bytes = new byte[8];
        for (int i = 0; i < bytes.length; ++i)
            bytes[i] = (byte)(value >> (8 * (bytes.length - 1 - i)));
        return bytes;
    }

    /**
     * Writes the given <i>value</i> to the <i>buffer</i> in big-endian format,
     * with the index position of the most significant byte being <i>index</i>.
     * 
     * @param value
     *            the integer to write to the ByteBuffer
     * @param buffer
     *            where the integer will be stored
     * @param index
     *            index of the most significant byte of the stored <i>value</i>
     */
    public static void writeLong(final ByteBuffer buffer, final long value, final int index) {
        final byte[] bytes = longToBytes(value);
        buffer.position(index);
        for (int i = 0; i < bytes.length; ++i)
            buffer.put(bytes[i]);
    }

    /**
     * Writes the two least-significant big-endian-ordered bytes of an integer
     * the a specified position in a {@link ByteBuffer}.
     * 
     * @param buffer
     *            where the bytes will be written
     * @param value
     *            the value to convert and copy
     * @param index
     *            the position of the most significant byte in the {@link ByteBuffer}
     */
    public static void writeTwoByteInt(final ByteBuffer buffer, final int value, final int index) {
        buffer.position(index);
        buffer.put((byte)(value >> 8));// most significant byte
        buffer.put((byte)value);// least significant byte
    }

    /**
     * Writes the three least-significant big-endian-ordered bytes of an integer
     * the a specified position in a {@link ByteBuffer}.
     * 
     * @param buffer
     *            where the bytes will be written
     * @param value
     *            the value to convert and copy
     * @param index
     *            the position of the most significant byte in the {@link ByteBuffer}
     */
    public static void writeThreeByteInt(final ByteBuffer buffer, final int value, final int index) {

        buffer.position(index);
        buffer.put((byte)(value >> 16));// most significant byte
        buffer.put((byte)(value >> 8));
        buffer.put((byte)value);// least significant byte
    }

    /**
     * Reads an unsigned 4-byte integer from four consecutive big-endian-ordered
     * bytes of a {@link ByteBuffer} object and returns the value.
     * 
     * @param buffer
     *            the {@link ByteBuffer} containing the bytes
     * @param start
     *            the index position of the most-significant byte
     * @return the value of the unsigned four-byte integer
     */
    public static long readUnsignedInt(final ByteBuffer buffer, final int start) {
        return readFourByteInt(buffer, start) & 0xffffffffL;
    }

}

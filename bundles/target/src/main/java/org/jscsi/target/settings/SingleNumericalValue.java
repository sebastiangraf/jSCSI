package org.jscsi.target.settings;

import java.util.regex.Matcher;

/**
 * A {@link NumericalValue} sub-class for representing single integers.
 * 
 * @author Andreas Ergenzinger
 */
public final class SingleNumericalValue extends NumericalValue {

    /**
     * The represented integer.
     */
    private final int value;

    /**
     * The constructor.
     * <p>
     * This method is private to enforce usage of {@link #create(int)}, similar to the way new
     * {@link NumericalValueRange} objects are created.
     * 
     * @param value
     *            the value of the new {@link SingleNumericalValue}
     */
    private SingleNumericalValue(final int value) {
        this.value = value;
    }

    /**
     * Returns the represented value.
     * 
     * @return the represented value
     */
    public final int getValue() {
        return value;
    }

    /**
     * Returns a new {@link SingleNumericalValue} with the specified value.
     * 
     * @param value
     *            the value of the returned {@link SingleNumericalValue}
     * @return a new {@link SingleNumericalValue}
     */
    public static final SingleNumericalValue create(final int value) {
        return new SingleNumericalValue(value);
    }

    /**
     * Parses a {@link SingleNumericalValue} from a {@link String}. The
     * parameter must contain an integer value in decimal, hexadecimal, or
     * Base64 format. If it does not, <code>null</code> will be returned.
     * 
     * @param value
     *            the {@link String} to parse
     * @return a {@link SingleNumericalValue} with the parsed value or <code>null</code>
     */
    public static final SingleNumericalValue parseSingleNumericValue(final String value) {
        // decimal constant
        final Matcher decimalMatcher = DECIMAL_CONSTANT_PATTERN.matcher(value);
        if (decimalMatcher.matches())
            return new SingleNumericalValue(parseDecimalConstantString(value));
        // hex constant
        final Matcher hexMatcher = HEX_CONSTANT_PATTERN.matcher(value);
        if (hexMatcher.matches())
            return new SingleNumericalValue(parseHexConstantString(value));
        // base 64 constant
        final Matcher base64Matcher = BASE_64_CONSTANT_PATTERN.matcher(value);
        if (base64Matcher.matches())
            return new SingleNumericalValue(parseBase64ConstantString(value));
        // none of the above
        return null;
    }

    /**
     * Parses an integer value from a {@link String} encoded in decimal format.
     * <p>
     * This method does not check the passed {@link String} before parsing, so this method should only be used
     * on string which are certain to match the required format.
     * 
     * @param value
     *            a {@link String} containing (only) an integer encoded in
     *            decimal format
     * @return the parsed integer
     */
    private static final int parseDecimalConstantString(final String value) {
        return Integer.valueOf(value);
    }

    /**
     * Parses an integer value from a {@link String} encoded in hexadecimal
     * format.
     * <p>
     * This method does not check the passed {@link String} before parsing, so this method should only be used
     * on string which are certain to match the required format.
     * 
     * @param value
     *            a {@link String} containing (only) an integer encoded in
     *            hexadecimal format
     * @return the parsed integer
     */
    private static final int parseHexConstantString(final String value) {
        return Integer.parseInt(value.substring(2),// skip "0x" prefix
            16);
    }

    /**
     * Parses an integer value from a {@link String} encoded in Base64 format.
     * <p>
     * This method does not check the passed {@link String} before parsing, so this method should only be used
     * on string which are certain to match the required format.
     * 
     * @param value
     *            a {@link String} containing (only) an integer encoded in
     *            Base64 format
     * @return the parsed integer
     */
    private static final int parseBase64ConstantString(final String value) {
        final String s = value.substring(2);// crop "0b"/"0B" at the beginning
        final int length = s.length();
        int result = 0;
        for (int i = 0; i < length; ++i) {
            final char c = s.charAt(length - 1 - i);
            result += base64ValueOf(c) * Math.pow(64, i);
        }
        return result;
    }


    /**
     * Returns a character's value in Base64
     * 
     * @param c
     *            a character that is assigned a value from 0 to 63 in Base64
     * @throws NumberFormatException
     *             if the parameter is not a Base64 character
     * @return the character's value in Base64
     */
    private static final int base64ValueOf(final char c) {
        if ('A' <= c && c <= 'Z')
            return c - 'A';
        if ('a' <= c && c <= 'z')
            return c - 'a' + 26;
        if ('0' <= c && c <= '9')
            return c - '0' + 52;
        if (c == '+')
            return 62;
        if (c == '/')
            return 63;
        throw new NumberFormatException();
    }

    @Override
    public final String toString() {
        return "[" + value + "]";
    }

    @Override
    public boolean contains(Object value) {
        if (value == null)
            return false;
        if (value instanceof SingleNumericalValue)
            return contains(((SingleNumericalValue)value).getValue());
        if (value instanceof Integer)
            return contains((int)((Integer)value));
        return false;
    }

    @Override
    public boolean contains(final int value) {
        if (value == this.value)
            return true;
        return false;
    }
}

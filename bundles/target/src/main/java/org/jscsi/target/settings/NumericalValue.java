package org.jscsi.target.settings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An abstract parent class to {@link SingleNumericalValue} and {@link NumericalValueRange}. Objects of these
 * two classes represent single
 * integers and integer intervals, respectively, and can be parsed from any
 * properly formatted <i>key=value</i> pair <i>value</i>, be they formatted in
 * decimal, hexadecimal, or Base64 notation.
 * <p>
 * Instances of {@link NumericalValue} can be intersected, i.e. that by using either the
 * {@link #intersect(NumericalValue)} or the {@link #intersect(NumericalValue, NumericalValue)}, a
 * {@link NumericalValue} can be created which encompasses all integers that are part of the range of values
 * represented by the intersected {@link NumericalValue} objects.
 * <p>
 * For example, if the interval [1,10] is intersected with the interval [5,15], the result would be a
 * {@link NumericalValueRange} representing the interval [5,10]. Intersecting an interval with a single number
 * would return the number, but only if the number is part of the interval. Generally, both methods return
 * <code>null</code> if there is no overlap.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class NumericalValue {

    // *** single number regular expressions ***
    /**
     * Regular expression for decimal integers.
     */
    private static final String DECIMAL_REGEX = "0|[-]?[1-9][0-9]*";

    /**
     * Regular expression for hexadecimal integers.
     */
    private static final String HEX_REGEX = "0[x|X][0-9a-fA-F]+";

    /**
     * Regular expression for Base64 integers.
     */
    private static final String BASE_64_REGEX = "0[b|B][0-9a-zA-Z+/]+";
    private static final String SINGLE_CONSTANT_REGEX = DECIMAL_REGEX + "|" + HEX_REGEX + "|" + BASE_64_REGEX;

    // *** single number patterns ***
    /**
     * A precompiled pattern for matching decimal integer {@link String}.
     */
    public static final Pattern DECIMAL_CONSTANT_PATTERN = Pattern.compile(DECIMAL_REGEX);

    /**
     * A precompiled pattern for matching hexadecimal integer {@link String}.
     */
    public static final Pattern HEX_CONSTANT_PATTERN = Pattern.compile(HEX_REGEX);

    /**
     * A precompiled pattern for matching Base64 integer {@link String}.
     */
    public static final Pattern BASE_64_CONSTANT_PATTERN = Pattern.compile(BASE_64_REGEX);

    /**
     * A precompiled pattern for matching decimal, hexadecimal, and Base64
     * integer {@link String}.
     */
    public static final Pattern SINGLE_CONSTANT_PATTERN = Pattern.compile(SINGLE_CONSTANT_REGEX);

    /**
     * A precompiled pattern for matching decimal, hexadecimal, and Base64
     * integer interval {@link String} (two integers separated by '~').
     */
    public static final Pattern NUMERICAL_RANGE_PATTERN = Pattern.compile("(" + SINGLE_CONSTANT_REGEX + ")~("
        + SINGLE_CONSTANT_REGEX + ")");

    // *** methods ***

    /**
     * Parses a {@link NumericalValue} from a {@link String}.
     * 
     * @param value
     *            the {@link String} to parse.
     * @return a {@link NumericalValue} or <code>null</code> if the parameter
     *         does not match any of the supported patterns (specified by the
     *         iSCSI standard).
     */
    public static final NumericalValue parseNumericalValue(final String value) {
        // return SingleNumericalValue ...
        final Matcher singleValueMatcher = SINGLE_CONSTANT_PATTERN.matcher(value);
        if (singleValueMatcher.matches())
            return SingleNumericalValue.parseSingleNumericValue(value);
        // ... NumericalValueRange ...
        final Matcher rangeMatcher = NUMERICAL_RANGE_PATTERN.matcher(value);
        if (rangeMatcher.matches())
            return NumericalValueRange.parseNumericalValueRange(value);
        // ... or null
        return null;
    }

    /**
     * Returns a {@link NumericalValue} spanning the overlap of this {@link NumericalValue} and the parameter.
     * 
     * @param value
     *            the {@link NumericalValue} to be intersected with this object
     * @return a {@link NumericalValue} representing the intersection of this {@link NumericalValue} with the
     *         parameter
     */
    public NumericalValue intersect(final NumericalValue value) {
        return intersect(this, value);
    }

    /**
     * Returns a {@link NumericalValue} representing the intersection of the two
     * parameters
     * 
     * @param a
     *            the first {@link NumericalValue}
     * @param b
     *            the second {@link NumericalValue}
     * @return a {@link NumericalValue} representing the intersection of the two
     *         parameters
     */
    public static NumericalValue intersect(final NumericalValue a, final NumericalValue b) {
        // early exit
        if (a == null || b == null)
            return null;
        // get ranges of a and b
        int aMin, aMax, bMin, bMax;
        if (a instanceof SingleNumericalValue) {
            final SingleNumericalValue v = (SingleNumericalValue)a;
            aMin = v.getValue();
            aMax = aMin;
        } else {
            final NumericalValueRange r = (NumericalValueRange)a;
            aMin = r.getMin();
            aMax = r.getMax();
        }
        if (b instanceof SingleNumericalValue) {
            final SingleNumericalValue v = (SingleNumericalValue)b;
            bMin = v.getValue();
            bMax = bMin;
        } else {
            final NumericalValueRange r = (NumericalValueRange)b;
            bMin = r.getMin();
            bMax = r.getMax();
        }
        // intersect ranges
        final int min = Math.max(aMin, bMin);
        final int max = Math.min(aMax, bMax);
        if (min == max)
            return SingleNumericalValue.create(min);
        else
            return NumericalValueRange.create(min, max);
    }

    /**
     * Returns true if the passed {@link Integer} or {@link NumericalValue} lies
     * completely inside the interval represented by this {@link NumericalValue} . If the parameter is not an
     * {@link Integer} or a {@link NumericalValue},
     * the method will return <code>false</code>.
     * 
     * @param value
     *            the {@link Integer} or {@link NumericalValue} to check
     * @return <code>true</code> if the value is complete contained, <code>false</code> if it is not
     */
    public abstract boolean contains(Object value);

    /**
     * Returns true if the passed integer lies completely inside the interval
     * represented by this {@link NumericalValue}.
     * 
     * @param value
     *            the integer to check
     * @return <code>true</code> if the value is complete contained, <code>false</code> if it is not
     */
    public abstract boolean contains(int value);
}

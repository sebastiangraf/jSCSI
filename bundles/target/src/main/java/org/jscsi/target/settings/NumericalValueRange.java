package org.jscsi.target.settings;

import java.util.regex.Matcher;

/**
 * This is a {@link NumericalValue} sub-class for representing integer
 * intervals. Each {@link NumericalValueRange} is defined by two integers - {@link #min} and {@link #max} -
 * the lower and the upper bound of the
 * represented interval.
 * 
 * @author Andreas Ergenzinger
 */
public final class NumericalValueRange extends NumericalValue {

    /**
     * The lower boundary of the represented interval.
     */
    private final int min;

    /**
     * The upper boundary of the represented interval.
     */
    private final int max;

    /**
     * The build method for creating {@link NumericalValueRange} objects. The
     * specified <i>min</i> and <i>max</i> parameters are going to be used for
     * initializing the {@link #min} and {@link #max} variables of the new {@link NumericalValueRange}. Since
     * this only makes sense if <i>min</i>
     * &le; <i>max</i>, <code>null</code> will be returned if this requirement
     * is violated.
     * 
     * @param min
     *            the lower bound
     * @param max
     *            the upper bound
     * @return a {@link NumericalValueRange} representing the specified interval
     *         or <code>null</code>
     */
    public static final NumericalValueRange create(final int min, final int max) {
        if (min > max)
            return null;
        return new NumericalValueRange(min, max);
    }

    /**
     * Parses a {@link NumericalValueRange} from a {@link String}. The lower and
     * boundaries must be separated by a '~' character and the the leading
     * integer must not be larger than the trailing one. If these format
     * requirements are violated the method will return <code>null</code>.
     * 
     * @param value
     *            the {@link String} to parse
     * @return a {@link NumericalValueRange} representing the interval in the
     *         parsed {@link String} or <code>null</code>
     */
    public static final NumericalValueRange parseNumericalValueRange(final String value) {
        // check formatting
        final Matcher rangeMatcher = NUMERICAL_RANGE_PATTERN.matcher(value);
        if (!rangeMatcher.matches()) {
            return null;
        }
        int min, max;

        // split parameter at '~' sign and parse boundaries individually
        String[] numbers = value.split("~");
        final SingleNumericalValue minVal = SingleNumericalValue.parseSingleNumericValue(numbers[0]);
        final SingleNumericalValue maxVal = SingleNumericalValue.parseSingleNumericValue(numbers[1]);
        if (minVal == null || maxVal == null)
            return null;// not possible, format checked by rangeMatcher
        min = minVal.getValue();
        max = maxVal.getValue();

        // return with sanity check, enforce min <= max
        return create(min, max);
    }

    /**
     * The private constructor for {@link NumericalValueRange} objects.
     * <p>
     * This method has a limited visibility to make sure that {@link #min} &le; {@link #max} is always true,
     * by forcing users to rely on {@link #create(int, int)}.
     * 
     * @param min
     *            the lower boundary of the specified interval
     * @param max
     *            the upper boundary of the specified interval
     */
    private NumericalValueRange(final int min, final int max) {
        if (min <= max) {
            this.min = min;
            this.max = max;
        } else {
            this.min = max;
            this.max = min;
        }
    }

    /**
     * Returns the lower boundary.
     * 
     * @return the lower boundary
     */
    public final int getMin() {
        return min;
    }

    /**
     * Returns the upper boundary.
     * 
     * @return the upper boundary
     */
    public final int getMax() {
        return max;
    }

    @Override
    public final String toString() {
        return "[" + min + "," + max + "]";
    }

    @Override
    public boolean contains(final Object value) {
        if (value instanceof NumericalValue)
            return contains((NumericalValue)value);
        if (value instanceof Integer)
            return contains((int)((Integer)value));
        return false;
    }

    /**
     * Returns true if the passed {@link NumericalValue} lies completely inside
     * the interval represented by this {@link NumericalValueRange}.
     * 
     * @param value
     *            the {@link NumericalValue} to check
     * @return <code>true</code> if the value is complete contained, <code>false</code> if it is not
     */
    public boolean contains(final NumericalValue value) {
        if (value instanceof SingleNumericalValue)
            return contains((SingleNumericalValue)value);
        if (value instanceof NumericalValueRange)
            return contains((NumericalValueRange)value);
        return false;
    }

    /**
     * Returns true if the passed {@link NumericalValueRange} lies completely
     * inside the interval represented by this {@link NumericalValueRange}.
     * 
     * @param range
     *            the {@link NumericalValueRange} to check
     * @return <code>true</code> if the value is complete contained, <code>false</code> if it is not
     */
    public boolean contains(final NumericalValueRange range) {
        if (range == null)
            return false;
        if (min <= range.getMin() && range.getMax() <= max)
            return true;
        return false;
    }

    /**
     * Returns true if the passed {@link SingleNumericalValue} lies completely
     * inside the interval represented by this {@link NumericalValueRange}.
     * 
     * @param value
     *            the {@link SingleNumericalValue} to check
     * @return <code>true</code> if the value is complete contained, <code>false</code> if it is not
     */
    public boolean contains(final SingleNumericalValue value) {
        if (value == null)
            return false;
        return contains(value.getValue());
    }

    @Override
    public boolean contains(final int value) {
        if (min <= value && value <= max)
            return true;
        return false;
    }
}

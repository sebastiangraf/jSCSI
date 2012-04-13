package org.jscsi.target.settings;

import org.jscsi.target.settings.entry.Entry;

/**
 * {@link KeySet} objects are used by {@link Entry} instances for storing all
 * <i>keys</i> that might be used to negotiate the parameter managed by the {@link Entry}. Individual
 * <i>keys</i> are represented as {@link String} objects.
 * <p>
 * Although most parameters are associated with just one <i>key</i>, there are some exceptions, hence
 * justifying the added complexity of using a dedicated class for managing <i>keys</i> in favor of simply
 * using multiple {@link String} objects.
 * 
 * @author Andreas Ergenzinger
 */
public final class KeySet {

    /**
     * An array containing all <i>keys</i> which are part of the set.
     */
    final String[] values;

    /**
     * Constructor for creating {@link KeySet} instances with just one
     * <i>key</i>.
     * 
     * @param value
     *            the <i>key</i>
     */
    public KeySet(final String value) {
        values = new String[1];
        values[0] = value;
    }

    /**
     * Constructor for creating {@link KeySet} instances with multiple
     * <i>keys</i>.
     * 
     * @param firstValue
     *            the first <i>key</i>
     * @param additionalValues
     *            additional <i>keys</i>
     */
    public KeySet(final String firstValue, final String... additionalValues) {
        values = new String[additionalValues.length + 1];
        values[0] = firstValue;
        for (int i = 1; i < values.length; ++i)
            values[i] = additionalValues[i - 1];
    }

    /**
     * Returns <code>true</code> if one of the <i>keys</i> in the set equals the
     * parameter.
     * 
     * @param key
     *            the <i>key</i> to compare to the set's <i>keys</i>
     * @return <code>true</code> if there is a match, <code>false</code> if
     *         there is not
     */
    public boolean matchKey(final String key) {
        if (key == null)
            return false;
        for (int i = 0; i < values.length; ++i)
            if (values[i].equals(key))
                return true;
        return false;
    }

    /**
     * Returns the primary <i>key</i>, stored at the first position in {@link #values}.
     * 
     * @return the primary <i>key</i>
     */
    public String getValue() {
        return values[0];
    }

    /**
     * Returns the <i>key</i> stored at the specified position in {@link #values}.
     * 
     * @param index
     *            the position of the requested <i>key</i> in {@link #values}
     * @return the <i>key</i> stored at the specified position in {@link #values}
     */
    public String getValue(final int index) {
        return values[index];
    }

    /**
     * Returns the number of <i>keys</i> in this set.
     * 
     * @return the number of <i>keys</i> in this set
     */
    public int size() {
        return values.length;
    }

    @Override
    public String toString() {
        return java.util.Arrays.toString(values);
    }
}

package org.jscsi.target.settings;

import org.jscsi.target.settings.entry.NumericalEntry;

/**
 * A result function used by the {@link NumericalEntry} class to determine the
 * outcome of integer parameter negotiations.
 * <p>
 * The behavior of this enumeration is described in the {@link #getResult(int, int)} method.
 * 
 * @author Andreas Ergenzinger
 */
public enum NumericalResultFunction {
    /**
     * The {@link #getResult(int, int)} method will return the smaller one of
     * the two parameters.
     */
    MIN,
    /**
     * The {@link #getResult(int, int)} method will return the larger one of the
     * two parameters.
     */
    MAX;

    /**
     * Returns either the value of the first or the second parameter, depending
     * of the value of this enumeration.
     * 
     * @param a
     *            the first value
     * @param b
     *            the second value
     * @return either <i>a</i> or <i>b</i>
     */
    public int getResult(final int a, final int b) {
        if (this == MIN)
            return Math.min(a, b);
        return Math.max(a, b);
    }
}

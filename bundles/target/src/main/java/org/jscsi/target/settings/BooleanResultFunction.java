package org.jscsi.target.settings;

import org.jscsi.target.settings.entry.BooleanEntry;

/**
 * {@link BooleanResultFunction}s are used by instances of{@link BooleanEntry} during the negotiation of
 * boolean parameters.
 * <p>
 * They determine a negotiation outcome based on a logical <code>AND</code> or <code>OR</code> operation.
 * 
 * @author Andreas Ergenzinger
 */
public enum BooleanResultFunction {
    /**
     * The negotiation result will be <code>true</code> only if both initiator
     * and target support that value.
     */
    AND,
    /**
     * The negotiation result will be <code>true</code> only if either the
     * initiator or the target support that value.
     */
    OR;

    /**
     * Performs a logical <code>AND</code> or <code>OR</code> operation on the
     * two parameters and returns the result. The type of the operation depends
     * on the value of this {@link BooleanResultFunction}.
     * 
     * @param a
     *            the first boolean value
     * @param b
     *            the second boolean value
     * @return the result of a logical <code>AND</code> or <code>OR</code> operation.
     */
    public final boolean getResult(final boolean a, final boolean b) {
        if (this == AND)
            return a && b;
        return a || b;
    }
}

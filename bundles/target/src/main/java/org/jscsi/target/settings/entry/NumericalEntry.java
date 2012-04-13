package org.jscsi.target.settings.entry;

import org.jscsi.target.TargetServer;
import org.jscsi.target.settings.KeySet;
import org.jscsi.target.settings.NegotiationStatus;
import org.jscsi.target.settings.NegotiationType;
import org.jscsi.target.settings.NumericalResultFunction;
import org.jscsi.target.settings.NumericalValueRange;
import org.jscsi.target.settings.SingleNumericalValue;

/**
 * An {@link Entry} sub-class for boolean parameters.
 * <p>
 * During text parameter negotiation, numerical parameters are negotiated by sending a single integer
 * <i>value</i> as part of a <i>key=value</i> pair. Based on the values permitted for the specific <i>key</i>
 * ( {@link #protocolValueRange}), the {@link #resultFunction} and the {@link #negotiationValue}, the jSCSI
 * Target will choose a single value as the negotiation outcome.
 * <p>
 * For declared parameters this process is less complex, any proposed value from the
 * {@link #protocolValueRange} will be silently accepted.
 * <p>
 * Please note the difference between numerical values and numerical range values (see
 * {@link NumericalRangeEntry}).
 * <p>
 * The default or negotiated value can be accessed via the {@link #getIntegerValue()} method.
 * 
 * @see Entry
 * @author Andreas Ergenzinger
 */
public final class NumericalEntry extends Entry {

    /**
     * The integer interval describing the range of legal values.
     */
    private final NumericalValueRange protocolValueRange;

    /**
     * An integer serving as a boundary to the values the jSCSI Target is
     * willing to accept.
     */
    private final int negotiationValue;

    /**
     * The {@link NumericalResultFunction} used for negotiating the parameter.
     */
    private final NumericalResultFunction resultFunction;

    /**
     * <code>true</code> means that a {@link #negotiationValue} of <code>0</code> allows the jSCSI Target to
     * choose any value from the
     * resulting range. <code>false</code> means that {@link #negotiationValue} must always be treated as an
     * upper or lower boundary.
     */
    private final boolean zeroMeansDontCare;

    /**
     * The {@link NumericalEntry} constructor.
     * 
     * @param keySet
     *            contains all relevant keys
     * @param negotiationType
     *            declared or negotiated
     * @param use
     *            determines under which circumstances the parameter may be
     *            negotiated
     * @param negotiationStatus
     *            indicates whether there is a default value or if the parameter
     *            must be negotiated
     * @param negotiationValue
     *            together with the <i>resultFunction</i> and the
     *            <i>protocolValueRange</i> parameters this value indicates the
     *            values supported by the jSCSI Target
     * @param protocolValueRange
     *            specifying the range of legal values
     * @param resultFunction
     *            determines the negotiation outcome
     * @param defaultValue
     *            the default value or <code>null</code>
     * @param zeroMeansDontCare
     *            if <code>true</code> and <i>negotiationValue</i> equals <code>0</code> then
     *            <i>negotiationValue</i> does not serve as
     *            an upper or lower boundary to the values the jSCSI Target will
     *            accept
     */
    public NumericalEntry(final KeySet keySet, final NegotiationType negotiationType, final Use use,
        final NegotiationStatus negotiationStatus, final int negotiationValue,
        final NumericalValueRange protocolValueRange, final NumericalResultFunction resultFunction,
        final Integer defaultValue, final boolean zeroMeansDontCare) {
        super(keySet, negotiationType, use, negotiationStatus, defaultValue);
        this.protocolValueRange = protocolValueRange;
        this.negotiationValue = negotiationValue;
        this.resultFunction = resultFunction;
        this.zeroMeansDontCare = zeroMeansDontCare;
    }

    @Override
    protected boolean inProtocolValueRange(final Object values) {
        // receives an Integer
        final int val = (Integer)values;
        if (zeroMeansDontCare && val == 0)// val might not lie inside reg.
                                          // interval
            return true;
        return protocolValueRange.contains(val);
    }

    @Override
    protected Object parseOffer(final TargetServer target, final String values) {
        // return an Integer
        return SingleNumericalValue.parseSingleNumericValue(values).getValue();
    }

    @Override
    protected void processDeclaration(final Object values) {
        // receives an Integer
        final int val = (Integer)values;
        if (zeroMeansDontCare && val == 0)// pick value desired by target
            value = negotiationValue;
        else
            value = values;
    }

    @Override
    protected String processNegotiation(final Object values) {
        // receives an Integer
        final int val = (Integer)values;
        if (zeroMeansDontCare && val == 0)// pick value desired by target
            value = negotiationValue;
        else
            // pick value based on result function and offer
            value = resultFunction.getResult(negotiationValue, val);
        return value.toString();
    }

    @Override
    public Integer getIntegerValue() {
        return (Integer)value;
    }

    @Override
    public Entry copy() {
        final NumericalEntry e =
            new NumericalEntry(keySet, negotiationType, use, negotiationStatus, negotiationValue,
                protocolValueRange, resultFunction, (Integer)value, zeroMeansDontCare);
        e.alreadyNegotiated = this.alreadyNegotiated;
        return e;
    }
}

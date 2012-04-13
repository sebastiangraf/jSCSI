package org.jscsi.target.settings.entry;

import org.jscsi.target.TargetServer;
import org.jscsi.target.settings.BooleanResultFunction;
import org.jscsi.target.settings.KeySet;
import org.jscsi.target.settings.NegotiationStatus;
import org.jscsi.target.settings.NegotiationType;
import org.jscsi.target.settings.TextParameter;

/**
 * An {@link Entry} sub-class for boolean parameters.
 * <p>
 * During text parameter negotiation, boolean values are encoded as <code>Yes</code> and <code>No</code>,
 * meaning <code>true</code> and <code>false</code>, respectively.
 * <p>
 * The default or negotiated value can be accessed via the {@link #getBooleanValue()} method.
 * 
 * @see Entry
 * @author Andreas Ergenzinger
 */
public final class BooleanEntry extends Entry {

    private final boolean negotiationValue;
    private final BooleanResultFunction resultFunction;

    /**
     * The {@link BooleanEntry} constructor.
     * 
     * @param keySet
     *            contains all relevant keys
     * @param use
     *            determines under which circumstances the parameter may be
     *            negotiated
     * @param negotiationStatus
     *            indicates whether there is a default value or if the parameter
     *            must be negotiated
     * @param negotiationValue
     *            together with the <i>resultFunction</i> parameter this value
     *            indicates the values supported by the jSCSI Target
     * @param resultFunction
     *            the result function to use during negotiations
     * @param defaultValue
     *            the default value or <code>null</code>
     */
    public BooleanEntry(final KeySet keySet, final Use use, final NegotiationStatus negotiationStatus,
        final boolean negotiationValue, final BooleanResultFunction resultFunction, final Boolean defaultValue) {
        super(keySet, NegotiationType.NEGOTIATED, use, negotiationStatus, defaultValue);
        this.negotiationValue = negotiationValue;
        this.resultFunction = resultFunction;
    }

    @Override
    protected Object parseOffer(TargetServer target, final String values) {
        return TextParameter.parseBooleanValue(values);
    }

    @Override
    protected boolean inProtocolValueRange(final Object values) {
        if (values instanceof Boolean)
            return true;
        return false;// should never happen
    }

    @Override
    protected void processDeclaration(final Object values) {
        // there are no declarations, see constructor
    }

    @Override
    protected String processNegotiation(final Object values) {
        final boolean request = (Boolean)values;
        final boolean result = resultFunction.getResult(request, negotiationValue);
        value = result;
        return TextParameter.booleanToTextValue(result);
    }

    @Override
    public Boolean getBooleanValue() {
        return (Boolean)value;
    }

    @Override
    public Entry copy() {
        final BooleanEntry e =
            new BooleanEntry(keySet, use, negotiationStatus, negotiationValue, resultFunction, (Boolean)value);
        e.alreadyNegotiated = this.alreadyNegotiated;
        return e;
    }

}

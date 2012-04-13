package org.jscsi.target.settings.entry;

import org.jscsi.target.TargetServer;
import org.jscsi.target.settings.KeySet;
import org.jscsi.target.settings.NegotiationStatus;
import org.jscsi.target.settings.NegotiationType;
import org.jscsi.target.settings.TextParameter;

/**
 * This {@link Entry} sub-class is used for managing and negotiation String
 * parameters. The <i>value</i> part of the corresponding <i>key-value</i> pairs
 * can be made up of one or several comma-separated strings.
 * <p>
 * The default or negotiated value can be accessed via the {@link #getStringValue()} method.
 * 
 * @see Entry
 * @author Andreas Ergenzinger
 */
public final class StringEntry extends Entry {

    /**
     * Contains all values supported by the jSCSI Target.
     */
    private final String[] supportedValues;

    /**
     * The {@link StringEntry} constructor.
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
     * @param supportedValues
     *            all values supported by the jSCSI Target
     * @param defaultValue
     *            the default value or <code>null</code>
     */
    public StringEntry(final KeySet keySet, final NegotiationType negotiationType, final Use use,
        final NegotiationStatus negotiationStatus, final String[] supportedValues, final Object defaultValue) {
        super(keySet, negotiationType, use, negotiationStatus, defaultValue);
        this.supportedValues = supportedValues;
    }

    @Override
    protected boolean inProtocolValueRange(Object values) {
        // receives a String array
        // (size of array has already been checked in parseOffer(...))
        final String[] vals = (String[])values;
        // check for illegal characters
        for (String s : vals)
            if (!TextParameter.checkTextValueFormat(s))
                return false;
        return true;
    }

    @Override
    protected Object parseOffer(TargetServer target, String values) {

        // receives a comma-separated list of string values (or a single value)
        // enforce that declaration consists of exactly one value
        final String[] split = TextParameter.splitValues(values);
        if (negotiationType == NegotiationType.DECLARED && split.length > 1)
            return null;// protocol error
        return split;
    }

    @Override
    protected void processDeclaration(Object values) {
        // receives a String array or length 1
        value = ((String[])values)[0];
    }

    @Override
    protected String processNegotiation(Object values) {
        // receives a String array
        final String[] requestedValues = (String[])values;
        final String[] commonValues = TextParameter.intersect(supportedValues, requestedValues);
        // reject if no commonly supported values
        if (commonValues.length == 0)
            return null;
        // otherwise, save and return negotiation result
        value = commonValues[0];
        return commonValues[0];
    }

    @Override
    public String getStringValue() {
        return (String)value;
    }

    @Override
    public Entry copy() {
        final StringEntry e =
            new StringEntry(keySet, negotiationType, use, negotiationStatus, supportedValues, (String)value);
        e.alreadyNegotiated = this.alreadyNegotiated;
        return e;
    }
}

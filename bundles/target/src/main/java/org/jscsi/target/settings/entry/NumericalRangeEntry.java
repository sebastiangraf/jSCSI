package org.jscsi.target.settings.entry;

import org.jscsi.target.TargetServer;
import org.jscsi.target.settings.KeySet;
import org.jscsi.target.settings.NegotiationStatus;
import org.jscsi.target.settings.NegotiationType;
import org.jscsi.target.settings.NumericalValueRange;
import org.jscsi.target.settings.SingleNumericalValue;

/**
 * An {@link Entry} sub-class used for negotiating integer parameters which
 * require the iSCSI initiator to offer an interval from which the target must
 * choose the final value. Thereby the <i>key=value</i> format differs from that
 * required by the {@link NumericalEntry} class, for which <i>value</i> is a
 * single integer. The <i>value</i> part expected by this class looks like <code>1024~2048</code>, however the
 * response <i>value</i> will again be a
 * single integer from the given range.
 * <p>
 * Currently this {@link Entry} sub-class is only used for two parameters - <code>IFMarkInt</code> and
 * <code>OFMarkInt</code>. However, since the jSCSI Target does not support stream markers (
 * <code>IFMarker=No</code> and <code>OFMarker=No</code>), the correct response value to
 * <code>IFMarkInt</code> and <code>OFMarkInt</code> <i>keys</i> must be <code>Irrelevant</code>. This
 * behavior is part of the
 * {@link Entry#negotiate(org.jscsi.target.TargetServer, org.jscsi.parser.login.LoginStage, boolean, boolean, String, String, java.util.Collection)}
 * method, which will lead to correct responses without having to check for additional constraints (the
 * presence and <i>value</i> of <code>IFMarker</code> and <code>OFMarker</code> <i>key=value</i> pairs.
 * 
 * @author Andreas Ergenzinger
 */
public final class NumericalRangeEntry extends Entry {

    /**
     * A {@link NumericalValueRange} specifying the boundaries into which any
     * RFC-conform <i>value</i> range proposed by the iSCSI initiator must fall.
     */
    private final NumericalValueRange protocolValueRange;

    /**
     * This value will be the negotiation result, if it falls into the
     * <i>value</i> range proposed by the iSCSI initiator.
     */
    private final int negotiationValue;

    /**
     * The {@link NumericalRangeEntry} constructor.
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
     *            the value the jSCSI Target would like to use
     * @param protocolValueRange
     *            specifies the boundaries into which any RFC-conform
     *            <i>value</i> range proposed by the iSCSI initiator must fall
     * @param defaultValue
     *            the default value or <code>null</code>
     */
    public NumericalRangeEntry(final KeySet keySet, final Use use, final NegotiationStatus negotiationStatus,
        final int negotiationValue, final NumericalValueRange protocolValueRange, final Object defaultValue) {
        super(keySet, NegotiationType.NEGOTIATED, use, negotiationStatus, defaultValue);
        this.negotiationValue = negotiationValue;
        this.protocolValueRange = protocolValueRange;
    }

    @Override
    protected boolean inProtocolValueRange(Object values) {
        // receives a NumericalValueRange
        return protocolValueRange.contains((NumericalValueRange)values);
    }

    @Override
    protected Object parseOffer(TargetServer target, String values) {
        // expected format: "1234~5678"

        NumericalValueRange range = NumericalValueRange.parseNumericalValueRange(values);
        if (range == null && target.getConfig().getAllowSloppyNegotiation()) {
            /*
             * The format was violated.
             * 
             * The jSCSI Initiator sends "IFMarkInt=2048" and "OFMarkInt=2048",
             * not ranges.
             * 
             * If values is at least a number we will fix this.
             */
            final SingleNumericalValue singleValue = SingleNumericalValue.parseSingleNumericValue(values);
            if (singleValue != null) {
                range = NumericalValueRange.create(singleValue.getValue(),// min
                    singleValue.getValue());// max
            }
        }

        return range;
    }

    @Override
    protected void processDeclaration(Object values) {
        // there are no declarations, see constructor
    }

    @Override
    protected String processNegotiation(Object values) {
        // receives a NumericalValueRange
        final NumericalValueRange range = (NumericalValueRange)values;
        // accept if negotiatedValue in initiator offer, else reject
        if (range.contains(negotiationValue)) {
            value = negotiationValue;
            return value.toString();
        } // else
        value = null;
        return null;
    }

    @Override
    public Integer getIntegerValue() {
        return (Integer)value;
    }

    @Override
    public Entry copy() {
        final NumericalRangeEntry e =
            new NumericalRangeEntry(keySet, use, negotiationStatus, negotiationValue, protocolValueRange,
                (Integer)value);
        e.alreadyNegotiated = this.alreadyNegotiated;
        return e;
    }
}

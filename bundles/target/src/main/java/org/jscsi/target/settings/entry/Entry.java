package org.jscsi.target.settings.entry;

import java.util.Collection;

import javax.naming.OperationNotSupportedException;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.login.LoginStage;
import org.jscsi.target.TargetServer;
import org.jscsi.target.settings.KeySet;
import org.jscsi.target.settings.NegotiationStatus;
import org.jscsi.target.settings.NegotiationType;
import org.jscsi.target.settings.SettingsNegotiator;
import org.jscsi.target.settings.TextKeyword;
import org.jscsi.target.settings.TextParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Entry} objects are used by instances {@link SettingsNegotiator} during
 * text negotiation of connection and session parameter. For all parameters that
 * are either declared by the iSCSI initiator or negotiated between the
 * initiator and the target a separate {@link Entry} takes care of processing
 * the respective <i>key=value</i> pair and returning the negotiated value, if
 * appropriate.
 * <p>
 * For brevity, the term "negotiated" will be used in the following in a way that can either mean
 * "declared or negotiated", unless the distinction is evident by context.
 * 
 * @author Andreas Ergenzinger, University of Konstanz
 */
public abstract class Entry {

    private static final Logger LOGGER = LoggerFactory.getLogger(Entry.class);

    /**
     * A {@link KeySet} containing all keys that can be used for negotiating
     * this {@link Entry}'s value.
     */
    protected final KeySet keySet;

    /**
     * Specifies if the {@link Entry}'s parameter is declared or negotiated.
     */
    protected final NegotiationType negotiationType;

    /**
     * Determines during which stages this {@link Entry}'s parameters may be
     * negotiated.
     */
    protected final Use use;

    /**
     * This variable specifies the progress and necessity of negotiating the
     * parameter managed by this {@link Entry}.
     */
    protected NegotiationStatus negotiationStatus;

    /**
     * The currently valid value or <code>null</code>.
     */
    protected Object value;

    /**
     * This variable is used to detect illegal attempts to renegotiate a
     * previously negotiated or declared text parameter.
     * <p>
     * This variable will be set back to <code>false</code> after each negotiation task (login phase, or text
     * parameter negotiation stage). Renegotiation accross stages/tasks can be prevented by initializing the
     * {@link #use} variable accordingly.
     * 
     * @see #resetAlreadyNegotiated()
     */
    protected boolean alreadyNegotiated = false;

    /**
     * Abstract constructor.
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
     * @param defaultValue
     *            the default value or <code>null</code>
     */
    public Entry(final KeySet keySet, final NegotiationType negotiationType, final Use use,
        final NegotiationStatus negotiationStatus, Object defaultValue) {
        this.keySet = keySet;
        this.negotiationType = negotiationType;
        this.use = use;
        this.negotiationStatus = negotiationStatus;
        this.value = defaultValue;
    }

    /**
     * Logs an error message containing all {@link #keySet} keys as well as the
     * passed {@link String} parameter and indicates an unsuccessful negotiation
     * by setting {@link #negotiationStatus} to {@link NegotiationStatus#REJECTED}.
     * 
     * @param logMessage
     */
    private void fail(final String logMessage) {
        LOGGER.error("negotiation error " + keySet + ": " + logMessage);
        negotiationStatus = NegotiationStatus.REJECTED;
    }

    /**
     * Parses the passed {@link String} parameter and returns a
     * sub-class-specific {@link Object} which represents the the specified
     * <i>value</i> part a <i>key=value</i> pair.
     * 
     * @param values
     *            the <i>value</i> part of a <i>key=value</i> pair
     * @return sub-class-specific {@link Object} or <code>null</code> if the
     *         parameter violated the expected format
     */
    protected abstract Object parseOffer(TargetServer target, String values);

    /**
     * This method is used for negotiating or declaring the {@link Entry}'s
     * parameter.
     * 
     * @param loginStage
     *            specifying the current stage or phase of the connection whose
     *            parameters are to be negotiated
     * @param leadingConnection
     *            <code>true</code> if the connection is the first connection in
     *            its session, <code>false</code> if not
     * @param initialPdu
     *            <code>true</code> if the <i>key=value</i> pair parameters have
     *            been sent in the first login {@link ProtocolDataUnit} from the
     *            initiator, <code>false</code> if thy have not
     * @param key
     *            the <i>key</i> part from the received <i>key=value</i> pair
     * @param values
     *            the <i>value</i> part from the received <i>key=value</i> pair
     * @param responseKeyValuePairs
     *            where the reply <i>key=value</i> pair will be added to if
     *            necessary
     * @return <code>true</code> if everything went fine, <code>false</code> if
     *         errors occured
     */
    public final boolean negotiate(TargetServer target, final LoginStage loginStage,
        final boolean leadingConnection, final boolean initialPdu, final String key, final String values,
        final Collection<String> responseKeyValuePairs) {

        // (re)check key (just in case), this should have been checked before
        // calling this method
        if (!matchKey(key)) {
            fail("\"" + key + "\" does not match key in" + keySet);
            return false;
        }

        // prevent renegotiation and remember this negotiation
        if (alreadyNegotiated) {
            fail("illegal renegotiation");
            return false;
        }
        alreadyNegotiated = true;

        // check use code
        if (!use.checkUse(loginStage, leadingConnection, initialPdu)) {
            fail("wrong use: " + use + ", " + loginStage + ", " + leadingConnection + ", " + initialPdu);
            return false;
        }

        // transform values to appropriate type
        final Object offer = parseOffer(target, values);

        if (offer == null) {
            fail("value format error: " + values);
            return false;
        }

        // check if values are in the protocol-conform range/set of values
        if (!inProtocolValueRange(offer)) {
            fail("illegal values offered: " + values);
            return false;
        }

        // *** declare ***
        if (negotiationType == NegotiationType.DECLARED) {
            // save received value ...
            processDeclaration(offer);
            // ... and accept silently
            negotiationStatus = NegotiationStatus.ACCEPTED;
            return true;
        }

        // *** negotiate ***
        if (negotiationType == NegotiationType.NEGOTIATED) {

            String negotiatedValue;// will be returned as value part

            if (negotiationStatus == NegotiationStatus.IRRELEVANT)
                negotiatedValue = TextKeyword.IRRELEVANT;
            else
                negotiatedValue = processNegotiation(offer);

            String reply;
            // reply, remember outcome, log, and return
            if (negotiatedValue == null) {// no commonly supported values
                reply = TextParameter.toKeyValuePair(key, TextKeyword.REJECT);
                responseKeyValuePairs.add(reply);
                fail("rejected value(s): " + values);
                return false;
            }// else
            reply = TextParameter.toKeyValuePair(key, negotiatedValue);
            responseKeyValuePairs.add(reply);
            return true;
        }

        // we should not be here
        fail("initialization error: negotiationType == null");
        return false;
    }

    /**
     * Sets {@link #alreadyNegotiated} back to <code>false</code>.
     * <p>
     * This method must be used at the end of each negotiation task, i.e. at the end of the login phase and
     * the FFP text negotiation stage.
     */
    public void resetAlreadyNegotiated() {
        alreadyNegotiated = false;
    }

    /**
     * Returns the negotiated (or default) value as a {@link Boolean}.
     * 
     * @return the negotiated (or default) value as a {@link Boolean}
     * @throws OperationNotSupportedException
     *             if {@link #value} is not of the boolean type
     */
    public Boolean getBooleanValue() throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    /**
     * Returns the negotiated (or default) value as an {@link Integer}.
     * 
     * @return the negotiated (or default) value as an {@link Integer}
     * @throws OperationNotSupportedException
     *             if {@link #value} is not of the integer type
     */
    public Integer getIntegerValue() throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    /**
     * Returns the negotiated (or default) value as a {@link String}.
     * 
     * @return the negotiated (or default) value as a {@link String}
     * @throws OperationNotSupportedException
     *             if {@link #value} is not a {@link String}
     */
    public String getStringValue() throws OperationNotSupportedException {
        throw new OperationNotSupportedException();
    }

    /**
     * Returns <code>true</code> if one of the keys of {@link #keySet} equals
     * the parameter and <code>false</code> if there is not match.
     * 
     * @param key
     *            the key to compare to the {@link #keySet} keys
     * @return <code>true</code> if one of the keys of {@link #keySet} equals
     *         the parameter and <code>false</code> if not
     */
    public final boolean matchKey(final String key) {
        return keySet.matchKey(key);
    }

    /**
     * This method is used for checking if a sub-class-specific {@link Object},
     * representing a single, a range, or a list of values sent by the
     * initiator, is illegal, according to the iSCSI standard.
     * 
     * @param values
     *            a sub-class-specific {@link Object}, representing a single, a
     *            range, or a list of values sent by the initiator
     * @return <code>false</code> if the iSCSI standard has been violated, <code>true</code> if not
     */
    protected abstract boolean inProtocolValueRange(Object values);

    /**
     * Receives a sub-class-specific {@link Object}, representing a legal
     * parameter value declared by the initiator and accepts it as the new {@link #value}.
     * 
     * @param values
     *            sub-class-specific representation of a single <i>value</i>
     *            declared by the initiator
     */
    protected abstract void processDeclaration(Object values);

    // returns null if reply is to be key=Reject
    /**
     * Receives a sub-class-specific {@link Object}, representing a list, a
     * range, or a single legal parameter value offered by the initiator and
     * tries to select a value from that offer. If none of the offered values is
     * supported by the jSCSI Target, <code>null</code> is returned, otherwise
     * the selection is accepted as the new {@link #value} and returned as a {@link String}. {@link #value}.
     * 
     * @param values
     *            a sub-class-specific {@link Object}, representing a list, a
     *            range, or a single legal parameter value offered by the
     *            initiator
     * @return the final, negotiated value or <code>null</code>, if the
     *         initiator's offer does not overlap with the values supported by
     *         the jSCSI Target
     */
    protected abstract String processNegotiation(Object values);

    /**
     * Returns {@link #negotiationStatus}.
     * 
     * @return {@link #negotiationStatus}
     */
    public final NegotiationStatus getNegotiationStatus() {
        return negotiationStatus;
    }

    /**
     * Returns an exact copy of this {@link Entry}.
     * 
     * @return a copy of this {@link Entry}.
     */
    public abstract Entry copy();

    /**
     * Returns <code>true</code> if {@link #negotiationStatus} is {@link NegotiationStatus#ACCEPTED} and
     * <code>false</code> if it is not.
     * 
     * @return <code>true</code> if {@link #negotiationStatus} is {@link NegotiationStatus#ACCEPTED},
     *         <code>false</code> if not
     */
    public boolean checkAccepted() {
        return negotiationStatus == NegotiationStatus.ACCEPTED;
    }
}

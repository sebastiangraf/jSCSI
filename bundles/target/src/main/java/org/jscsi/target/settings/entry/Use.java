package org.jscsi.target.settings.entry;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.login.LoginStage;
import org.jscsi.target.connection.stage.fullfeature.TextNegotiationStage;
import org.jscsi.target.connection.stage.login.LoginOperationalParameterNegotiationStage;

/**
 * Instances of this class are used by {@link Entry} objects to determine during
 * which circumstances a parameter may be negotiated or declared.
 * <p>
 * The factors to consider are:
 * <ul>
 * <li>the current stage of the connection</li>
 * <li>whether that connection is the leading connection, i.e. the initial connection in the enclosing
 * session.</li>
 * <li>if the <i>key-value</i> pair relating to the parameter was part of the first {@link ProtocolDataUnit}
 * sent over the connection</i>
 * </ul>
 * 
 * @author Andreas Ergenzinger
 */
public enum Use {
    /**
     * The parameter may be negotiated by a leading connection in the
     * {@link LoginOperationalParameterNegotiationStage} only.
     */
    LEADING_LOPNS,
    /**
     * The parameter may be negotiated by any connection in the
     * {@link LoginOperationalParameterNegotiationStage}.
     */
    LOPNS,
    /**
     * The parameter may be negotiated by any connection in the
     * {@link LoginOperationalParameterNegotiationStage} or in the Full Feature
     * Phase (specifically the {@link TextNegotiationStage}).
     */
    LOPNS_AND_FFP,
    /**
     * The parameter may be negotiated by any connection in the Full Feature
     * Phase (specifically the {@link TextNegotiationStage}).
     */
    FFP,
    /**
     * The parameter may only be declared in the initial {@link ProtocolDataUnit}.
     */
    INITIAL,
    /**
     * The parameter may only be declared in the initial {@link ProtocolDataUnit} or by any PDU sent as part
     * of the {@link TextNegotiationStage} in the Full Feature Phase.
     */
    INITIAL_AND_FFP;

    /**
     * This method can be used for checking if a specified {@link Use} permits
     * negotiating its associated parameter given a situation described by
     * several parameters.
     * 
     * @param use
     *            describes the conditions
     * @param loginStage
     *            specifies the stage of phase
     * @param leadingConnection
     *            <code>true</code> if and only if the connection is the leading
     *            connection of its session
     * @param initialPdu
     *            <code>true</code> if and only if the {@link ProtocolDataUnit} is the first PDU sent over the
     *            connection
     * @return <code>true</code> if and only if all requirements of the
     *         <i>use</i> parameter have been met
     */
    private static boolean checkUse(final Use use, final LoginStage loginStage,
        final boolean leadingConnection, final boolean initialPdu) {
        switch (use) {
        case LEADING_LOPNS:
            if (leadingConnection && loginStage == LoginStage.LOGIN_OPERATIONAL_NEGOTIATION)
                return true;
            return false;
        case LOPNS:
            if (loginStage == LoginStage.LOGIN_OPERATIONAL_NEGOTIATION)
                return true;
            return false;
        case LOPNS_AND_FFP:
            if (loginStage == LoginStage.LOGIN_OPERATIONAL_NEGOTIATION
                || loginStage == LoginStage.FULL_FEATURE_PHASE)
                return true;
            return false;
        case FFP:
            if (loginStage == LoginStage.FULL_FEATURE_PHASE)
                return true;
            return false;
        case INITIAL_AND_FFP:
            if (initialPdu && loginStage == LoginStage.FULL_FEATURE_PHASE)
                return true;
            // fall through
        case INITIAL:
            if (initialPdu
                && (loginStage == LoginStage.SECURITY_NEGOTIATION || loginStage == LoginStage.LOGIN_OPERATIONAL_NEGOTIATION))
                return true;
            return false;
        default:
            return false;// unreachable
        }
    }

    /**
     * This method can be used for checking if this {@link Use} instance permits
     * negotiating its associated parameter given a situation described by the
     * passed parameters.
     * 
     * @param loginStage
     *            specifies the stage of phase
     * @param leadingConnection
     *            <code>true</code> if and only if the connection is the leading
     *            connection of its session
     * @param initialPdu
     *            <code>true</code> if and only if the {@link ProtocolDataUnit} is the first PDU sent over the
     *            connection
     * @return <code>true</code> if and only if all requirements of this {@link Use} instance have been met
     */
    public boolean checkUse(final LoginStage loginStage, final boolean leadingConnection,
        final boolean initialPdu) {
        return checkUse(this, loginStage, leadingConnection, initialPdu);
    }
}

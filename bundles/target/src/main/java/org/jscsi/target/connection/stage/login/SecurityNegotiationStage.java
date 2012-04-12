package org.jscsi.target.connection.stage.login;

import java.io.IOException;
import java.security.DigestException;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.login.LoginStage;
import org.jscsi.parser.login.LoginStatus;
import org.jscsi.target.connection.phase.TargetLoginPhase;
import org.jscsi.target.settings.SettingsException;
import org.jscsi.target.settings.TextKeyword;
import org.jscsi.target.settings.TextParameter;

/**
 * A {@link TargetLoginStage} sub-class representing Security Negotiation
 * Stages.
 * 
 * @author Andreas Ergenzinger
 */
public final class SecurityNegotiationStage extends TargetLoginStage {

    private static final Logger LOGGER = Logger
            .getLogger(SecurityNegotiationStage.class);

    /**
     * The constructor.
     * 
     * @param targetLoginPhase
     *            the login phase this stage is a part of
     */
    public SecurityNegotiationStage(TargetLoginPhase targetLoginPhase) {
        super(targetLoginPhase, LoginStage.SECURITY_NEGOTIATION);
    }

    @Override
    public void execute(ProtocolDataUnit initialPdu) throws IOException,
            InterruptedException, InternetSCSIException, DigestException,
            SettingsException {

        // "receive" initial PDU
        BasicHeaderSegment bhs = initialPdu.getBasicHeaderSegment();
        initiatorTaskTag = bhs.getInitiatorTaskTag();

        boolean authenticated = false;

        do {// while initiator is not willing and not authorized to transit to
            // next stage

            // build text parameter string from current login PDU sequence
            final String requestTextParameters = receivePduSequence(initialPdu);

            // split key-value pairs
            final Vector<String> requestKeyValuePairs = TextParameter
                    .tokenizeKeyValuePairs(requestTextParameters);

            // Vector for AuthMethod keys
            final Vector<String> authMethodKeyValuePairs = new Vector<String>();

            // log initiator's key-value pairs
            if (LOGGER.isDebugEnabled()) {
                final StringBuilder sb = new StringBuilder();
                sb.append("request key value pairs:\n");
                for (String s : requestKeyValuePairs)
                    sb.append("   " + s + "\n");
                LOGGER.debug(sb.toString());
            }

            // extract available AuthMethod key-value pair, so that settings can
            // finish
            // processing the other parameters before authorization begins
            String authMethodValues = null;
            if (!authenticated) {// authentication part one
                for (int i = 0; i < requestKeyValuePairs.size(); ++i) {
                    final String[] split = TextParameter
                            .splitKeyValuePair(requestKeyValuePairs.get(i));
                    if (split == null) {
                        sendRejectPdu(LoginStatus.INITIATOR_ERROR);
                        throw new InternetSCSIException(
                                "key=value format error: "
                                        + requestKeyValuePairs.get(i));
                    }
                    if (TextKeyword.AUTH_METHOD.equals(split[0])) {
                        authMethodValues = split[1];
                        // remove key-value pair from Vector
                        requestKeyValuePairs.remove(i--);// correct for shifted
                                                         // indices
                        // no break here to catch all authMethodKeyValuePairs in
                        // else block
                    } else if (isAuthenticationKey(split[0])) {
                        // move key-value pair to authMethodKeyValuePairs
                        authMethodKeyValuePairs.add(requestKeyValuePairs
                                .remove(i--));// correct for shifted indices
                    }
                }
                if (authMethodValues == null) {// missing AuthMethod key
                    sendRejectPdu(LoginStatus.MISSING_PARAMETER);// require
                                                                 // AuthMethod
                                                                 // to be
                                                                 // specified in
                                                                 // first PDU
                                                                 // sequence
                    // close connection
                    throw new InternetSCSIException(
                            "Missing AuthMethod key-value pair");
                }
            }

            // negotiate remaining parameters
            final Vector<String> responseKeyValuePairs = new Vector<String>();// these
                                                                              // will
                                                                              // be
                                                                              // sent
                                                                              // back
            if (!negotiator
                    .negotiate(session.getTargetServer(), stageNumber, connection.isLeadingConnection(),
                            ((TargetLoginPhase) targetPhase)
                                    .getFirstPduAndSetToFalse(),
                            requestKeyValuePairs, responseKeyValuePairs)) {
                // negotiation error
                sendRejectPdu(LoginStatus.INITIATOR_ERROR);
                throw new InternetSCSIException("negotiation failure");
            }

            // ** authentication ** (part two)
            if (!authenticated) {
                if (authMethodValues.contains(TextKeyword.NONE)) {

                    authenticated = true;
                    responseKeyValuePairs.add(TextParameter.toKeyValuePair(
                            TextKeyword.AUTH_METHOD,// key
                            TextKeyword.NONE));// value

                    // concatenate key value pairs to single string
                    final String responseString = TextParameter
                            .concatenateKeyValuePairs(responseKeyValuePairs);

                    if (LOGGER.isDebugEnabled())
                        LOGGER.debug("response: " + responseString);

                    // send reply (sequence), set transit bit of last PDU
                    sendPduSequence(responseString, requestedNextStageNumber);

                    // leave this (and proceed to next) stage
                    if (requestedNextStageNumber == LoginStage.LOGIN_OPERATIONAL_NEGOTIATION
                            || requestedNextStageNumber == LoginStage.FULL_FEATURE_PHASE) {
                        nextStageNumber = requestedNextStageNumber;
                        return;
                    }
                } else {
                    // TODO support CHAP (and use String
                    // authMethodKeyValuePairs)
                    LOGGER.error("initiator attempted CHAP authentication");
                    // nextStageNumber = null;//no change
                    return;
                }

            }

        } while (!bhs.isFinalFlag() && !authenticated);
    }

    /**
     * Checks if a the parameter is the key of an AuthMethod Key, which means
     * one of the following (where &#60;key&#62; depends on the AuthMethod
     * prefix):
     * <ul>
     * <li>CHAP_&#60;key&#62;/il>
     * <li>KRB_&#60;key&#62;/il>
     * <li>SPKM_&#60;key&#62;/il>
     * <li>SRP_&#60;key&#62;/il>
     * </ul>
     * 
     * @param <i>key</i> part of a <i>key-value</i> pair
     * @return <code>true</code> if the String is an AuthMethod key,
     *         <code>false</code> if it is not.
     */
    private final boolean isAuthenticationKey(final String key) {
        if (key == null || key.length() < 5)
            return false;
        final String fourChars = key.substring(0, 4);
        final String fiveChars = key.substring(0, 5);
        if ("CHAP_".matches(fiveChars)
                || "KRB_".matches(fourChars)
                || "SPKM_".matches(fiveChars)
                || "SRP_".matches(fourChars)
                || (key.length() >= 10 && "TargetAuth".matches(key.substring(0,
                        10))))
            return true;
        return false;
    }
}

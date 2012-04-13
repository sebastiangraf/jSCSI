package org.jscsi.target.connection.stage.login;

import java.io.IOException;
import java.security.DigestException;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.login.LoginStage;
import org.jscsi.parser.login.LoginStatus;
import org.jscsi.target.connection.phase.TargetLoginPhase;
import org.jscsi.target.settings.SettingsException;
import org.jscsi.target.settings.TextParameter;

/**
 * A {@link TargetLoginStage} sub-class representing Login Operational
 * (Parameter) Negotiation Stages.
 * 
 * @author Andreas Ergenzinger
 */
public final class LoginOperationalParameterNegotiationStage extends TargetLoginStage {

    private static final Logger LOGGER = Logger.getLogger(LoginOperationalParameterNegotiationStage.class);

    /**
     * The constructor.
     * 
     * @param targetLoginPhase
     *            the login phase this stage is a part of.
     */
    public LoginOperationalParameterNegotiationStage(TargetLoginPhase targetLoginPhase) {
        super(targetLoginPhase, LoginStage.LOGIN_OPERATIONAL_NEGOTIATION);
    }

    @Override
    public void execute(ProtocolDataUnit pdu) throws IOException, InterruptedException,
        InternetSCSIException, DigestException, IllegalArgumentException, SettingsException {

        LOGGER.debug("Entering LOPN Stage");

        BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        initiatorTaskTag = bhs.getInitiatorTaskTag();

        String keyValuePairProposal = receivePduSequence(pdu);

        // negotiate parameters, leave if unsuccessful
        final List<String> requestKeyValuePairs = TextParameter.tokenizeKeyValuePairs(keyValuePairProposal);
        final List<String> responseKeyValuePairs = new Vector<String>();
        if (!negotiator.negotiate(session.getTargetServer(), stageNumber, connection.isLeadingConnection(),
            ((TargetLoginPhase)targetPhase).getFirstPduAndSetToFalse(), requestKeyValuePairs,
            responseKeyValuePairs)) {
            // negotiation failure, no exception
            sendRejectPdu(LoginStatus.INITIATOR_ERROR);
            // nextStageNumber = null;//no change
            return;
        }

        // print request and response key value pairs if debugging
        if (LOGGER.isDebugEnabled()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("request: ");
            for (String s : requestKeyValuePairs) {
                sb.append("\n  ");
                sb.append(s);
            }
            sb.append("\nresponse: ");
            for (String s : responseKeyValuePairs) {
                sb.append("\n  ");
                sb.append(s);
            }
            LOGGER.debug(sb.toString());
        }

        // make sure that initiator wants to proceed to FFP, leave if it does
        // not
        if (requestedNextStageNumber != LoginStage.FULL_FEATURE_PHASE) {
            sendRejectPdu(LoginStatus.INITIATOR_ERROR);
            throw new InternetSCSIException();
        }

        // concatenate key-value pairs to null char-separated string
        final String keyValuePairReply = TextParameter.concatenateKeyValuePairs(responseKeyValuePairs);

        // send reply, finish negotiation, and return successfully
        sendPduSequence(keyValuePairReply, LoginStage.FULL_FEATURE_PHASE);
        negotiator.finishNegotiation(true);
        nextStageNumber = LoginStage.FULL_FEATURE_PHASE;
    }
}

package org.jscsi.target.connection.stage.fullfeature;

import java.io.IOException;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.logout.LogoutResponse;
import org.jscsi.target.connection.TargetPduFactory;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.settings.SettingsException;

/**
 * A stage for processing logout requests.
 * <p>
 * Since <code>MaxConnections</code> is currently limited to <code>1</code>, all logout requests will be
 * treated as requests to close the session.
 * 
 * @author Andreas Ergenzinger
 */
public final class LogoutStage extends TargetFullFeatureStage {

    public LogoutStage(TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    @Override
    public void execute(ProtocolDataUnit pdu) throws IOException, InterruptedException,
        InternetSCSIException, DigestException, SettingsException {

        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final int initiatorTaskTag = bhs.getInitiatorTaskTag();

        final ProtocolDataUnit responsePDU =
            TargetPduFactory.createLogoutResponsePdu(LogoutResponse.CONNECTION_CLOSED_SUCCESSFULLY,
                initiatorTaskTag, (short)settings.getDefaultTime2Wait(),// time2Wait
                (short)settings.getDefaultTime2Retain());// time2Retain

        connection.sendPdu(responsePDU);
    }

}

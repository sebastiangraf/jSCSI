package org.jscsi.target.connection.stage.login;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.login.LoginRequestParser;
import org.jscsi.parser.login.LoginStage;
import org.jscsi.parser.login.LoginStatus;
import org.jscsi.target.connection.TargetPduFactory;
import org.jscsi.target.connection.phase.TargetLoginPhase;
import org.jscsi.target.connection.stage.TargetStage;
import org.jscsi.target.settings.ConnectionSettingsNegotiator;
import org.jscsi.target.settings.SettingsException;
import org.jscsi.target.util.ReadWrite;

/**
 * This class is an abstract super-class for stages of the {@link TargetLoginPhase} (see
 * {@link TargetConnection} for a description of
 * the relationship between sessions, connections, phases and sessions), namely
 * the {@link LoginOperationalParameterNegotiationStage} and the {@link SecurityNegotiationStage}.
 * <p>
 * The stage is started by calling the {@link #execute(ProtocolDataUnit)} method with the first
 * {@link ProtocolDataUnit} to be processed as part of the stage.
 * <p>
 * Of equal importance is the {@link #getNextStageNumber()} method, which must be used to find out which stage
 * or phase will follow this one.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class TargetLoginStage extends TargetStage {

    /**
     * Manages the text parameter negotiation.
     */
    protected final ConnectionSettingsNegotiator negotiator;

    /**
     * The stage number used for describing this stage in Login Request PDUs.
     * 
     * @see LoginRequestParser#getCurrentStageNumber()
     * @see LoginRequestParser#getNextStageNumber()
     */
    protected final LoginStage stageNumber;

    /**
     * An identifier used by the initiator to identify the login task. Sent with
     * the first Login Request PDU.
     */
    protected int initiatorTaskTag;

    /**
     * A stage number describing which stage the initiator wants to transition
     * to. This value will be updated with every received PDU.
     * 
     * @see #stageNumber
     */
    protected LoginStage requestedNextStageNumber;

    /**
     * A stage number describing which stage must follow this stage.
     * <p>
     * This value is initialized with <code>null</code>, and will be changed in
     * {@link #execute(ProtocolDataUnit)}, if the stage was finished successfully.
     * 
     * @see #stageNumber
     */
    protected LoginStage nextStageNumber;

    /**
     * The abstract constructor.
     * 
     * @param targetLoginPhase
     *            the phase this stage is a part of
     * @param stageNumber
     *            the stage number used for describing this stage in Login
     *            Request PDUs
     */
    public TargetLoginStage(final TargetLoginPhase targetLoginPhase, final LoginStage stageNumber) {
        super(targetLoginPhase);
        this.stageNumber = stageNumber;
        negotiator = connection.getConnectionSettingsNegotiator();
    }

    /**
     * Returns <code>true</code>, if and only if the specified PDU is a Login
     * Request PDU and the CSN and InitiatorTaskTag fields check out.
     * 
     * @param pdu
     *            the PDU to check
     * @return <code>true</code> if the PDU checks out
     */
    protected boolean checkPdu(ProtocolDataUnit pdu) {
        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final LoginRequestParser parser = (LoginRequestParser)bhs.getParser();
        if (bhs.getOpCode() == OperationCode.LOGIN_REQUEST && parser.getCurrentStageNumber() == stageNumber
            && bhs.getInitiatorTaskTag() == initiatorTaskTag)
            return true;
        return false;
    }

    /**
     * Receives a sequence of Login Request PDUs (as indicated by the
     * {@link LoginRequestParser#isContinueFlag()} and returns the concatenated
     * content of the text data segments.
     * 
     * @return the concatenated content of the text data segments
     * @throws DigestException
     * @throws InternetSCSIException
     * @throws IOException
     * @throws SettingsException
     * @throws InterruptedException
     */
    protected final String receivePduSequence() throws DigestException, InternetSCSIException, IOException,
        SettingsException, InterruptedException {
        final ProtocolDataUnit pdu = connection.receivePdu();
        return receivePduSequence(pdu);
    }

    /**
     * Receives a sequence of Login Request PDUs (as indicated by the
     * {@link LoginRequestParser#isContinueFlag()} and returns the concatenated
     * content of the text data segments.
     * 
     * @param pdu
     *            the first PDU of the sequence
     * @return the concatenated content of the text data segments
     * @throws InternetSCSIException
     * @throws InterruptedException
     * @throws IOException
     * @throws DigestException
     * @throws SettingsException
     */
    protected final String receivePduSequence(ProtocolDataUnit pdu) throws InternetSCSIException,
        InterruptedException, IOException, DigestException, SettingsException {

        // StringBuilder for the key-value pairs received during this sequence
        final StringBuilder stringBuilder = new StringBuilder();

        // for accessing the fields of the last received PDU
        BasicHeaderSegment bhs;
        LoginRequestParser parser;

        // begin sequence
        int sequenceLength = 1;
        while (sequenceLength <= session.getTargetServer().getConfig().getInMaxRecvTextPduSequenceLength()) {
            bhs = pdu.getBasicHeaderSegment();
            parser = (LoginRequestParser)bhs.getParser();

            // check PDU
            if (!checkPdu(pdu)) {
                // send login reject and leave stage
                sendRejectPdu(LoginStatus.INVALID_DURING_LOGIN);
                throw new InternetSCSIException("Wrong PDU in TargetLoginStage");
            }

            // PDU is okay, so append text data segment to stringBuilder
            ReadWrite.appendTextDataSegmentToStringBuffer(pdu.getDataSegment(), stringBuilder);

            // remember what stage the initiator wants to transition to
            requestedNextStageNumber = parser.getNextStageNumber();

            // continue?
            if (parser.isContinueFlag()) {
                // send reception confirmation
                pdu = TargetPduFactory.createLoginResponsePdu(false,// transitFlag
                    false,// continueFlag
                    stageNumber,// currentStage
                    stageNumber,// nextStage
                    session.getInitiatorSessionID(),// initiatorSessionID
                    session.getTargetSessionIdentifyingHandle(),// targetSessionIdentifyingHandle
                    initiatorTaskTag, LoginStatus.SUCCESS,// status
                    ByteBuffer.allocate(0));// dataSegment
                connection.sendPdu(pdu);

                // receive the next pdu
                pdu = connection.receivePdu();
            } else
                // sequence is over
                return stringBuilder.toString();
        }

        // initiator's text PDU sequence was too long
        // send login reject and leave stage
        sendRejectPdu(LoginStatus.OUT_OF_RESOURCES);
        throw new InternetSCSIException("Wrong PDU in TargetLoginStage");
    }

    /**
     * Sends a Login Response PDU sequence containing the specified
     * <i>key-value</i> pairs.
     * 
     * @param keyValuePairs
     *            contains <i>key-value</i> pairs to send
     * @param nextStage
     *            indicates if the target is willing to transition to a
     *            different stage
     * @throws SettingsException
     * @throws InterruptedException
     * @throws IOException
     * @throws InternetSCSIException
     * @throws DigestException
     */
    protected final void sendPduSequence(final String keyValuePairs, final LoginStage nextStage)
        throws SettingsException, InterruptedException, IOException, InternetSCSIException, DigestException {

        // some variables
        ProtocolDataUnit pdu;
        BasicHeaderSegment bhs;
        LoginRequestParser parser;
        boolean continueFlag = true;
        boolean transitFlag = false;

        // split input string into text data segments
        final ByteBuffer[] dataSegments = ReadWrite.stringToTextDataSegments(keyValuePairs,// string
            settings.getMaxRecvDataSegmentLength());// bufferSize

        // send all data segments (and receive confirmations)
        for (int i = 0; i < dataSegments.length; ++i) {

            // adjust flags
            if (i == dataSegments.length - 1) {
                continueFlag = false;
                if (stageNumber != nextStage)
                    transitFlag = true;
            }

            // create and send PDU
            pdu = TargetPduFactory.createLoginResponsePdu(transitFlag,// transitFlag
                continueFlag,// continueFlag
                stageNumber,// currentStage
                nextStage,// nextStage
                session.getInitiatorSessionID(),// initiatorSessionID
                session.getTargetSessionIdentifyingHandle(),// targetSessionIdentifyingHandle
                initiatorTaskTag, LoginStatus.SUCCESS,// status
                dataSegments[i]);// dataSegment
            connection.sendPdu(pdu);

            // receive confirmation
            if (continueFlag) {
                // receive and check
                pdu = connection.receivePdu();
                bhs = pdu.getBasicHeaderSegment();
                parser = (LoginRequestParser)bhs.getParser();
                if (!checkPdu(pdu) || parser.isContinueFlag()) {
                    // send login reject and leave stage
                    sendRejectPdu(LoginStatus.INITIATOR_ERROR);
                    throw new InternetSCSIException();
                }
            }
        }
    }

    /**
     * Sends a Login Response PDU informing the initiator that an error has
     * occurred and that the connection must be closed.
     * 
     * @param errorStatus
     *            hints to the cause of the error
     * @throws InterruptedException
     * @throws IOException
     * @throws InternetSCSIException
     */
    protected final void sendRejectPdu(final LoginStatus errorStatus) throws InterruptedException,
        IOException, InternetSCSIException {
        final ProtocolDataUnit rejectPDU = TargetPduFactory.createLoginResponsePdu(false,// transit flag
            false,// continueFlag
            stageNumber,// currentStage
            stageNumber,// nextStage
            session.getInitiatorSessionID(),// initiatorSessionID
            session.getTargetSessionIdentifyingHandle(),// targetSessionIdentifyingHandle
            initiatorTaskTag,// initiatorTaskTag
            errorStatus,// status
            ByteBuffer.allocate(0));// dataSegment
        connection.sendPdu(rejectPDU);
    }

    /**
     * Returns a stage number describing which stage of phase must follow this
     * stage, or <code>null</code> if the initiator is not allowed to transition
     * any further.
     * 
     * @return an identifier of the next stage/phase or <code>null</code>
     */
    public final LoginStage getNextStageNumber() {
        return nextStageNumber;
    }
}

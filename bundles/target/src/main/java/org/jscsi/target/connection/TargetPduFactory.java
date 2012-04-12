package org.jscsi.target.connection;

import java.nio.ByteBuffer;

import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.ProtocolDataUnitFactory;
import org.jscsi.parser.data.DataInParser;
import org.jscsi.parser.login.ISID;
import org.jscsi.parser.login.LoginResponseParser;
import org.jscsi.parser.login.LoginStage;
import org.jscsi.parser.login.LoginStatus;
import org.jscsi.parser.logout.LogoutResponse;
import org.jscsi.parser.logout.LogoutResponseParser;
import org.jscsi.parser.nop.NOPInParser;
import org.jscsi.parser.r2t.Ready2TransferParser;
import org.jscsi.parser.scsi.SCSIResponseParser;
import org.jscsi.parser.scsi.SCSIStatus;
import org.jscsi.parser.text.TextResponseParser;
import org.jscsi.target.scsi.ScsiResponseDataSegment;

/**
 * A factory class for creating instances of different {@link ProtocolDataUnit} types sent by the jSCSI
 * Target.
 * <p>
 * The static build methods of this class have a parameter for each field and flag of the respective PDU type,
 * except for <code>StatusSN</code>, <code>ExpCmdSN</code>, and <code>MaxCmdSN</code> fields, which are set
 * during the send process (see {@link TargetSenderWorker#sendOverWire(ProtocolDataUnit)}).
 */
public class TargetPduFactory {

    /**
     * Used for creating blank {@link ProtocolDataUnit} objects.
     */
    private static final ProtocolDataUnitFactory factory = new ProtocolDataUnitFactory();

    public static final ProtocolDataUnit createDataInPdu(boolean finalFlag, boolean acknowledgeFlag,
        boolean residualOverflowFlag, boolean residualUnderflowFlag, boolean statusFlag, SCSIStatus status,
        long logicalUnitNumber, int initiatorTaskTag, int targetTransferTag, int dataSequenceNumber,//
        int bufferOffset, int residualCount, ByteBuffer dataSegment) {
        final ProtocolDataUnit pdu =
            factory.create(false, finalFlag, OperationCode.SCSI_DATA_IN, "None", "None");
        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final DataInParser parser = (DataInParser)bhs.getParser();
        parser.setAcknowledgeFlag(acknowledgeFlag);
        parser.setResidualOverflowFlag(residualOverflowFlag);
        parser.setResidualUnderflowFlag(residualUnderflowFlag);
        parser.setStatusFlag(statusFlag);
        parser.setStatus(status);
        parser.setLogicalUnitNumber(logicalUnitNumber);
        bhs.setInitiatorTaskTag(initiatorTaskTag);
        parser.setTargetTransferTag(targetTransferTag);
        parser.setDataSequenceNumber(dataSequenceNumber);
        parser.setBufferOffset(bufferOffset);
        parser.setResidualCount(residualCount);
        pdu.setDataSegment(dataSegment);
        return pdu;
    }

    public static final ProtocolDataUnit
        createLoginResponsePdu(boolean transitFlag, boolean continueFlag, LoginStage currentStage,
            LoginStage nextStage, ISID initiatorSessionID, short targetSessionIdentifyingHandle,
            int initiatorTaskTag, LoginStatus status, ByteBuffer dataSegment) {
        final ProtocolDataUnit pdu =
            factory.create(false, transitFlag, OperationCode.LOGIN_RESPONSE, "None", "None");
        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final LoginResponseParser parser = (LoginResponseParser)bhs.getParser();
        parser.setContinueFlag(continueFlag);
        parser.setCurrentStageNumber(currentStage);
        parser.setNextStageNumber(nextStage);
        parser.setInitiatorSessionID(initiatorSessionID);
        parser.setTargetSessionIdentifyingHandle(targetSessionIdentifyingHandle);
        bhs.setInitiatorTaskTag(initiatorTaskTag);
        parser.setStatus(status);
        pdu.setDataSegment(dataSegment);
        return pdu;
    }

    public static final ProtocolDataUnit createLogoutResponsePdu(LogoutResponse response,
        int initiatorTaskTag, short time2Wait, short time2Retain) {
        final ProtocolDataUnit pdu =
            factory.create(false, true, OperationCode.LOGOUT_RESPONSE, "None", "None");
        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final LogoutResponseParser parser = (LogoutResponseParser)bhs.getParser();
        parser.setResponse(response);
        bhs.setInitiatorTaskTag(initiatorTaskTag);
        parser.setTime2Wait(time2Wait);
        parser.setTime2Retain(time2Retain);
        return (pdu);
    }

    public static final ProtocolDataUnit createReadyToTransferPdu(long logicalUnitNumber,
        int initiatorTaskTag, int targetTransferTag, int readyToTransferSequenceNumber, int bufferOffset,
        int desiredDataTransferLength) {
        final ProtocolDataUnit pdu = factory.create(false, true, OperationCode.R2T, "None", "None");
        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final Ready2TransferParser parser = (Ready2TransferParser)bhs.getParser();
        parser.setLogicalUnitNumber(logicalUnitNumber);
        bhs.setInitiatorTaskTag(initiatorTaskTag);
        parser.setTargetTransferTag(targetTransferTag);
        parser.setReady2TransferSequenceNumber(readyToTransferSequenceNumber);
        parser.setBufferOffset(bufferOffset);
        parser.setDesiredDataTransferLength(desiredDataTransferLength);
        return pdu;
    }

    public static final ProtocolDataUnit createSCSIResponsePdu(
        final boolean bidirectionalReadResidualOverflow, final boolean bidirectionalReadResidualUnderflow,
        final boolean residualOverflow, final boolean residualUnderflow,
        final SCSIResponseParser.ServiceResponse response, final SCSIStatus status,
        final int initiatorTaskTag, final int snackTag, final int expectedDataSequenceNumber,
        final int bidirectionalReadResidualCount, final int residualCount,
        final ScsiResponseDataSegment scsiResponseDataSegment) {
        final ProtocolDataUnit pdu = factory.create(false, true, OperationCode.SCSI_RESPONSE, "None", "None");
        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final SCSIResponseParser parser = (SCSIResponseParser)bhs.getParser();
        parser.setBidirectionalReadResidualOverflow(bidirectionalReadResidualOverflow);
        parser.setBidirectionalReadResidualUnderflow(bidirectionalReadResidualUnderflow);
        parser.setResidualOverflow(residualOverflow);
        parser.setResidualUnderflow(residualUnderflow);
        parser.setResponse(response);
        parser.setStatus(status);
        bhs.setInitiatorTaskTag(initiatorTaskTag);
        parser.setSNACKTag(snackTag);
        parser.setExpectedDataSequenceNumber(expectedDataSequenceNumber);
        parser.setBidirectionalReadResidualCount(bidirectionalReadResidualCount);
        parser.setResidualCount(residualCount);
        pdu.setDataSegment(scsiResponseDataSegment.serialize());
        return pdu;
    }

    public static final ProtocolDataUnit createTextResponsePdu(boolean finalFlag, boolean continueFlag,
        long logicalUnitNumber, int initiatorTaskTag, int targetTransferTag, ByteBuffer dataSegment) {
        final ProtocolDataUnit pdu =
            factory.create(false, finalFlag, OperationCode.TEXT_RESPONSE, "None", "None");
        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final TextResponseParser parser = (TextResponseParser)bhs.getParser();
        parser.setContinueFlag(continueFlag);
        parser.setLogicalUnitNumber(logicalUnitNumber);
        bhs.setInitiatorTaskTag(initiatorTaskTag);
        parser.setTargetTransferTag(targetTransferTag);
        pdu.setDataSegment(dataSegment);
        return pdu;
    }

    public static final ProtocolDataUnit createNopInPDU(final long logicalUnitNumber,
        final int initiatorTaskTag, final int targetTransferTag, final ByteBuffer dataSegment) {
        final ProtocolDataUnit pdu = factory.create(false, true, OperationCode.NOP_IN, "None", "None");
        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final NOPInParser parser = (NOPInParser)bhs.getParser();
        parser.setLogicalUnitNumber(logicalUnitNumber);
        bhs.setInitiatorTaskTag(initiatorTaskTag);
        parser.setTargetTransferTag(targetTransferTag);
        pdu.setDataSegment(dataSegment);
        return pdu;
    }
}

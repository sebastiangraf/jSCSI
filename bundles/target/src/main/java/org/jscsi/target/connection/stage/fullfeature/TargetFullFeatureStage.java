package org.jscsi.target.connection.stage.fullfeature;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.scsi.SCSIResponseParser;
import org.jscsi.parser.scsi.SCSIResponseParser.ServiceResponse;
import org.jscsi.parser.scsi.SCSIStatus;
import org.jscsi.target.connection.TargetPduFactory;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.connection.stage.TargetStage;
import org.jscsi.target.scsi.IResponseData;
import org.jscsi.target.scsi.ScsiResponseDataSegment;
import org.jscsi.target.scsi.sense.AdditionalSenseBytes;
import org.jscsi.target.scsi.sense.AdditionalSenseCodeAndQualifier;
import org.jscsi.target.scsi.sense.ErrorType;
import org.jscsi.target.scsi.sense.FixedFormatSenseData;
import org.jscsi.target.scsi.sense.SenseKey;
import org.jscsi.target.scsi.sense.information.FourByteInformation;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.FieldPointerSenseKeySpecificData;

/**
 * This class is an abstract super-class for stages of the
 * {@link TargetFullFeaturePhase}.
 * 
 * @see TargetStage
 * @author Andreas Ergenzinger
 */
public abstract class TargetFullFeatureStage extends TargetStage {

    /**
     * The abstract constructor.
     * 
     * @param targetFullFeaturePhase
     *            the phase this stage is a part of
     */
    public TargetFullFeatureStage(TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    /**
     * Creates a PDU with {@link FixedFormatSenseData} that must be sent to the
     * initiator after receiving a Command Descriptor Block with an illegal
     * field.
     * 
     * @param senseKeySpecificData
     *            contains a list of all illegal fields
     * @param additionalSenseCodeAndQualifier
     *            provides more specific information about the cause of the
     *            check condition
     * @param initiatorTaskTag
     *            used by the initiator to identify the task
     * @param expectedDataTransferLength
     *            the amount of payload data expected by the initiator (i.e.
     *            allocated buffer space)
     * @return the error PDU
     */
    protected static final ProtocolDataUnit createFixedFormatErrorPdu(
            final FieldPointerSenseKeySpecificData[] senseKeySpecificData,
            final AdditionalSenseCodeAndQualifier additionalSenseCodeAndQualifier,
            final int initiatorTaskTag, final int expectedDataTransferLength) {

        // create the whole sense data
        FixedFormatSenseData senseData = new FixedFormatSenseData(false,// valid
                ErrorType.CURRENT,// error type
                false,// file mark
                false,// end of medium
                false,// incorrect length indicator
                SenseKey.ILLEGAL_REQUEST,// sense key
                new FourByteInformation(),// information
                new FourByteInformation(),// command specific information
                additionalSenseCodeAndQualifier,// additional sense code and
                                                // qualifier
                (byte) 0,// field replaceable unit code
                senseKeySpecificData[0],// sense key specific data, only report
                                        // first problem
                new AdditionalSenseBytes());// additional sense bytes

        // keep only the part of the sense data that will be sent
        final ScsiResponseDataSegment dataSegment = new ScsiResponseDataSegment(
                senseData, expectedDataTransferLength);
        final int senseDataSize = senseData.size();

        // calculate residuals and flags
        final int residualCount = Math.abs(expectedDataTransferLength
                - senseDataSize);
        final boolean residualOverflow = expectedDataTransferLength < senseDataSize;
        final boolean residualUnderflow = expectedDataTransferLength > senseDataSize;

        // create and return PDU
        return TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                false,// bidirectionalReadResidualUnderflow
                residualOverflow,// residualOverflow
                residualUnderflow,// residualUnderflow,
                SCSIResponseParser.ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response,
                SCSIStatus.CHECK_CONDITION,// status,
                initiatorTaskTag,// initiatorTaskTag,
                0,// snackTag
                0,// expectedDataSequenceNumber
                0,// bidirectionalReadResidualCount
                residualCount,// residualCount
                dataSegment);// data segment
    }

    /**
     * Creates a PDU with {@link FixedFormatSenseData} that must be sent to the
     * initiator after receiving a Command Descriptor Block with an illegal
     * field, which requires the the additional sense code
     * {@link AdditionalSenseCodeAndQualifier#INVALID_FIELD_IN_CDB}.
     * 
     * @param senseKeySpecificData
     *            contains a list of all illegal fields
     * @param initiatorTaskTag
     *            used by the initiator to identify the task
     * @param expectedDataTransferLength
     *            the amount of payload data expected by the initiator (i.e.
     *            allocated buffer space)
     * @return the error PDU
     */
    protected static final ProtocolDataUnit createFixedFormatErrorPdu(
            final FieldPointerSenseKeySpecificData[] senseKeySpecificData,
            final int initiatorTaskTag, final int expectedDataTransferLength) {
        return createFixedFormatErrorPdu(senseKeySpecificData,
                AdditionalSenseCodeAndQualifier.INVALID_FIELD_IN_CDB,
                initiatorTaskTag, expectedDataTransferLength);
    }

    /**
     * Creates a SCSI Response PDU with a length zero data segment. Objects
     * created with this method can be used as replies in task terminating with
     * {@link SCSIStatus#GOOD} which do not require additional data to be
     * transfered, or for creating follow-up PDU with
     * {@link SCSIStatus#CHECK_CONDITION} status sent after Data-In PDUs with
     * sense data.
     * 
     * @param status
     *            the SCSI status of the task
     * @param initiatorTaskTag
     *            used by the initiator to identify the task
     * @param expectedDataTransferLength
     *            total amount of payload data in bytes expected by the
     *            initiator
     * @param responseDataSize
     *            actual amount of payload data in bytes sent by the target
     * @return the SCSI Response PDU
     */
    protected static final ProtocolDataUnit createScsiResponsePdu(
            final SCSIStatus status, final int initiatorTaskTag,
            final int expectedDataTransferLength, final int responseDataSize) {

        // calculate residuals and flags
        final int residualCount = Math.abs(expectedDataTransferLength
                - responseDataSize);
        final boolean residualOverflow = expectedDataTransferLength < responseDataSize;
        final boolean residualUnderflow = expectedDataTransferLength > responseDataSize;

        return TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                false,// bidirectionalReadResidualUnderflow
                residualOverflow,// residualOverflow,
                residualUnderflow,// residualUnderflow,
                SCSIResponseParser.ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response
                status,// status
                initiatorTaskTag,// initiatorTaskTag
                0,// snackTag
                0,// expectedDataSequenceNumber
                0,// bidirectionalReadResidualCount
                residualCount,// residualCount
                ScsiResponseDataSegment.EMPTY_DATA_SEGMENT);// data segment
    }

    /**
     * Sends a two byte sequence of a Data-In and a SCSI Response PDU with the
     * specified <i>responseData</i> payload to the initiator.
     * 
     * @param initiatorTaskTag
     *            used by the initiator to identify the task
     * @param expectedDataTransferLength
     *            the total amount of payload data in bytes expected by the
     *            initiator
     *            <p>
     *            The method might throw exceptions during PDU serialization and
     *            sending.
     * @param responseData
     *            the data requested by the initiator
     * @throws InterruptedException
     * @throws IOException
     * @throws InternetSCSIException
     */
    protected final void sendResponse(final int initiatorTaskTag,
            final int expectedDataTransferLength,
            final IResponseData responseData) throws InterruptedException,
            IOException, InternetSCSIException {

        // serialize all response data
        final ByteBuffer fullBuffer = ByteBuffer.allocate(responseData.size());
        responseData.serialize(fullBuffer, 0);

        // copy fullBuffer to buffer with size trimmed to
        // expectedDataTransferLength
        ByteBuffer trimmedBuffer;
        if (fullBuffer.capacity() <= expectedDataTransferLength) {
            // no trimming
            trimmedBuffer = fullBuffer;
        } else {
            trimmedBuffer = ByteBuffer.allocate(expectedDataTransferLength);
            trimmedBuffer.put(fullBuffer.array(),// source array
                    0,// offset in source
                    expectedDataTransferLength);// length
        }

        // coompute residual count and associated flags
        final boolean residualOverflow = expectedDataTransferLength < fullBuffer
                .capacity();
        final boolean residualUnderflow = expectedDataTransferLength > fullBuffer
                .capacity();
        final int residualCount = Math.abs(expectedDataTransferLength
                - fullBuffer.capacity());

        // //create and send PDU//TODO this worked for Open-iSCSI, MS Initiator
        // does not like phase collapse
        // ProtocolDataUnit pdu = TargetPduFactory.createDataInPdu(
        // true,//finalFlag
        // false,//acknowledgeFlag always false
        // residualOverflow,//residualOverflowFlag
        // residualUnderflow,//residualUnderflowFlag
        // true,//statusFlag
        // SCSIStatus.GOOD,//status
        // 0,//logicalUnitNumber reserved
        // initiatorTaskTag,//initiatorTaskTag
        // -1,//targetTransferTag reserved
        // 0,//dataSequenceNumber
        // 0,//bufferOffset
        // residualCount,//residualCount
        // trimmedBuffer);//dataSegment

        // create and send PDU
        ProtocolDataUnit pdu = TargetPduFactory.createDataInPdu(true,// finalFlag
                false,// acknowledgeFlag always false
                false,// residualOverflowFlag x
                false,// residualUnderflowFlag x
                false,// statusFlag
                SCSIStatus.GOOD,// status, reserved
                0,// logicalUnitNumber reserved
                initiatorTaskTag,// initiatorTaskTag
                -1,// targetTransferTag reserved
                0,// dataSequenceNumber
                0,// bufferOffset
                0,// residualCount x
                trimmedBuffer);// dataSegment

        connection.sendPdu(pdu);

        pdu = TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                false,// bidirectionalReadResidualUnderflow
                residualOverflow,// residualOverflow
                residualUnderflow,// residualUnderflow
                ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response
                SCSIStatus.GOOD,// status
                initiatorTaskTag, 0,// snackTag, reserved
                0,// expectedDataSequenceNumber
                0,// bidirectionalReadResidualCount
                residualCount,// residualCount
                ScsiResponseDataSegment.EMPTY_DATA_SEGMENT);// scsiResponseDataSegment

        connection.sendPdu(pdu);

    }
}

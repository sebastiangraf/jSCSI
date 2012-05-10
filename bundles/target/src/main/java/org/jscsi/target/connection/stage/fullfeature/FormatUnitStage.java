package org.jscsi.target.connection.stage.fullfeature;

import java.io.IOException;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.scsi.SCSIResponseParser;
import org.jscsi.parser.scsi.SCSIStatus;
import org.jscsi.target.connection.TargetPduFactory;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.scsi.ScsiResponseDataSegment;
import org.jscsi.target.scsi.cdb.FormatUnitCDB;
import org.jscsi.target.scsi.sense.AdditionalSenseBytes;
import org.jscsi.target.scsi.sense.AdditionalSenseCodeAndQualifier;
import org.jscsi.target.scsi.sense.ErrorType;
import org.jscsi.target.scsi.sense.FixedFormatSenseData;
import org.jscsi.target.scsi.sense.SenseKey;
import org.jscsi.target.scsi.sense.information.FourByteInformation;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.FieldPointerSenseKeySpecificData;
import org.jscsi.target.settings.SettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A stage for processing <code>FORMAT UNIT</code> SCSI commands.
 * <p>
 * The <code>FORMAT UNIT</code> command requests that the device server format the medium into application
 * client accessible logical blocks as specified in the number of logical blocks and logical block length
 * values received in the last mode parameter block descriptor in a <code>MODE SELECT</code> command (see
 * SPC-4). In addition, the device server may certify the medium and create control structures for the
 * management of the medium and defects.
 * <p>
 * The degree that the medium is altered by this command is vendor specific.
 * <p>
 * If a device server receives a <code>FORMAT UNIT</code> command before receiving a MODE SELECT command with
 * a mode parameter block descriptor, then the device server shall use the number of logical blocks and
 * logical block length at which the logical unit is currently formatted (i.e., no change is made to the
 * number of logical blocks and the logical block length of the logical unit during the format operation).
 * <p>
 * <code>FORMAT UNIT</code> commands received by the jSCI Target will not lead to any persistent changes of
 * the virtual logical unit.
 * 
 * @author Andreas Ergenzinger
 */
public class FormatUnitStage extends TargetFullFeatureStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadStage.class);

    public FormatUnitStage(TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    @Override
    public void execute(ProtocolDataUnit pdu) throws IOException, InterruptedException,
        InternetSCSIException, DigestException, SettingsException {

        LOGGER.debug("Initiator has sent FORMAT UNIT command.");

        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final SCSICommandParser parser = (SCSICommandParser)bhs.getParser();

        ProtocolDataUnit responsePdu = null;// the response PDU

        // get command details in CDB
        final FormatUnitCDB cdb = new FormatUnitCDB(parser.getCDB());
        final FieldPointerSenseKeySpecificData[] illegalFieldPointers = cdb.getIllegalFieldPointers();

        if (illegalFieldPointers != null) {
            // an illegal request has been made

            FixedFormatSenseData senseData = new FixedFormatSenseData(false,// valid
                ErrorType.CURRENT,// error type
                false,// file mark
                false,// end of medium
                false,// incorrect length indicator
                SenseKey.ILLEGAL_REQUEST,// sense key
                new FourByteInformation(),// information
                new FourByteInformation(),// command specific information
                AdditionalSenseCodeAndQualifier.INVALID_FIELD_IN_CDB,// additional
                                                                     // sense
                                                                     // code
                                                                     // and
                                                                     // qualifier
                (byte)0,// field replaceable unit code
                illegalFieldPointers[0],// sense key specific data, only
                                        // report first problem
                new AdditionalSenseBytes());// additional sense bytes

            responsePdu = TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                false,// bidirectionalReadResidualUnderflow
                false,// residualOverflow
                false,// residualUnderflow,
                SCSIResponseParser.ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response,
                SCSIStatus.CHECK_CONDITION,// status,
                bhs.getInitiatorTaskTag(),// initiatorTaskTag,
                0,// snackTag
                0,// expectedDataSequenceNumber
                0,// bidirectionalReadResidualCount
                0,// residualCount
                new ScsiResponseDataSegment(senseData, parser.getExpectedDataTransferLength()));// data
                                                                                                // segment

        } else {
            // PDU is okay

            // carry out command
            /*
             * If we were nice, we would have to get (we would actually have to
             * save it first) the number of blocks and the block length
             * requested by the initiator in the last MODE SENSE command and
             * then change the logical block layout accordingly.
             * 
             * However, since the target is not required by the SCSI standard to
             * make those changes ("The degree that the medium is altered by
             * this command is vendor specific."), doing nothing is okay.
             */

            responsePdu = createScsiResponsePdu(SCSIStatus.GOOD,// status
                bhs.getInitiatorTaskTag(),// initiatorTaskTag,
                parser.getExpectedDataTransferLength(),// expectedDataTransferLength,
                0);// responseDataSize
        }

        // send response
        connection.sendPdu(responsePdu);
    }

}

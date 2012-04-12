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
import org.jscsi.target.scsi.cdb.SendDiagnosticCdb;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.FieldPointerSenseKeySpecificData;
import org.jscsi.target.settings.SettingsException;

/**
 * A stage for processing <code>SEND DIAGNOSTIC</code> SCSI commands.
 * <p>
 * Only support for the default self-test feature, as required by SPC-3, is implemented. Request for other
 * types of self-test operations will be declined.
 * 
 * @author Andreas Ergenzinger
 */
public class SendDiagnosticStage extends TargetFullFeatureStage {

    public SendDiagnosticStage(TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    @Override
    public void execute(ProtocolDataUnit pdu) throws IOException, InterruptedException,
        InternetSCSIException, DigestException, SettingsException {

        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final SCSICommandParser parser = (SCSICommandParser)bhs.getParser();

        ProtocolDataUnit responsePdu = null;// the response PDU

        // get command details in CDB
        final SendDiagnosticCdb cdb = new SendDiagnosticCdb(parser.getCDB());
        final FieldPointerSenseKeySpecificData[] illegalFieldPointers = cdb.getIllegalFieldPointers();

        if (illegalFieldPointers != null) {
            // an illegal request has been made

            responsePdu = createFixedFormatErrorPdu(illegalFieldPointers,// senseKeySpecificData
                bhs.getInitiatorTaskTag(),// initiatorTaskTag
                parser.getExpectedDataTransferLength());// expectedDataTransferLength

        } else {
            // PDU is okay
            // carry out command

            /*
             * The self-test bit is 1, since request of a non-default self-test
             * is not supported and would have led to illegalFieldPointer !=
             * null
             * 
             * A self-test (SELFTEST) bit set to one specifies that the device
             * server shall perform the logical unit default self-test. If the
             * self-test successfully passes, the command shall be terminated
             * with GOOD status. If the self-test fails, the command shall be
             * terminated with CHECK CONDITION status, with the sense key set to
             * HARDWARE ERROR.
             * 
             * The self-test is always successful.
             */
            responsePdu = TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                false,// bidirectionalReadResidualUnderflow
                false,// residualOverflow
                false,// residualUnderflow,
                SCSIResponseParser.ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response,
                SCSIStatus.GOOD,// status,
                bhs.getInitiatorTaskTag(),// initiatorTaskTag,
                0,// snackTag
                0,// expectedDataSequenceNumber
                0,// bidirectionalReadResidualCount
                0,// residualCount
                ScsiResponseDataSegment.EMPTY_DATA_SEGMENT);// data
                                                            // segment
        }

        // send response
        connection.sendPdu(responsePdu);
    }

}

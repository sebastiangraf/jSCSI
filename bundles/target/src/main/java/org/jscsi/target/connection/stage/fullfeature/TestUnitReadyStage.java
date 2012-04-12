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
import org.jscsi.target.scsi.cdb.TestUnitReadyCdb;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.FieldPointerSenseKeySpecificData;
import org.jscsi.target.settings.SettingsException;

/**
 * A stage for processing <code>TEST UNIT READY</code> SCSI commands.
 * 
 * @author Andreas Ergenzinger
 */
public class TestUnitReadyStage extends TargetFullFeatureStage {

    public TestUnitReadyStage(TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    @Override
    public void execute(ProtocolDataUnit pdu) throws IOException, InterruptedException,
        InternetSCSIException, DigestException, SettingsException {

        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final SCSICommandParser parser = (SCSICommandParser)bhs.getParser();

        ProtocolDataUnit responsePdu;// the response PDU

        // get command details in CDB
        final TestUnitReadyCdb cdb = new TestUnitReadyCdb(parser.getCDB());
        final FieldPointerSenseKeySpecificData[] illegalFieldPointers = cdb.getIllegalFieldPointers();

        if (illegalFieldPointers != null) {
            // an illegal request has been made

            responsePdu = createFixedFormatErrorPdu(illegalFieldPointers,// senseKeySpecificData,
                bhs.getInitiatorTaskTag(),// initiatorTaskTag,
                parser.getExpectedDataTransferLength());// expDataTransferLength

        } else {
            // PDU is okay
            // carry out command
            // the logical unit is always ready
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

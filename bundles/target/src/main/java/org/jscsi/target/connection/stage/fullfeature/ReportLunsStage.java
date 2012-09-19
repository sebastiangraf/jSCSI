package org.jscsi.target.connection.stage.fullfeature;

import java.io.IOException;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.scsi.cdb.ReportLunsCDB;
import org.jscsi.target.scsi.cdb.SelectReport;
import org.jscsi.target.scsi.lun.ReportLunsParameterData;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.FieldPointerSenseKeySpecificData;
import org.jscsi.target.settings.SettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A stage for processing <code>REPORT LUNS</code> SCSI commands.
 * 
 * @author Andreas Ergenzinger
 */
public class ReportLunsStage extends TargetFullFeatureStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportLunsStage.class);

    public ReportLunsStage(TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    @Override
    public void execute(ProtocolDataUnit pdu) throws IOException, InterruptedException,
        InternetSCSIException, DigestException, SettingsException {

        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final SCSICommandParser parser = (SCSICommandParser)bhs.getParser();

        ProtocolDataUnit responsePdu = null;// the response PDU

        // get command details in CDB
        final ReportLunsCDB cdb = new ReportLunsCDB(parser.getCDB());
        final FieldPointerSenseKeySpecificData[] illegalFieldPointers = cdb.getIllegalFieldPointers();

        if (illegalFieldPointers != null) {
            // an illegal request has been made
            responsePdu = createFixedFormatErrorPdu(illegalFieldPointers,// senseKeySpecificData
                bhs.getInitiatorTaskTag(),// initiatorTaskTag
                parser.getExpectedDataTransferLength());// expectedDataTransferLength

            // send response
            connection.sendPdu(responsePdu);

        } else {
            // PDU is okay
            // carry out command
            final SelectReport selectReport = cdb.getSelectReport();
            LOGGER.debug("selectReport = " + selectReport);

            // there are only well known LUNs
            ReportLunsParameterData reportLunsParameterData;

            // TODO the switch isn't really needed right now, but maybe in
            // future implementations
            switch (selectReport) {
            case SELECTED_ADDRESSING_METHODS:
            case WELL_KNOWN_LUNS_ONLY:
            case ALL:
                reportLunsParameterData =
                    new ReportLunsParameterData(session.getTargetServer().getConfig().getLogicalUnitNumber());
                break;
            default:
                throw new InternetSCSIException();
                /*
                 * Unreachable, this case has already been checked in the
                 * ReportLunsCDB constructor
                 */
            }

            // send response
            sendResponse(bhs.getInitiatorTaskTag(),// initiatorTaskTag
                parser.getExpectedDataTransferLength(),// expectedDataTransferLength
                reportLunsParameterData);// responseData

        }
    }

}

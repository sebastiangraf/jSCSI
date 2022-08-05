package org.jscsi.target.connection.stage.fullfeature;


import java.io.IOException;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.scsi.cdb.OpCodesCDB;
import org.jscsi.target.scsi.report.OneOpCode;
import org.jscsi.target.settings.SettingsException;
import org.jscsi.target.util.Debug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A stage for processing <code>REPORT SUPPORTED OPERATION CODES</code> SCSI commands.
 *
 * @author CHEN Qingcan
 */
public class ReportOpCodesStage extends TargetFullFeatureStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReportOpCodesStage.class);

    public ReportOpCodesStage (TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    @Override
    public void execute (ProtocolDataUnit pdu) throws IOException , InterruptedException , InternetSCSIException , DigestException , SettingsException {
        final BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        final SCSICommandParser parser = (SCSICommandParser) bhs.getParser();

        // get command details in CDB
        if (LOGGER.isDebugEnabled()) {// print CDB bytes
            LOGGER.debug("CDB bytes: \n" + Debug.byteBufferToString(parser.getCDB()));
        }
        final OpCodesCDB cdb = new OpCodesCDB(parser.getCDB());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("cdb.getAllocationLength() = " + cdb.getAllocationLength());
            LOGGER.debug("cdb.isNormalACA() = " + cdb.isNormalACA());
            LOGGER.debug("cdb.getReportingOptions() = " + cdb.getReportingOptions());
            LOGGER.debug("cdb.getRequestedOperationCode() = " + cdb.getRequestedOperationCode());
        }

        if (cdb.getReportingOptions() != 0b001) {
            throw new InternetSCSIException("Only REPORTING OPTIONS = 001b is supported now but request is " +
                                            cdb.getReportingOptions());
        }

        // send response
        sendResponse (
            bhs.getInitiatorTaskTag(),
            parser.getExpectedDataTransferLength(),
            new OneOpCode (cdb.getRequestedOperationCode())
        );
    }

}

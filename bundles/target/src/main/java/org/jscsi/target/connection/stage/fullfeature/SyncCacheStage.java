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
import org.jscsi.target.scsi.cdb.SyncCacheCDB;
import org.jscsi.target.settings.SettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A stage for processing <code>SYNCHRONIZE CACHE (10), (16)</code> SCSI commands.
 *
 * @author CHEN Qingcan
 */
public final class SyncCacheStage extends TargetFullFeatureStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(SyncCacheStage.class);

    public SyncCacheStage(TargetFullFeaturePhase targetFullFeaturePhase) {
        super (targetFullFeaturePhase);
    }

    @Override
    public void execute (ProtocolDataUnit pdu) throws IOException , DigestException , InterruptedException , InternetSCSIException , SettingsException {
        // get relevant values from PDU/CDB
        BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        SCSICommandParser parser = (SCSICommandParser) bhs.getParser();
        final int initiatorTaskTag = bhs.getInitiatorTaskTag();

        // get command details in CDB
        final SyncCacheCDB cdb = new SyncCacheCDB(parser.getCDB());
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("cdb.getLogicalBlockAddress() = " + cdb.getLogicalBlockAddress());
            LOGGER.debug("cdb.getNumberOfBlocks() = " + cdb.getNumberOfBlocks());
        }

        // synchronize cache
        session.getStorageModule().syncCahe (cdb.getLogicalBlockAddress(), cdb.getNumberOfBlocks());

        // send SCSI Response PDU
        pdu = TargetPduFactory.createSCSIResponsePdu(
            false,// bidirectionalReadResidualOverflow
            false,// bidirectionalReadResidualUnderflow
            false,// residualOverflow
            false,// residualUnderflow
            SCSIResponseParser.ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response
            SCSIStatus.GOOD,// status
            initiatorTaskTag, 0,// snackTag
            0,// (ExpDataSN or) Reserved
            0,// bidirectionalReadResidualCount
            0,// residualCount
            ScsiResponseDataSegment.EMPTY_DATA_SEGMENT);// dataSegment
        connection.sendPdu(pdu);
    }

}

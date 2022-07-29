package org.jscsi.target.connection.stage.fullfeature;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.scsi.SCSIResponseParser.ServiceResponse;
import org.jscsi.parser.scsi.SCSIStatus;
import org.jscsi.target.connection.TargetPduFactory;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.scsi.ScsiResponseDataSegment;
import org.jscsi.target.scsi.cdb.Read10Cdb;
import org.jscsi.target.scsi.cdb.Read12Cdb;
import org.jscsi.target.scsi.cdb.Read16Cdb;
import org.jscsi.target.scsi.cdb.Read6Cdb;
import org.jscsi.target.scsi.cdb.ReadCdb;
import org.jscsi.target.scsi.cdb.ScsiOperationCode;
import org.jscsi.target.settings.SettingsException;
import org.jscsi.target.util.Debug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A stage for processing <code>READ (6)</code> and <code>READ (10)</code> SCSI commands.
 *
 * @author Andreas Ergenzinger
 */
public class ReadStage extends ReadOrWriteStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadStage.class);

    public ReadStage (final TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    @Override
    public void execute (ProtocolDataUnit pdu) throws IOException , InterruptedException , InternetSCSIException , SettingsException {

        // get relevant variables ...
        // ... from settings
        final boolean immediateData = settings.getImmediateData();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("immediateData = " + immediateData);
            LOGGER.debug("maxRecvDataSegmentLength = " + settings.getMaxRecvDataSegmentLength());
        }

        // ... and from the PDU
        BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        SCSICommandParser parser = (SCSICommandParser) bhs.getParser();
        final int initiatorTaskTag = bhs.getInitiatorTaskTag();

        // get the Read(6) or Read(10) CDB
        ReadCdb cdb;
        final ScsiOperationCode scsiOpCode = ScsiOperationCode.valueOf(parser.getCDB().get(0));
        if (scsiOpCode == ScsiOperationCode.READ_16)// most likely option first
            cdb = new Read16Cdb(parser.getCDB());
        else if (scsiOpCode == ScsiOperationCode.READ_12)
            cdb = new Read12Cdb(parser.getCDB());
        else if (scsiOpCode == ScsiOperationCode.READ_10)
            cdb = new Read10Cdb(parser.getCDB());
        else if (scsiOpCode == ScsiOperationCode.READ_6)
            cdb = new Read6Cdb(parser.getCDB());
        else {
            // anything else wouldn't be good (programmer error)
            // close connection
            throw new InternetSCSIException("wrong SCSI Operation Code " + scsiOpCode + " in ReadStage");
        }

        // check if requested blocks are out of bounds
        checkOverAndUnderflow(cdb);

        // check illegal field pointers
        if (cdb.getIllegalFieldPointers() != null) {
            // the command must fail

            LOGGER.error("illegal field in Read CDB");
            LOGGER.error("CDB:\n" + Debug.byteBufferToString(parser.getCDB()));

            // create and send error PDU and leave stage
            final ProtocolDataUnit responsePdu = createFixedFormatErrorPdu(cdb.getIllegalFieldPointers(),// senseKeySpecificData
                    initiatorTaskTag, parser.getExpectedDataTransferLength());
            connection.sendPdu(responsePdu);
            return;
        }

        final int totalTransferLength = session.getStorageModule().getBlockSize() * cdb.getTransferLength();
        final long storageOffset = session.getStorageModule().getBlockSize() * cdb.getLogicalBlockAddress();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("cdb.getLogicalBlockAddress() = " + cdb.getLogicalBlockAddress());
            LOGGER.debug("blockSize = " + session.getStorageModule().getBlockSize());
            LOGGER.debug("totalTransferLength = " + totalTransferLength);
            LOGGER.debug("expectedDataSegmentLength = " + parser.getExpectedDataTransferLength());
        }

        // *** start sending ***
        // initialize counters and data segment buffer
        int bytesSent = 0;
        int dataSequenceNumber = 0;
        byte[] dataSegmentArray = null;
        ByteBuffer dataSegment = null;
        ProtocolDataUnit responsePdu;

        // *** send up to last but one Data-In PDU ***
        // (with DataSegmentSize == MaxRecvDataSegmentLength)

        if (bytesSent < totalTransferLength - settings.getMaxRecvDataSegmentLength()) {
            /*
             * Initialize dataSegmentArray and dataSegment with MaxRecvDataSegmentLength bytes.
             */
            dataSegmentArray = connection.getDataInArray(settings.getMaxRecvDataSegmentLength());
            dataSegment = ByteBuffer.wrap(dataSegmentArray);
        }

        while (bytesSent < totalTransferLength - settings.getMaxRecvDataSegmentLength()) {

            // get data and prepare data segment
            session.getStorageModule().read(dataSegmentArray, storageOffset + bytesSent);

            // create and send PDU
            responsePdu = TargetPduFactory.createDataInPdu(false,// finalFlag,
                                                                 // not the last
                                                                 // PDU with
                                                                 // data payload
                                                                 // in the
                                                                 // sequence
                    false,// acknowledgeFlag, ErrorRecoveryLevel == 0, so we
                          // never do that
                    false,// residualOverflowFlag
                    false,// residualUnderflowFlag
                    false,// statusFlag
                    SCSIStatus.GOOD,// status, actually reserved i.e. 0x0
                    0L,// logicalUnitNumber, reserved
                    initiatorTaskTag, 0xffffffff,// targetTransferTag
                    dataSequenceNumber,// dataSequenceNumber
                    bytesSent,// bufferOffset
                    0,// residualCount
                    dataSegment);

            connection.sendPdu(responsePdu);

            // increment counters
            ++dataSequenceNumber;
            bytesSent += settings.getMaxRecvDataSegmentLength();
        }

        /*
         * If ImmediateData=Yes has been negotiated, then a phase collapse has to take place, i.e. the status is sent in
         * the last Data-In PDU. Otherwise a separate SCSI Response PDU must follow.
         */

        // *** send last Data-In PDU ***

        // get data and prepare data segment
        final int bytesRemaining = totalTransferLength - bytesSent;
        dataSegmentArray = connection.getDataInArray(bytesRemaining);
        session.getStorageModule().read(dataSegmentArray, storageOffset + bytesSent);
        dataSegment = ByteBuffer.wrap(dataSegmentArray);

        // create and send PDU (with or without status)
        responsePdu = TargetPduFactory.createDataInPdu(true,// finalFlag, last
                                                            // PDU in the
                                                            // sequence with
                                                            // data payload
                false,// acknowledgeFlag, ErrorRecoveryLevel == 0, so we never
                      // do that
                false,// residualOverflowFlag
                false,// residualUnderflowFlag
                immediateData,// statusFlag
                SCSIStatus.GOOD,// status, or not (reserved if no status)
                0L,// logicalUnitNumber, reserved
                initiatorTaskTag, 0xffffffff,// targetTransferTag
                dataSequenceNumber,// dataSequenceNumber
                bytesSent,// bufferOffset
                0,// residualCount
                dataSegment);

        LOGGER.debug("sending last Data-In PDU");
        connection.sendPdu(responsePdu);

        // send SCSI Response PDU?
        if (!immediateData) {

            responsePdu = TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
                    false,// bidirectionalReadResidualUnderflow
                    false,// residualOverflow
                    false,// residualUnderflow
                    ServiceResponse.COMMAND_COMPLETED_AT_TARGET,// response
                    SCSIStatus.GOOD,// status
                    initiatorTaskTag,// initiatorTaskTag
                    0,// snackTag, reserved
                    0,// expectedDataSequenceNumber, reserved
                    0,// bidirectionalReadResidualCount
                    0,// residualCount
                    ScsiResponseDataSegment.EMPTY_DATA_SEGMENT);// empty
                                                                // ScsiResponseDataSegment

            LOGGER.debug("sending SCSI Response PDU");
            connection.sendPdu(responsePdu);
        }

    }

}

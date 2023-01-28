package org.jscsi.target.connection.stage.fullfeature;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.AbstractMessageParser;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.data.DataOutParser;
import org.jscsi.parser.nop.NOPOutParser;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.scsi.SCSIResponseParser;
import org.jscsi.parser.scsi.SCSIStatus;
import org.jscsi.parser.tmf.TaskManagementFunctionRequestParser;
import org.jscsi.target.TargetServer;
import org.jscsi.target.connection.TargetPduFactory;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.connection.stage.TMStage;
import org.jscsi.target.scsi.ScsiResponseDataSegment;
import org.jscsi.target.scsi.cdb.ScsiOperationCode;
import org.jscsi.target.scsi.cdb.Write10Cdb;
import org.jscsi.target.scsi.cdb.Write12Cdb;
import org.jscsi.target.scsi.cdb.Write16Cdb;
import org.jscsi.target.scsi.cdb.Write6Cdb;
import org.jscsi.target.scsi.cdb.WriteCdb;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.FieldPointerSenseKeySpecificData;
import org.jscsi.target.settings.SettingsException;
import org.jscsi.target.util.Debug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A stage for processing <code>WRITE (6), (10), (12), (16)</code> SCSI commands.
 *
 * @author Andreas Ergenzinger
 * @author CHEN Qingcan
 */
public final class WriteStage extends ReadOrWriteStage {

    private static final Logger LOGGER = LoggerFactory.getLogger(WriteStage.class);

    public WriteStage (TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    private Queue<ProtocolDataUnit> queueWritePDU = new LinkedList<>();
    private boolean logQueueWrite = false;

    private static enum Checked { NORMAL, SKIP, ABORT }
    /**
     * Is used for checking if the PDUs received in a Data-Out sequence actually are Data-Out PDU and if the PDUs have
     * been received in order.
     */
    private Checked checkDataOutParser (
        final ProtocolDataUnit pdu,
        final int expectedInitiatorTaskTag,
        final int expectedDataSequenceNumber
    )
    throws DigestException, IOException, InterruptedException, InternetSCSIException, SettingsException {

        BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        AbstractMessageParser parser = bhs.getParser();

        if (parser instanceof DataOutParser) {
            if (bhs.getInitiatorTaskTag() != expectedInitiatorTaskTag) {
                LOGGER.error (String.format ("received erroneous PDU Initiator Task Tag in data-out sequence, got %d, expected %d, abort write stage.",
                    bhs.getInitiatorTaskTag(), expectedInitiatorTaskTag));
                return Checked.ABORT;
            }
            int gotDataSequenceNumber = ((DataOutParser) parser).getDataSequenceNumber();
            if (gotDataSequenceNumber != expectedDataSequenceNumber) {
                LOGGER.error (String.format ("received erroneous PDU Data Sequence Number in data-out sequence, got %d, expected %d, abort write stage.",
                    gotDataSequenceNumber, expectedDataSequenceNumber));
                return Checked.ABORT;
            }
            return Checked.NORMAL;

        } else if (parser instanceof NOPOutParser) {
            new PingStage((TargetFullFeaturePhase) targetPhase).execute (pdu);
            return Checked.SKIP;

        } else if (parser instanceof TaskManagementFunctionRequestParser) {
            new TMStage((TargetFullFeaturePhase) targetPhase).execute (pdu);
            return Checked.SKIP;

        } else if (parser instanceof SCSICommandParser) {
            ByteBuffer cdb = ((SCSICommandParser) parser).getCDB();
            LOGGER.info ("received another SCSI command in data-out sequence:\n{}", Debug.byteBufferToString(cdb));

            byte scsiOpByte = cdb.get(0);
            ScsiOperationCode scsiOpCode = ScsiOperationCode.valueOf(scsiOpByte);

            if (scsiOpCode == ScsiOperationCode.WRITE_16 ||
                scsiOpCode == ScsiOperationCode.WRITE_12 ||
                scsiOpCode == ScsiOperationCode.WRITE_10 ||
                scsiOpCode == ScsiOperationCode.WRITE_6) {
                queueWritePDU.add (pdu);
                LOGGER.info ("put another write PDU into pending queue (size = {}).", queueWritePDU.size ());
                return Checked.SKIP;
            }

            LOGGER.error ("received unsupport SCSI command {} in data-out sequence, abort write stage.", scsiOpCode);
            return Checked.ABORT;
        }

        if (parser != null) {
            final String parserName = parser.getClass().getName();
            LOGGER.error ("received erroneous PDU in data-out sequence, abort write stage. {}:\n{}",
                parserName, parser.toString ());
        } else {
            LOGGER.error ("received erroneous PDU in data-out sequence, parser is null, abort write stage.");
        }
        return Checked.ABORT;
    }

    @Override
    public void execute (ProtocolDataUnit pdu)
    throws IOException , DigestException , InterruptedException , InternetSCSIException , SettingsException {
        LOGGER.debug("Entering WRITE STAGE");

        // get relevant values from settings
        final boolean immediateData = settings.getImmediateData();
        final boolean initialR2T = settings.getInitialR2T();
        final int firstBurstLength = settings.getFirstBurstLength();
        final int maxBurstLength = settings.getMaxBurstLength();

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("immediateData = " + immediateData);
            LOGGER.debug("initialR2T = " + initialR2T);
        }

        // get relevant values from PDU/CDB
        BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();
        SCSICommandParser parser = (SCSICommandParser) bhs.getParser();
        final int initiatorTaskTag = bhs.getInitiatorTaskTag();
        WriteCdb cdb;
        final ScsiOperationCode scsiOpCode = ScsiOperationCode.valueOf(parser.getCDB().get(0));
        if (scsiOpCode == ScsiOperationCode.WRITE_16)
            cdb = new Write16Cdb(parser.getCDB());
        else if (scsiOpCode == ScsiOperationCode.WRITE_12)
            cdb = new Write12Cdb(parser.getCDB());
        else if (scsiOpCode == ScsiOperationCode.WRITE_10)
            cdb = new Write10Cdb(parser.getCDB());
        else if (scsiOpCode == ScsiOperationCode.WRITE_6)
            cdb = new Write6Cdb(parser.getCDB());
        else {
            // anything else wouldn't be good (programmer error)
            // close connection
            throw new InternetSCSIException("wrong SCSI Operation Code " + scsiOpCode + " in WriteStage");
        }
        final int transferLength = cdb.getTransferLength();
        final long logicalBlockAddress = cdb.getLogicalBlockAddress();
        final ByteBuffer originalCDB = parser.getCDB();

        // transform to from block units to byte units
        final int transferLengthInBytes = transferLength * session.getStorageModule().getBlockSize();
        long storageIndex = logicalBlockAddress * session.getStorageModule().getBlockSize();
        byte[] buffer = new byte [transferLengthInBytes];

        // check if requested blocks are out of bounds
        // (might add FPSKSD to the CDB's list to be detected in the next step)
        checkOverAndUnderflow(cdb);

        if (cdb.getIllegalFieldPointers() != null) {
            /*
             * CDB is invalid, inform initiator by closing the connection. Sending an error status SCSI Response PDU
             * will not work reliably, since the initiator may not be expecting a response so soon. Also, if the
             * WriteStage is simply left early (without closing the connection), the initiator may send additional
             * unsolicited Data-Out PDUs, which the jSCSI Target is currently unable to ignore or process properly.
             */
            LOGGER.error("illegal field in Write CDB");
            for (FieldPointerSenseKeySpecificData field1 : cdb.getIllegalFieldPointers()) {
                LOGGER.error("  field pointer = {} bit pointer = {}",
                    field1.getFieldPointer (), field1.getBitPointer ());
            }
            LOGGER.error("CDB:\n" + Debug.byteBufferToString(parser.getCDB()));

            // Not necessarily close the connection

            // create and send error PDU and leave stage
            final ProtocolDataUnit responsePdu = createFixedFormatErrorPdu(cdb.getIllegalFieldPointers(),// senseKeySpecificData
                    initiatorTaskTag, parser.getExpectedDataTransferLength());
            connection.sendPdu(responsePdu);
            return;
        }

        // *** start receiving data (or process what has already been sent) ***
        int bytesReceived = 0;

        // *** receive immediate data ***
        if (immediateData && bhs.getDataSegmentLength() > 0) {
            final byte[] immediateDataArray = pdu.getDataSegment().array();

            System.arraycopy (immediateDataArray, 0, buffer, 0, immediateDataArray.length);
            bytesReceived = immediateDataArray.length;
        }

        // *** receive unsolicited data ***
        if (!initialR2T && !bhs.isFinalFlag()) {

            if (LOGGER.isDebugEnabled()) LOGGER.debug("receiving unsolicited data");

            boolean firstBurstOver = false;
            while (!firstBurstOver && bytesReceived < firstBurstLength) {

                // receive and check PDU
                pdu = connection.receivePdu();
                switch (checkDataOutParser(pdu, initiatorTaskTag, 0)) {
                case SKIP:
                    LOGGER.info ("context before unsolicited skip:\n  storageIndex: {}\n  transferLengthInBytes: {}\n  firstBurstLength: {}\n  bytesReceived: {}\n  originalCDB:\n{}",
                        storageIndex, transferLengthInBytes, firstBurstLength, bytesReceived, Debug.byteBufferToString(originalCDB));
                    continue;
                case ABORT:
                    LOGGER.error ("context before unsolicited abort:\n  storageIndex: {}\n  transferLengthInBytes: {}\n  firstBurstLength: {}\n  bytesReceived: {}\n  originalCDB:\n{}",
                        storageIndex, transferLengthInBytes, firstBurstLength, bytesReceived, Debug.byteBufferToString(originalCDB));
                    throw new InternetSCSIException ("checkDataOutParser: ABORT");
                default:
                    /* NORMAL */
                }

                bhs = pdu.getBasicHeaderSegment();
                AbstractMessageParser aParser = bhs.getParser();
                int offsetDataOut = ((DataOutParser) aParser).getBufferOffset();
                System.arraycopy (pdu.getDataSegment().array(), 0, buffer, offsetDataOut, bhs.getDataSegmentLength());
                bytesReceived += bhs.getDataSegmentLength();

                if (bhs.isFinalFlag()) firstBurstOver = true;
            }
        }

        // *** receive solicited data ***
        boolean isFinalFlag = false;
        if (!isFinalFlag && bytesReceived < transferLengthInBytes) {
            if (LOGGER.isDebugEnabled()) LOGGER.debug(bytesReceived + "<" + transferLengthInBytes);

            int readyToTransferSequenceNumber = 0;
            int desiredDataTransferLength;

            while (bytesReceived < transferLengthInBytes) {

                desiredDataTransferLength = Math.min(maxBurstLength, transferLengthInBytes - bytesReceived);
                int targetTransferTag = TargetServer.getNextTargetTransferTag();

                // send R2T
                pdu = TargetPduFactory.createReadyToTransferPdu(0,// logicalUnitNumber
                        initiatorTaskTag, targetTransferTag,// targetTransferTag
                        readyToTransferSequenceNumber++, bytesReceived,// bufferOffset
                        desiredDataTransferLength);

                connection.sendPdu(pdu);

                // receive DataOut PDUs
                /**
                 * The <code>DataSN</code> value the next Data-Out PDU must carry.
                 */
                int expectedDataSequenceNumber = 0;

                boolean solicitedDataCycleOver = false;
                int bytesReceivedThisCycle = 0;
                while (!solicitedDataCycleOver && bytesReceivedThisCycle < desiredDataTransferLength) {

                    // receive and check PDU
                    pdu = connection.receivePdu();
                    switch (checkDataOutParser(pdu, initiatorTaskTag, expectedDataSequenceNumber)) {
                    case SKIP:
                        LOGGER.info ("context before solicited skip:\n  storageIndex: {}\n  transferLengthInBytes: {}\n  maxBurstLength: {}\n  bytesReceived: {}\n  desiredDataTransferLength: {}\n  readyToTransferSequenceNumber: {}\n  targetTransferTag: {}\n  expectedDataSequenceNumber: {}\n  bytesReceivedThisCycle: {}\n  originalCDB:\n{}",
                            storageIndex, transferLengthInBytes, maxBurstLength, bytesReceived, desiredDataTransferLength, readyToTransferSequenceNumber, targetTransferTag, expectedDataSequenceNumber, bytesReceivedThisCycle, Debug.byteBufferToString(originalCDB));
                        continue;
                    case ABORT:
                        LOGGER.warn ("context before solicited abort:\n  storageIndex: {}\n  transferLengthInBytes: {}\n  maxBurstLength: {}\n  bytesReceived: {}\n  desiredDataTransferLength: {}\n  readyToTransferSequenceNumber: {}\n  targetTransferTag: {}\n  expectedDataSequenceNumber: {}\n  bytesReceivedThisCycle: {}\n  originalCDB:\n{}",
                            storageIndex, transferLengthInBytes, maxBurstLength, bytesReceived, desiredDataTransferLength, readyToTransferSequenceNumber, targetTransferTag, expectedDataSequenceNumber, bytesReceivedThisCycle, Debug.byteBufferToString(originalCDB));
                        throw new InternetSCSIException ("checkDataOutParser: ABORT");
                    default:
                        /* NORMAL */
                        expectedDataSequenceNumber++;
                    }

                    bhs = pdu.getBasicHeaderSegment();
                    AbstractMessageParser aParser = bhs.getParser();
                    int offsetDataOut = ((DataOutParser) aParser).getBufferOffset();
                    System.arraycopy (pdu.getDataSegment().array(), 0, buffer, offsetDataOut, bhs.getDataSegmentLength());
                    bytesReceivedThisCycle += bhs.getDataSegmentLength();

                    /*
                     * Checking the final flag should be enough, but is not, when dealing with the jSCSI Initiator.
                     * This is also one of the reasons, why the contents of this while loop, though very similar to
                     * what is happening during the receiving of the unsolicited data PDU sequence, has not been put
                     * into a dedicated method.
                     */
                    if (bhs.isFinalFlag()) {
                        solicitedDataCycleOver = true;
                        isFinalFlag = true;
                    }
                }
                bytesReceived += bytesReceivedThisCycle;
            }
        }

        if (! queueWritePDU.isEmpty()) {
            logQueueWrite = true;
        }
        if (logQueueWrite) {
            LOGGER.info ("queue writing:\n  queueWritePDU: {}\n  logicalBlockAddress: {}\n  transferLength: {}\n   bytesReceived: {}\n",
                queueWritePDU.size(), logicalBlockAddress, transferLength, bytesReceived);
        }

        if (bytesReceived < transferLengthInBytes) {
            LOGGER.warn ("bytesReceived ({}) < transferLengthInBytes ({})", bytesReceived, transferLengthInBytes);
            buffer = Arrays.copyOf (buffer, bytesReceived);
        }
        session.getStorageModule().write(buffer, storageIndex);

        /* send SCSI Response PDU */
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

        while (! queueWritePDU.isEmpty ()) {
            LOGGER.info ("processing pending WRITE PDU queue (size = {})", queueWritePDU.size ());
            execute (queueWritePDU.remove ());
        }
    }
}

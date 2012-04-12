package org.jscsi.target.connection.stage.fullfeature;

import java.io.IOException;
import java.security.DigestException;

import org.apache.log4j.Logger;
import org.jscsi.parser.AbstractMessageParser;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.data.DataOutParser;
import org.jscsi.parser.exception.InternetSCSIException;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.parser.scsi.SCSIResponseParser;
import org.jscsi.parser.scsi.SCSIStatus;
import org.jscsi.target.TargetServer;
import org.jscsi.target.connection.TargetPduFactory;
import org.jscsi.target.connection.phase.TargetFullFeaturePhase;
import org.jscsi.target.scsi.ScsiResponseDataSegment;
import org.jscsi.target.scsi.cdb.ScsiOperationCode;
import org.jscsi.target.scsi.cdb.Write10Cdb;
import org.jscsi.target.scsi.cdb.Write6Cdb;
import org.jscsi.target.scsi.cdb.WriteCdb;
import org.jscsi.target.settings.SettingsException;
import org.jscsi.target.util.Debug;

/**
 * A stage for processing <code>WRITE (6)</code> and <code>WRITE (10)</code>
 * SCSI commands.
 * 
 * @author Andreas Ergenzinger
 */
public final class WriteStage extends ReadOrWriteStage {

    private static final Logger LOGGER = Logger.getLogger(WriteStage.class);

    /**
     * The <code>DataSN</code> value the next Data-Out PDU must carry.
     */
    private int expectedDataSequenceNumber = 0;

    public WriteStage(TargetFullFeaturePhase targetFullFeaturePhase) {
        super(targetFullFeaturePhase);
    }

    /**
     * Is used for checking if the PDUs received in a Data-Out sequence actually
     * are Data-Out PDU and if the PDUs have been received in order.
     * 
     * @param parser
     *            the {@link AbstractMessageParser} subclass instance retrieved
     *            from the {@link ProtocolDataUnit}'s {@link BasicHeaderSegment}
     * @throws InternetSCSIException
     *             if an unexpected PDU has been received
     */
    private void checkDataOutParser(final AbstractMessageParser parser)
            throws InternetSCSIException {
        if (parser instanceof DataOutParser) {
            final DataOutParser p = (DataOutParser) parser;
            if (p.getDataSequenceNumber() == expectedDataSequenceNumber++)
                return;
        }
        throw new InternetSCSIException(
                "received erroneous PDU in data-out sequence");
    }

    @Override
    public void execute(ProtocolDataUnit pdu) throws IOException,
            DigestException, InterruptedException, InternetSCSIException,
            SettingsException {

        if (LOGGER.isDebugEnabled())
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
        final ScsiOperationCode scsiOpCode = ScsiOperationCode.valueOf(parser
                .getCDB().get(0));
        if (scsiOpCode == ScsiOperationCode.WRITE_10)
            cdb = new Write10Cdb(parser.getCDB());
        else if (scsiOpCode == ScsiOperationCode.WRITE_6)
            cdb = new Write6Cdb(parser.getCDB());
        else {
            // anything else wouldn't be good (programmer error)
            // close connection
            throw new InternetSCSIException("wrong SCSI Operation Code "
                    + scsiOpCode + " in WriteStage");
        }
        final int transferLength = cdb.getTransferLength();
        final long logicalBlockAddress = cdb.getLogicalBlockAddress();

        // transform to from block units to byte units
        final int blockSize = session.getStorageModule().getBlockSizeInBytes();
        final int transferLengthInBytes = transferLength * blockSize;
        long storageIndex = logicalBlockAddress * blockSize;

        // check if requested blocks are out of bounds
        // (might add FPSKSD to the CDB's list to be detected in the next step)
        checkOverAndUnderflow(cdb);

        if (cdb.getIllegalFieldPointers() != null) {
            /*
             * CDB is invalid, inform initiator by closing the connection.
             * 
             * Sending an error status SCSI Response PDU will not work reliably,
             * since the initiator may not be expecting a response so soon.
             * Also, if the WriteStage is simply left early (without closing the
             * connection), the initiator may send additional unsolicited
             * Data-Out PDUs, which the jSCSI Target is currently unable to
             * ignore or process properly.
             */
            LOGGER.error("illegal field in Write CDB");
            LOGGER.error("CDB:\n" + Debug.byteBufferToString(parser.getCDB()));
            throw new InternetSCSIException();// leads to connection closing
        }

        // *** start receiving data (or process what has already been sent) ***
        int bytesReceived = 0;

        // *** receive immediate data ***
        if (immediateData && bhs.getDataSegmentLength() > 0) {
            final byte[] immediateDataArray = pdu.getDataSegment().array();

            session.getStorageModule().write(immediateDataArray, storageIndex);
            bytesReceived = immediateDataArray.length;

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("wrote " + immediateDataArray.length
                        + "bytes as immediate data");
        }

        // *** receive unsolicited data ***
        if (!initialR2T && !bhs.isFinalFlag()) {

            if (LOGGER.isDebugEnabled())
                LOGGER.debug("receiving unsolicited data");

            boolean firstBurstOver = false;
            while (!firstBurstOver && bytesReceived <= firstBurstLength) {

                // receive and check PDU
                pdu = connection.receivePdu();
                bhs = pdu.getBasicHeaderSegment();

                checkDataOutParser(bhs.getParser());

                final DataOutParser dataOutParser = (DataOutParser) bhs
                        .getParser();

                session.getStorageModule().write(pdu.getDataSegment().array(),// source
                        storageIndex + dataOutParser.getBufferOffset());// destination
                                                                        // index
                bytesReceived += bhs.getDataSegmentLength();

                if (bhs.isFinalFlag())
                    firstBurstOver = true;
            }
        }

        // *** receive solicited data ***
        if (bytesReceived < transferLengthInBytes) {
            if (LOGGER.isDebugEnabled())
                LOGGER.debug(bytesReceived + "<" + transferLengthInBytes);

            int readyToTransferSequenceNumber = 0;
            int desiredDataTransferLength;

            while (bytesReceived < transferLengthInBytes) {

                desiredDataTransferLength = Math.min(maxBurstLength,
                        transferLengthInBytes - bytesReceived);

                // send R2T
                pdu = TargetPduFactory.createReadyToTransferPdu(0,// logicalUnitNumber
                        initiatorTaskTag, TargetServer.getNextTargetTransferTag(),// targetTransferTag
                        readyToTransferSequenceNumber++, bytesReceived,// bufferOffset
                        desiredDataTransferLength);

                connection.sendPdu(pdu);

                // receive DataOut PDUs
                expectedDataSequenceNumber = 0;// reset sequence counter//FIXME
                                               // fix in jSCSI Initiator
                boolean solicitedDataCycleOver = false;
                int bytesReceivedThisCycle = 0;
                while (!solicitedDataCycleOver) {

                    // receive and check PDU
                    pdu = connection.receivePdu();
                    bhs = pdu.getBasicHeaderSegment();
                    checkDataOutParser(bhs.getParser());

                    final DataOutParser dataOutParser = (DataOutParser) bhs
                            .getParser();

                    session.getStorageModule().write(pdu.getDataSegment().array(),// source
                            storageIndex + dataOutParser.getBufferOffset());// destination
                                                                            // index
                    bytesReceivedThisCycle += bhs.getDataSegmentLength();

                    /*
                     * Checking the final flag should be enough, but is not,
                     * when dealing with the jSCSI Initiator. This is also one
                     * of the reasons, why the contents of this while loop,
                     * though very similar to what is happening during the
                     * receiving of the unsolicited data PDU sequence, has not
                     * been put into a dedicated method.
                     */
                    if (bhs.isFinalFlag()
                            || bytesReceivedThisCycle >= desiredDataTransferLength)
                        solicitedDataCycleOver = true;
                }
                bytesReceived += bytesReceivedThisCycle;
            }
        }

        /* send SCSI Response PDU */
        pdu = TargetPduFactory.createSCSIResponsePdu(false,// bidirectionalReadResidualOverflow
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

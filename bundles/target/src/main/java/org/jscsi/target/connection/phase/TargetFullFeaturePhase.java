package org.jscsi.target.connection.phase;

import java.io.IOException;
import java.security.DigestException;

import javax.naming.OperationNotSupportedException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.BasicHeaderSegment;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.connection.stage.fullfeature.FormatUnitStage;
import org.jscsi.target.connection.stage.fullfeature.InquiryStage;
import org.jscsi.target.connection.stage.fullfeature.LogoutStage;
import org.jscsi.target.connection.stage.fullfeature.ModeSenseStage;
import org.jscsi.target.connection.stage.fullfeature.PingStage;
import org.jscsi.target.connection.stage.fullfeature.ReadCapacityStage;
import org.jscsi.target.connection.stage.fullfeature.ReadStage;
import org.jscsi.target.connection.stage.fullfeature.ReportLunsStage;
import org.jscsi.target.connection.stage.fullfeature.RequestSenseStage;
import org.jscsi.target.connection.stage.fullfeature.SendDiagnosticStage;
import org.jscsi.target.connection.stage.fullfeature.TargetFullFeatureStage;
import org.jscsi.target.connection.stage.fullfeature.TestUnitReadyStage;
import org.jscsi.target.connection.stage.fullfeature.TextNegotiationStage;
import org.jscsi.target.connection.stage.fullfeature.UnsupportedOpCodeStage;
import org.jscsi.target.connection.stage.fullfeature.WriteStage;
import org.jscsi.target.scsi.cdb.ScsiOperationCode;
import org.jscsi.target.settings.SettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Objects of this class represent the Target Full Feature Phase of a
 * connection.
 * 
 * @see TargetPhase
 * @author Andreas Ergenzinger
 */
public final class TargetFullFeaturePhase extends TargetPhase {

    private static final Logger LOGGER = LoggerFactory.getLogger(TargetFullFeaturePhase.class);

    /**
     * The current stage of this phase.
     */
    private TargetFullFeatureStage stage;

    /**
     * While this variable is <code>true</code> the phase is still running,
     * either executing a specific stage or waiting for the next one to begin.
     */
    private boolean running;

    /**
     * The constructor.
     * 
     * @param connection
     *            {@inheritDoc}
     */
    public TargetFullFeaturePhase(Connection connection) {
        super(connection);
    }

    /**
     * Starts the full feature phase.
     * 
     * @return {@inheritDoc}
     * @throws OperationNotSupportedException
     *             {@inheritDoc}
     * @throws IOException
     *             {@inheritDoc}
     * @throws InterruptedException
     *             {@inheritDoc}
     * @throws InternetSCSIException
     *             {@inheritDoc}
     * @throws DigestException
     *             {@inheritDoc}
     * @throws SettingsException
     *             {@inheritDoc}
     */
    public boolean execute() throws DigestException, IOException, InterruptedException,
        InternetSCSIException, SettingsException {

        running = true;
        while (running) {

            ProtocolDataUnit pdu = connection.receivePdu();
            BasicHeaderSegment bhs = pdu.getBasicHeaderSegment();

            // identify desired stage
            switch (bhs.getOpCode()) {

            case SCSI_COMMAND:
                if (connection.getTargetSession().isNormalSession()) {
                    final SCSICommandParser parser = (SCSICommandParser)bhs.getParser();
                    ScsiOperationCode scsiOpCode = ScsiOperationCode.valueOf(parser.getCDB().get(0));

                    LOGGER.debug("scsiOpCode = " + scsiOpCode);// log SCSI
                                                               // Operation Code

                    if (scsiOpCode != null) {
                        switch (scsiOpCode) {
                        case TEST_UNIT_READY:
                            stage = new TestUnitReadyStage(this);
                            break;
                        case REQUEST_SENSE:
                            stage = new RequestSenseStage(this);
                            break;
                        case FORMAT_UNIT:
                            stage = new FormatUnitStage(this);
                            break;
                        case INQUIRY:
                            stage = new InquiryStage(this);
                            break;
                        case MODE_SELECT_6:
                            stage = null;
                            scsiOpCode = null;
                            break;
                        case MODE_SENSE_6:
                            stage = new ModeSenseStage(this);
                            if (!((ModeSenseStage)stage).canHandle(pdu)) {
                                stage = null;
                                scsiOpCode = null;
                            }
                            break;
                        case SEND_DIAGNOSTIC:
                            stage = new SendDiagnosticStage(this);
                            break;
                        case READ_CAPACITY_10:// use common read capacity stage
                        case READ_CAPACITY_16:
                            stage = new ReadCapacityStage(this);
                            break;
                        case WRITE_6:// use common write stage
                        case WRITE_10:
                            stage = new WriteStage(this);
                            break;
                        case READ_6:// use common read stage
                        case READ_10:
                            stage = new ReadStage(this);
                            break;
                        case REPORT_LUNS:
                            stage = new ReportLunsStage(this);
                            break;
                        default:
                            scsiOpCode = null;

                        }
                    }// else, or if default block was entered (programmer error)
                    if (scsiOpCode == null) {
                        LOGGER.error("Unsupported SCSI OpCode 0x"
                            + Integer.toHexString(parser.getCDB().get(0) & 255) + " in SCSI Command PDU.");
                        stage = new UnsupportedOpCodeStage(this);
                    }

                } else {// session is discovery session
                    throw new InternetSCSIException("received SCSI command in discovery session");
                }
                break; // SCSI_COMMAND

            case NOP_OUT:
                stage = new PingStage(this);
                break;
            case TEXT_REQUEST:
                stage = new TextNegotiationStage(this);
                break;
            case LOGOUT_REQUEST:
                stage = new LogoutStage(this);
                running = false;
                break;
            default:
                throw new InternetSCSIException();
            }

            // process the PDU
            stage.execute(pdu);
        }
        return false;
    }
}

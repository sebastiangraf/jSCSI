package org.jscsi.target.connection.stage;

import java.io.IOException;
import java.security.DigestException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.Connection;
import org.jscsi.target.connection.TargetSession;
import org.jscsi.target.connection.phase.TargetPhase;
import org.jscsi.target.settings.Settings;
import org.jscsi.target.settings.SettingsException;

/**
 * This class is an abstract super-class for stages of the (see {@link Connection} for a description of
 * the relationship between
 * sessions, connections, phases, and sessions).
 * <p>
 * The stage is started by calling the {@link #execute(ProtocolDataUnit)} method with the first
 * {@link ProtocolDataUnit} to be processed as part of the stage.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class TargetStage {

    /**
     * The phase this stage is a part of.
     */
    protected final TargetPhase targetPhase;

    /**
     * The connection the {@link org.jscsi.target.connection.phase.TargetFullFeaturePhase} is a part of
     */
    protected final Connection connection;

    /**
     * The session the {@link #connection} is a part of.
     */
    protected final TargetSession session;

    /**
     * The current {@link Settings} of {@link #connection}.
     */
    protected final Settings settings;

    /**
     * The abstract constructor.
     * 
     * @param targetPhase
     *            the phase this stage is a part of
     */
    public TargetStage(TargetPhase targetPhase) {
        this.targetPhase = targetPhase;
        this.connection = targetPhase.getTargetConnection();
        this.session = connection.getTargetSession();
        this.settings = connection.getSettings();
    }

    /**
     * Starts the stage. This method contains the operational logic required for
     * the receiving, processing and sending of PDUs which is needed to
     * successfully complete the represented iSCSI stage.
     * 
     * @param pdu
     *            the first {@link ProtocolDataUnit} to be processed in the
     *            stage
     * @throws IOException
     *             if the connection was closed unexpectedly
     * @throws InterruptedException
     * @throws InternetSCSIException
     *             if a PDU has violated the iSCSI standard
     * @throws DigestException
     *             if a digest error was detected
     * @throws SettingsException
     *             if the program has attempted to access a value from settings
     *             which has not been negotiated and which does not have a
     *             default value
     */
    public abstract void execute(ProtocolDataUnit pdu) throws IOException, InterruptedException,
        InternetSCSIException, DigestException, SettingsException;

    /**
     * Getting connection of this stage.
     * 
     * @return the related Connection of this stage.
     */
    public Connection getConnection() {
        return connection;
    }

}

package org.jscsi.target.connection.phase;

import java.io.IOException;
import java.security.DigestException;

import javax.naming.OperationNotSupportedException;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.target.connection.TargetConnection;
import org.jscsi.target.settings.SettingsException;

/**
 * Instances of this class represent a connection's phase (see {@link TargetConnection} for a description of
 * the relationship between
 * stages, phases, connections, and sessions).
 * <p>
 * To start a phase, one of the <i>execute</i> methods must be called, which one is sub-class-specific.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class TargetPhase {

    /**
     * The connection this phase is a part of.
     */
    protected TargetConnection connection;

    /**
     * The abstract constructor.
     * 
     * @param connection
     *            the connection is phase is a part of
     */
    public TargetPhase(TargetConnection connection) {
        this.connection = connection;
    }

    /**
     * Throws an {@link OperationNotSupportedException} unless overwritten.
     * 
     * @param pdu
     *            the first PDU to be processes as part of the phase
     * @return <code>true</code> if and only if the phase was completed
     *         successfully
     * @throws OperationNotSupportedException
     *             if the method is not overwritten
     * @throws IOException
     *             if an I/O error occurs
     * @throws InterruptedException
     *             if the current Thread is interrupted
     * @throws InternetSCSIException
     *             if a iSCSI protocol violation is detected
     * @throws DigestException
     *             if a PDU digest error is detected
     * @throws SettingsException
     *             if the target tries to access a parameter that has not been
     *             declared or negotiated and that has no default value
     */
    public boolean execute(ProtocolDataUnit pdu) throws OperationNotSupportedException, IOException,
        InterruptedException, InternetSCSIException, DigestException, SettingsException {
        throw new OperationNotSupportedException();
    }

    /**
     * Throws an {@link OperationNotSupportedException} unless overwritten.
     * 
     * @return <code>true</code> if and only if the phase was completed
     *         successfully
     * @throws OperationNotSupportedException
     *             if the method is not overwritten
     * @throws IOException
     *             if an I/O error occurs
     * @throws InterruptedException
     *             if the current Thread is interrupted
     * @throws InternetSCSIException
     *             if a iSCSI protocol violation is detected
     * @throws DigestException
     *             if a PDU digest error is detected
     * @throws SettingsException
     *             if the target tries to access a parameter that has not been
     *             declared or negotiated and that has no default value
     */
    public boolean execute() throws OperationNotSupportedException, InternetSCSIException, DigestException,
        IOException, InterruptedException, SettingsException {
        throw new OperationNotSupportedException();
    }

    public TargetConnection getTargetConnection() {
        return connection;
    }
}

package org.jscsi.target.connection;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.login.ISID;
import org.jscsi.target.Target;
import org.jscsi.target.TargetServer;
import org.jscsi.target.settings.SessionSettingsNegotiator;
import org.jscsi.target.storage.IStorageModule;
import org.jscsi.target.util.SerialArithmeticNumber;

/**
 * A class for objects representing an iSCSI session with all necessary
 * variables.
 * <p>
 * Currently, a {@link TargetSession} can only have <b>one</b> {@link Connection}, i.e.
 * <code>MaxConnections=1</code>.
 * 
 * @author Andreas Ergenzinger, University of Konstanz
 */
public class TargetSession {

    /**
     * Returns the value of the next {@link TargetSession} object's {@link #targetSessionIdentifyingHandle}
     * variable.
     * 
     * @return the value of the next {@link TargetSession} object's {@link #targetSessionIdentifyingHandle}
     *         variable
     */
    private static short getNextTargetSessionIdentifyingHandle() {
        short handle = nextTargetSessionIdentifyingHandle++;// no concurrency
                                                            // necessary
        if (handle == 0) {// is reserved
            handle = nextTargetSessionIdentifyingHandle++;
        }
        return handle;
    }

    /**
     * The {@link TargetServer} that this session is associated with
     */
    private TargetServer targetServer;

    /**
     * The {@link Target} that this session is associated with
     */
    private Target target;

    /**
     * The {@link TargetConnection} used for exchanging messages and data with
     * the session's initiator endpoint.
     */
    private Connection connection;

    /**
     * The {@link ISID} used by the initiator for identifying this session.
     */
    private ISID initiatorSessionID;

    /**
     * Keeps track of the value to expect in the <code>ExpCmdSN</code> field of
     * the next received {@link ProtocolDataUnit}
     */
    private SerialArithmeticNumber expectedCommandSequenceNumber;

    /**
     * Determines the number of iSCSI Command {@link ProtocolDataUnit}s the
     * initiator may send without having the wait for confirmation from the
     * target that the command has finished.
     * <p>
     * A value of <code>1</code> means that the initiator must wait for each command to finish before issuing
     * the next one.
     * 
     * @see #getExpectedCommandSequenceNumber()
     * @see #getMaximumCommandSequenceNumber()
     */
    private final int commandWindowSize = 1;

    /**
     * The {@link SessionSettingsNegotiator} responsible managing connection
     * parameters with session scope.
     */
    private SessionSettingsNegotiator sessionSettingsNegotiator;

    /**
     * A value used by the target to identify this session.
     */
    private short targetSessionIdentifyingHandle;

    /**
     * A counter for {@link #targetSessionIdentifyingHandle}s.
     * 
     * @see #getNextTargetSessionIdentifyingHandle()
     */
    private static short nextTargetSessionIdentifyingHandle = 1;

    /**
     * This value determines if this {@link TargetSession} is a discovery
     * session or a regular (operational) session.
     */
    private SessionType sessionType;

    /**
     * Constructs a new {@link TargetSession}
     * 
     * @param connection
     *            the session's (first) {@link Connection}
     * @param initiatorSessionID
     *            the {@link ISID} specified by the initiator
     * @param expectedCommandSequenceNumber
     *            initialization value of {@link #expectedCommandSequenceNumber}
     * @param statusSequenceNumber
     *            the value expected by the initiator in the next {@link ProtocolDataUnit}'s
     *            <code>StatSN</code> field
     */
    public TargetSession(final TargetServer target, final Connection connection,
        final ISID initiatorSessionID, final int expectedCommandSequenceNumber, final int statusSequenceNumber) {
        this.targetServer = target;
        // set connection variables and parameters
        connection.setSession(this);
        this.connection = connection;
        connection.setStatusSequenceNumber(statusSequenceNumber);

        // initialize ConnectionSettingsNegotiator (makes sure that settings are
        // initialized)
        sessionSettingsNegotiator = new SessionSettingsNegotiator();
        connection.initializeConnectionSettingsNegotiator(sessionSettingsNegotiator);

        // set session variables
        this.initiatorSessionID = initiatorSessionID;
        targetSessionIdentifyingHandle = getNextTargetSessionIdentifyingHandle();
        this.expectedCommandSequenceNumber = new SerialArithmeticNumber(expectedCommandSequenceNumber);
    }

    /**
     * Returns the session's {@link TargetServer}.
     * 
     * @return the session's {@link TargetServer}
     */
    public TargetServer getTargetServer() {
        return targetServer;
    }

    /**
     * Returns the session's {@link Connection}.
     * 
     * @return the session's {@link Connection}
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Returns the session's {@link Target}
     * 
     * @return the session's {@link Target}
     */
    public Target getTarget() {
        return target;
    }

    /**
     * Returns the session's {@link IStorageModule}.
     * 
     * @return the session's {@link IStorageModule}
     */
    public IStorageModule getStorageModule() {
        return target.getStorageModule();
    }

    /**
     * Returns the {@link SerialArithmeticNumber} representing the next expected
     * command sequence number.
     * <p>
     * This value will be used both during sending (<code>ExpCmdSN</code> field) and receiving (
     * <code>CmdSN</code>) of {@link ProtocolDataUnit}s.
     * 
     * @return the {@link SerialArithmeticNumber} representing the next expected
     *         command sequence number
     * @see #expectedCommandSequenceNumber
     */
    SerialArithmeticNumber getExpectedCommandSequenceNumber() {
        return expectedCommandSequenceNumber;
    }

    /**
     * Returns the {@link ISID} used by the initiator to identify this session.
     * 
     * @return the {@link ISID} used by the initiator to identify this session
     */
    public ISID getInitiatorSessionID() {
        return initiatorSessionID;
    }

    /**
     * Returns a {@link SerialArithmeticNumber} representing the maximum command
     * sequence number the target will accept.
     * <p>
     * This value will be used both during sending (<code>MaxCmdSN</code> field) and receiving (checking if
     * PDU's <code>CmdSN</code> lies in the command sequence number window resulting from
     * {@link #expectedCommandSequenceNumber} and {@link #commandWindowSize}) of {@link ProtocolDataUnit}s.
     * 
     * @return the {@link SerialArithmeticNumber} representing the next expected
     *         command sequence number
     */
    SerialArithmeticNumber getMaximumCommandSequenceNumber() {
        return new SerialArithmeticNumber(expectedCommandSequenceNumber.getValue() + commandWindowSize - 1);
    }

    /**
     * Returns the value used by the jSCSI Target to identify this session.
     * 
     * @return the value used by the jSCSI Target to identify this session
     */
    public short getTargetSessionIdentifyingHandle() {
        return targetSessionIdentifyingHandle;
    }

    /**
     * Returns <code>true</code> if this session is a regular (operational)
     * session, and <code>false</code> if it is s discovery session.
     * 
     * @return <code>true</code> if and only if this is not a regular
     *         (operational) session
     */
    public boolean isNormalSession() {
        return sessionType == SessionType.NORMAL;
    }

    /**
     * Removes a {@link TargetConnection} from the session's list of open
     * connections. If this reduces the number of connections to zero, the
     * session will be removed from the {@link TargetServer}'s list of active
     * sessions.
     * 
     * @param connection
     *            the connection to be removed
     */
    void removeTargetConnection(Connection connection) {
        // do this only if connection count == 0, currently it always is
        targetServer.removeTargetSession(this);
    }

    /**
     * Sets the session's type (discovery or operational).
     * <p>
     * The type may and must be set just once. Repeated calls of this method will fail. <code>true</code> will
     * be returned if the session type was set successfully, <code>false</code> if not.
     * 
     * @param sessionType
     *            the session type
     * @return <code>true</code> if the session type was set successfully, <code>false</code> if not
     */
    boolean setSessionType(SessionType sessionType) {
        // allow just once, accept only non-null parameter
        if (sessionType == null || this.sessionType != null)
            return false;
        this.sessionType = sessionType;
        return true;
    }

    /**
     * Sets the target name and retrieves the target (if it exists) from the TargetServer
     * 
     * @param targetName
     */
    public void setTargetName(String targetName) {
        if (targetName == null)
            target = null;
        target = targetServer.getTarget(targetName);
    }

    public String getTargetName() {
        if (target != null)
            return target.getTargetName();
        return null;
    }
}

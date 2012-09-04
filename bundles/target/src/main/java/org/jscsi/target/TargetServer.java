package org.jscsi.target;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.DigestException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.parser.OperationCode;
import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.login.ISID;
import org.jscsi.parser.login.LoginRequestParser;
import org.jscsi.target.connection.TargetConnection;
import org.jscsi.target.connection.TargetSession;
import org.jscsi.target.scsi.inquiry.DeviceIdentificationVpdPage;
import org.jscsi.target.settings.SettingsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The central class of the jSCSI Target, which keeps track of all active {@link TargetSession}s, stores
 * target-wide parameters and variables, and
 * which contains the {@link #main(String[])} method for starting the program.
 * 
 * @author Andreas Ergenzinger, University of Konstanz
 * @author Sebastian Graf, University of Konstanz
 */
public final class TargetServer implements Callable<Void> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TargetServer.class);

    /**
     * A {@link SocketChannel} used for listening to incoming connections.
     */
    private ServerSocketChannel serverSocketChannel;

    /**
     * Contains all active {@link TargetSession}s.
     */
    private Collection<TargetSession> sessions = new Vector<TargetSession>();

    /**
     * The jSCSI Target's global parameters.
     */
    private Configuration config;

    /**
     * 
     */
    private DeviceIdentificationVpdPage deviceIdentificationVpdPage;

    /**
     * A boolean that indicates that the initialization of all targets has been finished
     */
    private boolean initFinished = false;

    /**
     * The table of targets
     */
    private HashMap<String, Target> targets = new HashMap<String, Target>();

    /**
     * A target-wide counter used for providing the value of sent {@link ProtocolDataUnit}s'
     * <code>Target Transfer Tag</code> field, unless
     * that field is reserved.
     */
    private static final AtomicInteger nextTargetTransferTag = new AtomicInteger();

    /**
     * The connection the target server is using.
     */
    private TargetConnection connection;

    public TargetServer(final Configuration conf) {
        this.config = conf;

        LOGGER.debug("Starting jSCSI-target: ");

        // read target settings from configuration file

        LOGGER.debug("   port:           " + getConfig().getPort());
        LOGGER.debug("   loading targets.");
        // open the storage medium
        List<Target> targetInfo = getConfig().getTargets();
        for (Target curTargetInfo : targetInfo) {

            targets.put(curTargetInfo.getTargetName(), curTargetInfo);
            // print configuration and medium details
            LOGGER.debug("   target name:    " + curTargetInfo.getTargetName() + " loaded.");
        }

        this.deviceIdentificationVpdPage = new DeviceIdentificationVpdPage(this);
    }

    /**
     * Gets and increments the value to use in the next unreserved <code>Target Transfer Tag</code> field of
     * the next PDU to be sent by the
     * jSCSI Target.
     * 
     * @see #nextTargetTransferTag
     * @return the value to use in the next unreserved <code>Target Transfer Tag
     * </code> field
     */
    public static int getNextTargetTransferTag() {
        // value 0xffffffff is reserved
        int tag;
        do {
            tag = nextTargetTransferTag.getAndIncrement();
        } while (tag == -1);
        return tag;
    }

    /**
     * Starts the jSCSI target.
     * 
     * @param args
     *            all command line arguments are ignored
     * @throws IOException
     */
    public static void main(String[] args) throws Exception {
        TargetServer target;
        switch (args.length) {
        case 0:
            target = new TargetServer(Configuration.create());
            break;
        case 1:
            target =
                new TargetServer(Configuration.create(Configuration.CONFIGURATION_SCHEMA_FILE, new File(
                    args[0])));
            break;
        case 2:
            target = new TargetServer(Configuration.create(new File(args[0]), new File(args[1])));
            break;
        default:
            throw new IllegalArgumentException(
                "Only zero or one Parameter (Path to Configuration-File) allowed!");
        }
        target.call();
    }

    public Void call() throws Exception {

        LOGGER.debug("Starting jSCSI-target: ");

        // read target settings from configuration file

        LOGGER.debug("   port:           " + getConfig().getPort());
        LOGGER.debug("   loading targets.");
        // open the storage medium
        List<Target> targetInfo = getConfig().getTargets();
        for (Target curTargetInfo : targetInfo) {

            targets.put(curTargetInfo.getTargetName(), curTargetInfo);
            // print configuration and medium details
            LOGGER.debug("   target name:    " + curTargetInfo.getTargetName() + " loaded.");
        }

        initFinished = true;

        ExecutorService threadPool = Executors.newFixedThreadPool(4);
        // Create a blocking server socket and check for connections
        try {
            // Create a blocking server socket channel on the specified/default
            // port
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(true);
            serverSocketChannel.socket().bind(new InetSocketAddress(getConfig().getPort()));

            while (true) {
                // Accept the connection request.
                // If serverSocketChannel is blocking, this method blocks.
                // The returned channel is in blocking mode.
                final SocketChannel socketChannel = serverSocketChannel.accept();

                // deactivate Nagle algorithm
                socketChannel.socket().setTcpNoDelay(true);

                connection = new TargetConnection(socketChannel, true);
                try {
                    final ProtocolDataUnit pdu = connection.receivePdu();
                    // confirm OpCode
                    if (pdu.getBasicHeaderSegment().getOpCode() != OperationCode.LOGIN_REQUEST)
                        throw new InternetSCSIException();
                    // get initiatorSessionID
                    LoginRequestParser parser = (LoginRequestParser)pdu.getBasicHeaderSegment().getParser();
                    ISID initiatorSessionID = parser.getInitiatorSessionID();

                    /*
                     * TODO get (new or existing) session based on TSIH But
                     * since we don't do session reinstatement and
                     * MaxConnections=1, we can just create a new one.
                     */
                    TargetSession session =
                        new TargetSession(this, connection, initiatorSessionID, parser
                            .getCommandSequenceNumber(),// set ExpCmdSN
                                                        // (PDU is
                                                        // immediate,
                                                        // hence no ++)
                            parser.getExpectedStatusSequenceNumber());

                    sessions.add(session);
                    threadPool.submit(connection);// ignore returned Future
                } catch (DigestException e) {
                    LOGGER.info("Throws Exception", e);
                    continue;
                } catch (InternetSCSIException e) {
                    LOGGER.info("Throws Exception", e);
                    continue;
                } catch (SettingsException e) {
                    LOGGER.info("Throws Exception", e);
                    continue;
                }
            }
        } catch (IOException e) {
            // this block is entered if the desired port is already in use
            LOGGER.error("Throws Exception", e);
        }
        return null;
    }

    public Configuration getConfig() {
        return config;
    }

    public DeviceIdentificationVpdPage getDeviceIdentificationVpdPage() {
        return deviceIdentificationVpdPage;
    }

    public Target getTarget(String targetName) {
        synchronized (targets) {
            return targets.get(targetName);
        }
    }

    /**
     * Removes a session from the jSCSI Target's list of active sessions.
     * 
     * @param session
     *            the session to remove from the list of active sessions
     */
    public synchronized void removeTargetSession(TargetSession session) {
        sessions.remove(session);
    }

    public String[] getTargetNames() {
        String[] returnNames = new String[targets.size()];
        returnNames = targets.keySet().toArray(returnNames);
        return returnNames;
    }

    /**
     * Checks to see if this target name is valid.
     * 
     * @param checkTargetName
     * @return true if the the target name is configured
     */
    public boolean isValidTargetName(String checkTargetName) {
        return targets.containsKey(checkTargetName);
    }

    /**
     * Using this connection mainly for test pruposes.
     * 
     * @return the connection the target server established.
     */
    public TargetConnection getConnection() {
        return this.connection;
    }

    /**
     * @return true if the target is ready and all targets have been initialized (e.g. put into the list)
     */
    public boolean isReady() {
        return initFinished;
    }

}

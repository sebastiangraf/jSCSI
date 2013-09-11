package org.jscsi.target.settings;


import java.util.List;
import java.util.Vector;

import org.jscsi.parser.ProtocolDataUnit;
import org.jscsi.parser.login.LoginStage;
import org.jscsi.target.TargetServer;
import org.jscsi.target.connection.TargetSession;
import org.jscsi.target.settings.entry.BooleanEntry;
import org.jscsi.target.settings.entry.Entry;
import org.jscsi.target.settings.entry.NumericalEntry;
import org.jscsi.target.settings.entry.NumericalRangeEntry;
import org.jscsi.target.settings.entry.StringEntry;
import org.jscsi.target.settings.entry.Use;


/**
 * In addition to offering all methods of their {@link SettingsNegotiator} parent class,
 * {@link ConnectionSettingsNegotiator} instances provide all methods necessary for text parameter negotiation and for
 * creating {@link Settings} objects, which allow access to the current parameters.
 * <p>
 * Each instance of this class belongs to one <code>Connection</code>, for which it manages all connection-specific
 * parameters. A similar association exists between the connection's enclosing {@link TargetSession} and the
 * {@link #sessionSettingsNegotiator} member variable.
 * <p>
 * A negotiation sequence consists of the following steps, performed in the given order:
 * <p>
 * <ol>
 * <li>Call {@link #beginNegotiation()} until it returns <code>true</code>.</li>
 * <li>One or, if the initiator is using multiple PDU sequences, multiple calls of
 * {@link #negotiate(TargetServer, LoginStage, boolean, boolean, List, List)}. The method will return <code>false</code>
 * if there was a problem.</li>
 * <li>Call {@link #checkConstraints()} to check more complex requirements. The method will return <code>false</code> if
 * there was a problem.</li>
 * <li>Call {@link #finishNegotiation(boolean)} with the appropriate parameter. This step is mandatory.</li>
 * </ol>
 * 
 * @author Andreas Ergenzinger, University of Konstanz
 */
public final class ConnectionSettingsNegotiator extends SettingsNegotiator {

    /**
     * The {@link SessionSettingsNegotiator} maintaining all {@link Entry} objects for session-wide parameters.
     */
    private final SessionSettingsNegotiator sessionSettingsNegotiator;

    /**
     * A current snapshot of all connection-specific parameters.
     */
    private ConnectionSettingsBuilderComponent connectionSettingBuilderComponent;

    /**
     * A current snapshot of all parameters, both connection-specific and session-wide.
     */
    private Settings settings;

    /**
     * The {@link ConnectionSettingsNegotiator} constructor.
     * 
     * @param sessionSettingsNegotiator the {@link SessionSettingsNegotiator} maintaining all {@link Entry} objects for
     *            session-wide parameters
     */
    public ConnectionSettingsNegotiator (final SessionSettingsNegotiator sessionSettingsNegotiator) {
        super();// initializes entries
        this.sessionSettingsNegotiator = sessionSettingsNegotiator;
        // initialize settings
        updateSettingsBuilderComponent();
        updateSettings();
    }

    /**
     * This method must be called at the beginning of a parameter negotiation sequence.
     * <p>
     * This method will block until no other {@link ConnectionSettingsNegotiator} is negotiating and will return
     * <code>true</code> if negotiations may proceed, or <code>false</code>, if the request for the permission to
     * negotiate was denied.
     * 
     * @return <code>true</code> if and only if the caller is allowed to negotiate parameters
     * @see #finishNegotiation(boolean)
     */
    public boolean beginNegotiation () {
        final boolean hasLock = sessionSettingsNegotiator.lock();
        if (hasLock) {
            // back up entries with connection and with session scope
            backUpEntries();
            sessionSettingsNegotiator.backUpEntries();
        }
        return hasLock;
    }

    /**
     * This method must be called at the end of a negotiation sequence. If there was a problem during negotiations, all
     * changes to the managed {@link Entry} objects must be discarded by setting the <i>commitChanges</i> parameter to
     * <code>false</code>. If it is <code>true</code>, then the parameter changes will be incorporated into the updated
     * {@link #settings}.
     * <p>
     * Calling this method should be ensured by a <code>try ... catch ...
     * finally</code> block.
     * 
     * @param commitChanges <code>true</code> if and only if the negotiated parameter changes are to be committed
     */
    public void finishNegotiation (final boolean commitChanges) {
        // commit or roll back connection-specific entries
        commitOrRollBackChanges(commitChanges);
        // commit or roll back session-wide entries
        sessionSettingsNegotiator.commitOrRollBackChanges(commitChanges);
        // update settings
        updateSettingsBuilderComponent();
        sessionSettingsNegotiator.updateSettingsBuilderComponent();
        updateSettings();
        // allow other Threads to negotiate
        sessionSettingsNegotiator.unlock();
    }

    /**
     * Processes one or more <i>key-value</i> pairs sent by the initiator, formulates response <i>key-value</i> pairs
     * and changes the involved {@link Entry} instances accordingly.
     * 
     * @param loginStage specifies the current stage or phase
     * @param leadingConnection <code>true</code> if and only if the connection is the first connection of the enclosing
     *            session
     * @param initialPdu <code>true</code> if and only if the <i>key-value</i> pairs were sent in the first
     *            {@link ProtocolDataUnit} received on this connection
     * @param requestKeyValuePairs contains the <i>key-value</i> pairs from the initiator; processed elements will be
     *            removed
     * @param responseKeyValuePairs will contain the <i>key-value</i> pairs from the jSCSI target
     * @return <code>true</code> if everything went fine and <code>false</code> if there was an irreconcilable problem
     */
    public boolean negotiate (TargetServer target, final LoginStage loginStage, final boolean leadingConnection, final boolean initialPdu, final List<String> requestKeyValuePairs, final List<String> responseKeyValuePairs) {

        // split up key=value pairs from requester
        final List<String> keys = new Vector<String>();
        final List<String> values = new Vector<String>();
        for (String keyValuePair : requestKeyValuePairs) {
            final String[] split = TextParameter.splitKeyValuePair(keyValuePair);
            if (split == null) {
                System.err.println("malformatted key=value pair: " + keyValuePair);
                return false;
            }
            keys.add(split[0]);
            values.add(split[1]);
        }

        // get respective entry (declared by initiator, or negotiated) and
        // let it process the key=value pair
        Entry entry;
        boolean everythingOkay = true;
        while (keys.size() > 0) {
            entry = getEntry(keys.get(0));
            // respond to unknown keys
            if (entry == null) {
                responseKeyValuePairs.add(TextParameter.toKeyValuePair(keys.get(0), TextKeyword.NOT_UNDERSTOOD));
            } else {// appropriate entry was found
                // process key=value pair and remember if there is any trouble
                everythingOkay &= entry.negotiate(target, loginStage, leadingConnection, initialPdu, keys.get(0), values.get(0), responseKeyValuePairs);
            }
            // remove processed key and values
            keys.remove(0);
            values.remove(0);
        }

        // check if initiator has sent all mandatory parameters
        // and append target declarations
        if (initialPdu) {
            // initiator must provide the InitiatorName in the first Login PDU
            // (InitiatorAlias is optional)
            everythingOkay &= getEntry(TextKeyword.INITIATOR_NAME).checkAccepted();

            // in a normal session the initiator must declare TargetName
            // the target must reply with TargetPortalGroupTag and should
            // append TargetAlias
            boolean normalSession = TextKeyword.NORMAL.equals(((StringEntry) getEntry(TextKeyword.SESSION_TYPE)).getStringValue());
            if (normalSession) {
                // check if proposed TargetName is correct
                final StringEntry targetNameEntry = (StringEntry) getEntry(TextKeyword.TARGET_NAME);
                final String targetName = targetNameEntry.getStringValue();
                if (targetName == null || // not declared
                !target.isValidTargetName(targetName)) // wrong name
                everythingOkay = false;

                // send TargetAlias
                String targetAlias = null;
                if (everythingOkay) targetAlias = target.getTarget(targetName).getTargetAlias();// might
                // be
                // undefined
                if (targetAlias != null) {
                    responseKeyValuePairs.add(TextParameter.toKeyValuePair(TextKeyword.TARGET_ALIAS, targetAlias));
                }

                // send TargetPortalGroupTag
                responseKeyValuePairs.add(TextParameter.toKeyValuePair(TextKeyword.TARGET_PORTAL_GROUP_TAG, Integer.valueOf(target.getConfig().getTargetPortalGroupTag()).toString()));
            }
        }

        return everythingOkay;
    }

    @Override
    public boolean checkConstraints () {
        return sessionSettingsNegotiator.checkConstraints();
        /*
         * in multi-connection session, the InitiatorName of follow-up connections should be checked against the
         * InitiatorName of the leading connection
         */
    }

    /**
     * Returns the {@link Entry} responsible for negotiating the specified <i>key</i> identifying either a session-wide
     * or connection-specific parameter, or <code>null</code> if no such {@link Entry} can be found.
     * 
     * @param key identifies an {@link Entry}
     * @return the requested {@link Entry} or <code>null</code>
     */
    private Entry getEntry (final String key) {
        Entry entry = null;
        // check connection-only entries
        entry = getEntry(key, entries);
        if (entry == null) // keep looking in session-wide entries
        entry = sessionSettingsNegotiator.getEntry(key);
        return entry;
    }

    /**
     * This method checks if any parameter changes have been committed since the last call and, if so, creates a new
     * {@link Settings} object reflecting the current parameters.
     * 
     * @return The current parameters
     */
    public Settings getSettings () {
        // check if settings are up to date
        if (sessionSettingsNegotiator.getCurrentSettingsId() > settings.getSettingsId()) updateSettings();
        return settings;
    }

    /**
     * Updates {@link #settings} by replacing it with a more current copy of the managed parameters.
     */
    private synchronized void updateSettings () {
        settings = new Settings(connectionSettingBuilderComponent, sessionSettingsNegotiator.getSessionSettingsBuilderComponent());
    }

    @Override
    protected void initializeEntries () {

        /*
         * Determines type and use of the data digest.
         */
        entries.add(new StringEntry(new KeySet(TextKeyword.DATA_DIGEST),// keySet
        NegotiationType.NEGOTIATED,// negotiationType
        Use.LOPNS,// use
        NegotiationStatus.DEFAULT,// negotiationStatus
        new String[] { TextKeyword.NONE },// supportedValues,
        TextKeyword.NONE));// defaultValue

        /*
         * Determines type and use of the header digest.
         */
        entries.add(new StringEntry(new KeySet(TextKeyword.HEADER_DIGEST),// keySet
        NegotiationType.NEGOTIATED,// negotiationType
        Use.LOPNS,// use
        NegotiationStatus.DEFAULT,// negotiationStatus
        new String[] { TextKeyword.NONE },// supportedValues,
        TextKeyword.NONE));// defaultValue

        /*
         * Turns the target-to-initiator markers on or off.
         */
        entries.add(new BooleanEntry(new KeySet(TextKeyword.IF_MARKER),// keySet
        Use.LOPNS,// use
        NegotiationStatus.DEFAULT,// negotiationStatus
        false,// negotiationValue
        BooleanResultFunction.AND,// resultFunction
        false));// defaultValue

        /*
         * Interval value (in 4-byte words) for target-to-initiator markers. The interval is measured from the end of
         * one marker to the beginning of the next one. The offer can have only a range; the response can have only a
         * single value (picked from the offered range) or Reject. Will always be Irrelevant.
         */
        entries.add(new NumericalRangeEntry(new KeySet(TextKeyword.IF_MARK_INT),// keySet
        Use.LOPNS,// use
        NegotiationStatus.IRRELEVANT,// negotiationStatus
        2048,// negotiationValue
        NumericalValueRange.create(1, 65535),// protocolValueRange
        2048));// defaultValue

        /*
         * The maximum amount of data that either the initiator or the target can receive in any iSCSI PDU. Zero (don't
         * care) can be used. I&T can specify (declare) the Max they can receive. This is a connection- and direction-
         * specific parameter. The actual value used by the target will be min(This value, MaxBurstLength) for data-in
         * and solicited data-out data. Min(This value, FirstBurstLength) for unsolicited data.
         */
        entries.add(new NumericalEntry(new KeySet(TextKeyword.MAX_RECV_DATA_SEGMENT_LENGTH),// keySet
        NegotiationType.DECLARED,// negotiationType
        Use.LOPNS_AND_FFP,// use
        NegotiationStatus.DEFAULT,// negotiationStatus
        8192,// negotiationValue (default value, will be used if I sends
             // 0)
        NumericalValueRange.create(512, 16777215),// protocolValueRange
        // 512 to 2^24 - 1
        NumericalResultFunction.MIN,// resultFunction
        8192,// defaultValue, 8K
        true));// zeroMeansDontCare

        /*
         * Turns the initiator-to-target markers on or off.
         */
        entries.add(new BooleanEntry(new KeySet(TextKeyword.OF_MARKER),// keySet
        Use.LOPNS,// use
        NegotiationStatus.DEFAULT,// negotiationStatus
        false,// negotiationValue
        BooleanResultFunction.AND,// resultFunction
        false));// defaultValue

        /*
         * Interval value (in 4-byte words) for initiator-to-target markers. The interval is measured from the end of
         * one marker to the beginning of the next one. The offer can have only a range; the response can have only a
         * single value (picked from the offered range) or Reject. Will always be Irrelevant.
         */
        entries.add(new NumericalRangeEntry(new KeySet(TextKeyword.OF_MARK_INT),// keySet
        Use.LOPNS,// use
        NegotiationStatus.IRRELEVANT,// negotiationStatus
        2048,// negotiationValue
        NumericalValueRange.create(1, 65535),// protocolValueRange
        2048));// defaultValue

        /*
         * This entry is not used for Settings initialization, TargetName has a session-wide scope. This entry
         * intercepts the TargetName parameter the initiator has to declare at the beginning of normal sessions.
         */
        entries.add(new StringEntry(new KeySet(TextKeyword.TARGET_NAME),// keySet
        NegotiationType.DECLARED,// negotiationType
        Use.INITIAL,// use
        NegotiationStatus.NOT_NEGOTIATED,// negotiationStatus
        null,// supportedValues, anything goes
        null));// defaultValue
    }

    /**
     * Updates {@link ConnectionSettingsBuilderComponent} with the currently valid parameters retrieved from the
     * elements of {@link SettingsNegotiator#entries}.
     */
    protected void updateSettingsBuilderComponent () {
        connectionSettingBuilderComponent = new ConnectionSettingsBuilderComponent(entries);
    }
}

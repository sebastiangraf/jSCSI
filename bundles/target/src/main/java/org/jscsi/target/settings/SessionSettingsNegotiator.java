package org.jscsi.target.settings;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;

import javax.naming.OperationNotSupportedException;

import org.jscsi.target.connection.TargetSession;
import org.jscsi.target.settings.entry.BooleanEntry;
import org.jscsi.target.settings.entry.Entry;
import org.jscsi.target.settings.entry.NumericalEntry;
import org.jscsi.target.settings.entry.StringEntry;
import org.jscsi.target.settings.entry.Use;
import org.jscsi.target.util.BinaryLock;

/**
 * A class for managing {@link Entry} objects responsible for negotiating
 * session-wide parameters.
 * <p>
 * Each instance of this class belongs to one {@link TargetSession} object.
 * 
 * @see SettingsNegotiator
 * @author Andreas Ergenzinger
 */
public final class SessionSettingsNegotiator extends SettingsNegotiator {

    /**
     * Prevents concurrent negotiations over multiple connections of the same
     * session.
     * 
     * @see #lock()
     * @see #unlock()
     */
    private final BinaryLock lock = new BinaryLock();

    /**
     * A counter that provides a unique, ordered identifying value for {@link Settings} objects.
     */
    private final AtomicLong currentSettingsId = new AtomicLong();

    /**
     * A current snapshot of all session-wide parameters.
     */
    private SessionSettingsBuilderComponent sessionSettingBuilderComponent;

    /**
     * The {@link SessionSettingsNegotiator} constructor.
     */
    public SessionSettingsNegotiator() {
        super();// initializes entries
        updateSettingsBuilderComponent();
    }

    /**
     * Blocks until the {@link #lock()} has been acquired and returns <code>true</code> or returns
     * <code>false</code> if the method returned
     * prematurely due to an interrupt.
     * <p>
     * The usual safeguards with using {@link Lock} objects should be applied, namely using
     * <code>try ...  catch ... finally ...</code> blocks to make sure the lock is always released.
     * 
     * @return <code>true</code>if and only if the lock has been acquired
     */
    boolean lock() {
        return lock.lock();
    }

    /**
     * Releases the {@link #lock} if called by the locking {@link Thread}.
     * 
     * @see #lock()
     */
    void unlock() {
        lock.unlock();
    }

    /**
     * Returns the {@link Entry} responsible for negotiating the specified
     * <i>key</i> identifying a session-wide parameter, or <code>null</code> if
     * no such {@link Entry} can be found.
     * 
     * @param key
     *            identifies an {@link Entry} responsible for a session-wide
     *            parameter
     * @return the requested {@link Entry} or <code>null</code>
     */
    Entry getEntry(final String key) {
        return getEntry(key, entries);
    }

    long getCurrentSettingsId() {
        return currentSettingsId.get();
    }

    @Override
    protected void initializeEntries() {

        /*
         * No indicates that the data PDUs within a sequence can be in any
         * order. Yes indicates that the data PDUs within a sequence have to be
         * at continuously increasing addresses and that overlays are forbidden.
         */
        entries.add(new BooleanEntry(new KeySet(TextKeyword.DATA_PDU_IN_ORDER),// keySet
            Use.LEADING_LOPNS,// use
            NegotiationStatus.DEFAULT,// negotiationStatus
            true,// negotiationValue
            BooleanResultFunction.OR,// resultFunction
            true));// defaultValue

        /*
         * If set to No, the data PDU sequence may be transferred in any order.
         * If set to Yes, the sequence must be transferred using continuously
         * increasing offsets except for error recovery.
         */
        entries.add(new BooleanEntry(new KeySet(TextKeyword.DATA_SEQUENCE_IN_ORDER),// keySet
            Use.LEADING_LOPNS,// use
            NegotiationStatus.DEFAULT,// negotiationStatus
            true,// negotiationValue
            BooleanResultFunction.OR,// resultFunction
            true));// defaultValue

        /*
         * Max seconds that connection and task allegiance reinstatement is
         * still possible following a connection termination or reset. A value
         * of zero means that no reinstatement is possible.
         */
        entries.add(new NumericalEntry(new KeySet(TextKeyword.DEFAULT_TIME_2_RETAIN,
            TextKeyword.TIME_2_RETAIN),// keySet
            NegotiationType.NEGOTIATED,// negotiationType
            Use.LEADING_LOPNS,// use
            NegotiationStatus.DEFAULT,// negotiationStatus
            0,// negotiationValue
            NumericalValueRange.create(0, 3600),// protocolValueRange
            NumericalResultFunction.MIN,// resultFunction
            20,// defaultValue
            false));// zeroMeansDontCare

        /*
         * Min seconds to wait before attempting connection and task allegiance
         * reinstatement after a connection termination or a connection reset. A
         * value of zero means that task reassignment can be done immediately.
         */
        entries.add(new NumericalEntry(new KeySet(TextKeyword.DEFAULT_TIME_2_WAIT, TextKeyword.TIME_2_WAIT),// keySet
            NegotiationType.NEGOTIATED,// negotiationType
            Use.LEADING_LOPNS,// use
            NegotiationStatus.DEFAULT,// negotiationStatus
            2,// negotiationValue
            NumericalValueRange.create(0, 3600),// protocolValueRange
            NumericalResultFunction.MAX,// resultFunction
            2,// defaultValue
            false));// zeroMeansDontCare

        /*
         * Recovery levels represent a combination of recovery capabilities.
         * Each level includes all the capabilities of the lower recovery level.
         */
        entries.add(new NumericalEntry(new KeySet(TextKeyword.ERROR_RECOVERY_LEVEL),// keySet
            NegotiationType.NEGOTIATED,// negotiationType
            Use.LEADING_LOPNS,// use
            NegotiationStatus.DEFAULT,// negotiationStatus
            0,// negotiationValue
            NumericalValueRange.create(0, 2),// protocolValueRange
            NumericalResultFunction.MIN,// resultFunction
            0,// defaultValue
            false));// zeroMeansDontCare

        /*
         * Maximum SCSI payload, in bytes, of unsolicited data an initiator may
         * send to the target. Includes immediate data and a sequence of
         * unsolicited Data-Out PDUs. Zero (don't care) can be used. Must be <=
         * MaxBurstLength.
         */
        entries.add(new NumericalEntry(new KeySet(TextKeyword.FIRST_BURST_LENGTH),// keySet
            NegotiationType.NEGOTIATED,// negotiationType
            Use.LEADING_LOPNS,// use
            NegotiationStatus.DEFAULT,// negotiationStatus
            65536,// negotiationValue (default value)
            NumericalValueRange.create(512, 16777215),// protocolValueRange
            // 512 to 2^24 - 1
            NumericalResultFunction.MIN,// resultFunction
            65536,// defaultValue, 64K
            true));// zeroMeansDontCare

        /*
         * Either the initiator or target can turn off ImmediateData.
         */
        entries.add(new BooleanEntry(new KeySet(TextKeyword.IMMEDIATE_DATA),// keySet
            Use.LEADING_LOPNS,// use
            NegotiationStatus.DEFAULT,// negotiationStatus
            true,// negotiationValue
            BooleanResultFunction.AND,// resultFunction
            true));// defaultValue

        /*
         * Turns off the default use of R2T; allows an initiator to start
         * sending data to a target as if it had received an initial R2T.
         */
        entries.add(new BooleanEntry(new KeySet(TextKeyword.INITIAL_R_2_T),// keySet
            Use.LEADING_LOPNS,// use
            NegotiationStatus.DEFAULT,// negotiationStatus
            true,// negotiationValue
            BooleanResultFunction.OR,// resultFunction
            true));// defaultValue

        /*
         * Sent to the target in the login PDU.
         */
        entries.add(new StringEntry(new KeySet(TextKeyword.INITIATOR_ALIAS),// keySet
            NegotiationType.DECLARED,// negotiationType
            Use.INITIAL_AND_FFP,// use
            NegotiationStatus.NOT_NEGOTIATED,// negotiationStatus
            null,// supportedValues, anything goes
            null));// defaultValue

        /*
         * Must be sent on first login request per connection.
         */
        entries.add(new StringEntry(new KeySet(TextKeyword.INITIATOR_NAME),// keySet
            NegotiationType.DECLARED,// negotiationType
            Use.INITIAL,// use
            NegotiationStatus.NOT_NEGOTIATED,// negotiationStatus
            null,// supportedValues, anything goes
            null));// defaultValue

        /*
         * Maximum SCSI data payload in bytes for data-in or for a solicited
         * data-out sequence. Zero (don't care) can be used.
         */
        entries.add(new NumericalEntry(new KeySet(TextKeyword.MAX_BURST_LENGTH),// keySet
            NegotiationType.NEGOTIATED,// negotiationType
            Use.LEADING_LOPNS,// use
            NegotiationStatus.DEFAULT,// negotiationStatus
            262144,// negotiationValue (default value)
            NumericalValueRange.create(512, 16777215),// protocolValueRange
            // 512 to 2^24 - 1
            NumericalResultFunction.MIN,// resultFunction
            262144,// defaultValue, 256K
            true));// zeroMeansDontCare

        /*
         * The initiator and target negotiate the maximum number of connections
         * that can be requested or are acceptable.
         */
        entries.add(new NumericalEntry(new KeySet(TextKeyword.MAX_CONNECTIONS),// keySet
            NegotiationType.NEGOTIATED,// negotiationType
            Use.LEADING_LOPNS,// use
            NegotiationStatus.DEFAULT,// negotiationStatus
            1,// negotiationValue (default value)
            NumericalValueRange.create(1, 65535),// protocolValueRange
            NumericalResultFunction.MIN,// resultFunction
            1,// defaultValue
            false));// zeroMeansDontCare

        /*
         * The maximum number of outstanding R2Ts.
         */
        entries.add(new NumericalEntry(new KeySet(TextKeyword.MAX_OUTSTANDING_R_2_T),// keySet
            NegotiationType.NEGOTIATED,// negotiationType
            Use.LEADING_LOPNS,// use
            NegotiationStatus.DEFAULT,// negotiationStatus
            1,// negotiationValue (default value)
            NumericalValueRange.create(1, 65535),// protocolValueRange
            NumericalResultFunction.MIN,// resultFunction
            1,// defaultValue
            false));// zeroMeansDontCare

        /*
         * The session type (Normal or Discovery).
         */
        entries.add(new StringEntry(new KeySet(TextKeyword.SESSION_TYPE),// keySet
            NegotiationType.DECLARED,// negotiationType
            Use.INITIAL,// use
            NegotiationStatus.DEFAULT,// negotiationStatus
            null,// supportedValues, no checking
            TextKeyword.NORMAL));// defaultValue
    }

    /**
     * Updates {@link #sessionSettingBuilderComponent} with the currently valid
     * parameters retrieved from the elements of {@link SettingsNegotiator#entries}.
     */
    protected void updateSettingsBuilderComponent() {

        sessionSettingBuilderComponent =
            new SessionSettingsBuilderComponent(currentSettingsId.getAndIncrement() + 1,// settingsId
                entries);// entries with current/new values
    }

    SessionSettingsBuilderComponent getSessionSettingsBuilderComponent() {
        return sessionSettingBuilderComponent;
    }

    @Override
    public boolean checkConstraints() {
        try {
            // ensure FirstBurstLength <= MaxBurstLength
            final int firstBurstLength = getEntry(TextKeyword.FIRST_BURST_LENGTH).getIntegerValue();
            final int maxBurstLength = getEntry(TextKeyword.MAX_BURST_LENGTH).getIntegerValue();
            if (maxBurstLength > firstBurstLength)
                return false;
        } catch (OperationNotSupportedException e) {
            // programmer error, requested wrong data type
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

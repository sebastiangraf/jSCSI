package org.jscsi.target.settings;

import java.util.Collection;

import javax.naming.OperationNotSupportedException;

import org.jscsi.target.settings.entry.Entry;

/**
 * Instances of {@link SessionSettingsBuilderComponent} are used jointly with
 * instances of {@link ConnectionSettingsBuilderComponent} for creating {@link Settings} objects.
 * <p>
 * {@link SessionSettingsBuilderComponent} objects provide all session-wide parameters managed by the
 * session's {@link SessionSettingsNegotiator}.
 * 
 * @see Settings#Settings(ConnectionSettingsBuilderComponent, SessionSettingsBuilderComponent)
 * @author Andreas Ergenzinger
 */
final class SessionSettingsBuilderComponent {

    /**
     * A serial number that allows to order {@link Settings} objects based on
     * creation order.
     */
    long settingsId;

    /**
     * The <code>DataPDUInOrder</code> parameter.
     */
    Boolean dataPduInOrder;

    /**
     * The <code>DataSequenceInOrder</code> parameter.
     */
    Boolean dataSequenceInOrder;

    /**
     * The <code>DefaultTime2Retain</code> parameter.
     */
    Integer defaultTime2Retain;

    /**
     * The <code>DefaultTime2Wait</code> parameter.
     */
    Integer defaultTime2Wait;

    /**
     * The <code>ErrorRecoveryLevel</code> parameter.
     */
    Integer errorRecoveryLevel;

    /**
     * The <code>FirstBurstLength</code> parameter.
     */
    Integer firstBurstLength;

    /**
     * The <code>ImmediateData</code> parameter.
     */
    Boolean immediateData;

    /**
     * The <code>InitialR2T</code> parameter.
     */
    Boolean initialR2T;

    /**
     * The <code>InitiatorAlias</code> parameter.
     */
    String initiatorAlias;

    /**
     * The <code>InitiatorName</code> parameter.
     */
    String initiatorName;

    /**
     * The <code>MaxBurstLength</code> parameter.
     */
    Integer maxBurstLength;

    /**
     * The <code>MaxConnections</code> parameter.
     */
    Integer maxConnections;

    /**
     * The <code>MaxOutstandingR2T</code> parameter.
     */
    Integer maxOutstandingR2T;

    /**
     * The <code>SessionType</code> parameter.
     */
    String sessionType;

    /**
     * The {@link SessionSettingsBuilderComponent} constructor. The passed {@link Collection} must contain all
     * session-wide {@link Entry} objects,
     * since the constructor will try to locate a specific {@link Entry} for
     * each member variable and copy its current value.
     * 
     * @param entries
     *            a {@link Collection} containing all session-wide {@link Entry} objects
     */
    SessionSettingsBuilderComponent(final long settingsId, final Collection<Entry> entries) {
        this.settingsId = settingsId;
        try {
            dataPduInOrder =
                SettingsNegotiator.getEntry(TextKeyword.DATA_PDU_IN_ORDER, entries).getBooleanValue();
            dataSequenceInOrder =
                SettingsNegotiator.getEntry(TextKeyword.DATA_SEQUENCE_IN_ORDER, entries).getBooleanValue();
            defaultTime2Retain =
                SettingsNegotiator.getEntry(TextKeyword.DEFAULT_TIME_2_RETAIN, entries).getIntegerValue();
            defaultTime2Wait =
                SettingsNegotiator.getEntry(TextKeyword.DEFAULT_TIME_2_WAIT, entries).getIntegerValue();
            errorRecoveryLevel =
                SettingsNegotiator.getEntry(TextKeyword.ERROR_RECOVERY_LEVEL, entries).getIntegerValue();
            firstBurstLength =
                SettingsNegotiator.getEntry(TextKeyword.FIRST_BURST_LENGTH, entries).getIntegerValue();
            immediateData =
                SettingsNegotiator.getEntry(TextKeyword.IMMEDIATE_DATA, entries).getBooleanValue();
            initialR2T = SettingsNegotiator.getEntry(TextKeyword.INITIAL_R_2_T, entries).getBooleanValue();
            initiatorAlias =
                SettingsNegotiator.getEntry(TextKeyword.INITIATOR_ALIAS, entries).getStringValue();
            initiatorName = SettingsNegotiator.getEntry(TextKeyword.INITIATOR_NAME, entries).getStringValue();
            maxBurstLength =
                SettingsNegotiator.getEntry(TextKeyword.MAX_BURST_LENGTH, entries).getIntegerValue();
            maxConnections =
                SettingsNegotiator.getEntry(TextKeyword.MAX_CONNECTIONS, entries).getIntegerValue();
            maxOutstandingR2T =
                SettingsNegotiator.getEntry(TextKeyword.MAX_OUTSTANDING_R_2_T, entries).getIntegerValue();
            sessionType = SettingsNegotiator.getEntry(TextKeyword.SESSION_TYPE, entries).getStringValue();
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }
}

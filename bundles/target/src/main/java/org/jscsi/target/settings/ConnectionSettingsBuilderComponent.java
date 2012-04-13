package org.jscsi.target.settings;

import java.util.Collection;

import javax.naming.OperationNotSupportedException;

import org.jscsi.target.settings.entry.Entry;

/**
 * Instances of {@link ConnectionSettingsBuilderComponent} are used jointly with
 * instances of {@link SessionSettingsBuilderComponent} for creating {@link Settings} objects.
 * <p>
 * {@link ConnectionSettingsBuilderComponent} objects provide all connection-specific parameters managed by
 * the connection's {@link ConnectionSettingsNegotiator}.
 * 
 * @see Settings#Settings(ConnectionSettingsBuilderComponent, SessionSettingsBuilderComponent)
 * @author Andreas Ergenzinger
 */
final class ConnectionSettingsBuilderComponent {

    /**
     * The <code>TargetName</code> parameter.
     */
    String targetName;

    /**
     * The <code>DataDigest</code> parameter.
     */
    String dataDigest;

    /**
     * The <code>HeaderDigest</code> parameter.
     */
    String headerDigest;

    /**
     * The <code>IFMarker</code> parameter.
     */
    Boolean ifMarker;

    /**
     * The <code>IFMarkInt</code> parameter.
     */
    Integer ifMarkInt;

    /**
     * The <code>MaxRecvDataSegmentLength</code> parameter.
     */
    Integer maxRecvDataSegmentLength;

    /**
     * The <code>OFMarker</code> parameter.
     */
    Boolean ofMarker;

    /**
     * The <code>OFMarkInt</code> parameter.
     */
    Integer ofMarkInt;

    /**
     * The {@link ConnectionSettingsBuilderComponent} constructor. The passed {@link Collection} must contain
     * all connection-specific {@link Entry} objects, since the constructor will try to locate a specific
     * {@link Entry} for each member variable and copy its current value.
     * 
     * @param entries
     *            a {@link Collection} containing all connection-specific {@link Entry} objects
     */
    ConnectionSettingsBuilderComponent(final Collection<Entry> entries) {
        try {
            targetName = SettingsNegotiator.getEntry(TextKeyword.TARGET_NAME, entries).getStringValue();
            dataDigest = SettingsNegotiator.getEntry(TextKeyword.DATA_DIGEST, entries).getStringValue();
            headerDigest = SettingsNegotiator.getEntry(TextKeyword.HEADER_DIGEST, entries).getStringValue();
            ifMarker = SettingsNegotiator.getEntry(TextKeyword.IF_MARKER, entries).getBooleanValue();
            ifMarkInt = SettingsNegotiator.getEntry(TextKeyword.IF_MARK_INT, entries).getIntegerValue();
            maxRecvDataSegmentLength =
                SettingsNegotiator.getEntry(TextKeyword.MAX_RECV_DATA_SEGMENT_LENGTH, entries)
                    .getIntegerValue();
            ofMarker = SettingsNegotiator.getEntry(TextKeyword.OF_MARKER, entries).getBooleanValue();
            ofMarkInt = SettingsNegotiator.getEntry(TextKeyword.OF_MARK_INT, entries).getIntegerValue();
        } catch (OperationNotSupportedException e) {
            e.printStackTrace();
        }
    }
}

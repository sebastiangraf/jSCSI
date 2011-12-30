package org.jscsi.target.connection;

import org.jscsi.target.settings.TextKeyword;

/**
 * The {@link SessionType} of a {@link TargetSession} determines which stages
 * can be reached in the Full Feature Phase.
 * 
 * @author Andreas Ergenzinger
 */
public enum SessionType {

    /**
     * The session is a discovery session. The initiator is only allowed to
     * issue request a list of available target and close the connection.
     */
    DISCOVERY(TextKeyword.DISCOVERY),
    /**
     * The session is a normal session. The initiator is allowed to issue all
     * supported commands.
     */
    NORMAL(TextKeyword.NORMAL);

    /**
     * The session type as a text parameter negotiation <i>value</i> (as used in
     * <i>key-value</> pairs).
     */
    private final String value;

    /**
     * The constructor.
     * 
     * @param value
     *            the text parameter negotiation <i>value</i> (as used in
     *            <i>key-value</> pairs) describing this session type.
     */
    private SessionType(final String value) {
        this.value = value;
    }

    public final String getValue() {
        return value;
    }

    /**
     * Returns a {@link SessionType} based on the <i>value</i>, which must be
     * either <code>Discovery</code> or <code>Normal</code>. Otherwise the
     * method will return <code>null</code>.
     * 
     * @param value
     *            <code>Discovery</code> or <code>Normal</code>
     * @return the specified {@link SessionType} or <code>null</code>
     */
    public static final SessionType getSessionType(final String value) {
        final SessionType[] values = values();
        for (SessionType s : values)
            if (s.value.equals(value))
                return s;
        return null;
    }
}

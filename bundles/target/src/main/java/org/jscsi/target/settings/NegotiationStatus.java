package org.jscsi.target.settings;

import org.jscsi.target.settings.entry.Entry;

/**
 * {@link NegotiationStatus} objects specifies the necessity, progress, and
 * outcome of the parameter negotiation managed by {@link Entry} instances.
 * <p>
 * The only legal transitions of an {@link Entry} object's {@link NegotiationStatus} are from
 * {@link #NOT_NEGOTIATED} or {@link #DEFAULT} to {@link #ACCEPTED} or {@link #REJECTED}. A status of
 * {@link #IRRELEVANT} must never change.
 * 
 * @author Andreas Ergenzinger
 */
public enum NegotiationStatus {
    /**
     * The parameter has not been negotiated/declared, yet.
     */
    NOT_NEGOTIATED,
    /**
     * The parameter has not been negotiated/exchanged, yet. This exchange is
     * optional.
     */
    DEFAULT,
    /**
     * The parameter has been declared or negotiated and accepted by both sides.
     */
    ACCEPTED,
    /**
     * Attempts to negotiate the parameter have failed.
     */
    REJECTED,
    /**
     * The parameter is irrelevant.
     */
    IRRELEVANT;
}

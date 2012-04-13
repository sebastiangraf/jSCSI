package org.jscsi.target.settings;

import org.jscsi.target.settings.entry.Entry;

/**
 * The {@link NegotiationType} of an {@link Entry} affects the way the {@link Entry}'s parameter final value
 * is reached.
 * 
 * @author Andreas Ergenzinger
 */
public enum NegotiationType {
    /**
     * The parameter is declared, i.e. the iSCSI initiator will determine the
     * value and only inform the jSCSI Target about its selection.
     */
    DECLARED,
    /**
     * The parameter must be negotiated, i.e. the jSCSI Target must try to
     * select a mutually supported value and return the result to the initiator.
     */
    NEGOTIATED
}

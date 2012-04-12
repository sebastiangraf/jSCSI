package org.jscsi.target.scsi.modeSense;

/**
 * This enumeration is used to determine which header type a {@link ModeParameterList} should use.
 * 
 * @author Andreas Ergenzinger
 */
public enum HeaderType {
    /**
     * Header format required by responses to a <code>MODE
     * SENSE (6)</code> SCSI command.
     * 
     * @see ModeParameterHeader6
     */
    MODE_PARAMETER_HEADER_6,
    /**
     * Header format required by responses to a <code>MODE
     * SENSE (10)</code> SCSI command.
     * 
     * @see ModeParameterHeader10
     */
    MODE_PARAMETER_HEADER_10
}

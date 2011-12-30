package org.jscsi.target.scsi.sense;

/**
 * Describes the format of requested sense data.
 * 
 * @author Andreas Ergenzinger
 */
public enum SenseDataFormat {
    /**
     * Fixed format.
     */
    FIXED,
    /**
     * Descriptor format.
     */
    DESCRIPTOR
}

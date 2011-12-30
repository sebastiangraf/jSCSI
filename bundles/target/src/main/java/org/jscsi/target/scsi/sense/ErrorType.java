package org.jscsi.target.scsi.sense;

/**
 * Describes the type of error that makes it necessary to send of sense data to
 * the initiator.
 * 
 * @author Andreas Ergenzinger
 */
public enum ErrorType {
    /**
     * Current errors (indicated by sense response codes 70h and 72h) indicate
     * that the sense data returned is the result of an error or exception
     * condition on the task that returned the CHECK CONDITION status or a
     * protocol specific failure condition. This includes errors generated
     * during processing of the command. It also includes errors not related to
     * any command that are detected during processing of a command (e.g., disk
     * servo-mechanism failure, off-track errors, or power-up test errors).
     */
    CURRENT,
    /**
     * Deferred errors (indicated by sense response codes 71h and 73h) indicate
     * that the sense data returned is the result of an error or exception
     * condition that occurred during processing of a previous command for which
     * GOOD, CONDITION MET, INTERMEDIATE, and INTERMEDIATE-CONDITION MET status
     * has already been returned. Such commands are associated with the use of
     * the immediate bit and with some forms of caching. Device servers that
     * implement these features shall implement deferred error reporting.
     */
    DEFERRED
}

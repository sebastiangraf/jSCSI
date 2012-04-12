/**
 * 
 */
package org.jscsi.exception;

import java.util.concurrent.ExecutionException;

/**
 * Handling exceptions occuring while executing any kind of multithreaded task.
 * 
 * @author Sebastian Graf, University of Konstanz
 * 
 */
public class TaskExecutionException extends Exception {

    /**
     * Necessary for the exception.
     */
    private static final long serialVersionUID = -4387318563121826377L;

    /**
     * Simple constructor to handle {@link ExecutionException}s.
     * 
     * @param exc
     *            to be encapsulated
     */
    public TaskExecutionException(final ExecutionException exc) {
        super(exc);
    }

    /**
     * Simple constructor to handle {@link InterruptedException}s.
     * 
     * @param exc
     *            to be encapsulated
     */
    public TaskExecutionException(final InterruptedException exc) {
        super(exc);
    }

}

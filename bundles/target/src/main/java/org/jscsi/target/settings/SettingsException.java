package org.jscsi.target.settings;

/**
 * A new {@link SettingsException} must be thrown every time someone tries to
 * <i>get</i> an undefined parameter from a {@link Settings} object (i.e. an
 * attempt is made to retrieve a parameter that has neither been declared nor
 * negotiated and for which there is no default value).
 * 
 * @author Andreas Ergenzinger
 */
public final class SettingsException extends Exception {

    private static final long serialVersionUID = 2044993883966503569L;

    /**
     * Creates a new {@link SettingsException} without additional information.
     */
    public SettingsException() {
        super();
    }

    /**
     * Creates a new {@link SettingsException} with additional information.
     * 
     * @param message
     *            information about the cause of the exception
     */
    public SettingsException(final String message) {
        super(message);
    }
}

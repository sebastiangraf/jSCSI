package org.jscsi.target.settings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jscsi.target.settings.entry.Entry;

/**
 * This is an abstract parent-class to {@link ConnectionSettingsNegotiator} and
 * {@link SessionSettingsNegotiator}. The former one is in charge of all {@link Entry} objects with
 * connection-specific parameters, the latter is in
 * charge of all session-wide parameter {@link Entry} objects.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class SettingsNegotiator {

    /**
     * All {@link Entry} objects managed by this {@link SettingsNegotiator}.
     * <p>
     * These elements might change consecutively during parameter negotiations.
     */
    protected List<Entry> entries = new ArrayList<Entry>();

    /**
     * A back-up copy of the last consistent state of all elements in {@link #entries} or <code>null</code>.
     * 
     * @see #backUpEntries
     * @see #commitOrRollBackChanges(boolean)
     */
    protected List<Entry> backUpEntries;

    /**
     * Returns the first {@link Entry} in the specified {@link Collection} for
     * which {@link Entry#matchKey(String)} returns <code>true</code>.
     * 
     * @param key
     *            the key String identifying the {@link Entry}
     * @param entries
     *            a {@link Collection} of {@link Entry} objects.
     * @return a matching {@link Entry} or null, if no such {@link Entry} exists
     */
    static final Entry getEntry(final String key, final Collection<Entry> entries) {
        for (Entry e : entries)
            if (e.matchKey(key))
                return e;
        return null;
    }

    /**
     * Returns a deep copy of the passed {@link List} with exact copies of all
     * contained {@link Entry} objects.
     * 
     * @param entries
     *            the {@link List} to copy
     * @return a copy of <i>entries</i>
     */
    protected static final List<Entry> copy(final List<Entry> entries) {
        if (entries == null)
            return null;
        final ArrayList<Entry> copy = new ArrayList<Entry>();
        for (Entry e : entries)
            copy.add(e.copy());
        return copy;
    }

    /**
     * The abstract constructor.
     * <p>
     * Calls {@link #initializeEntries()}.
     */
    public SettingsNegotiator() {
        initializeEntries();
    }

    /**
     * Stores an exact copy of {@link #entries} in {@link #backUpEntries}.
     */
    protected final void backUpEntries() {
        backUpEntries = copy(entries);
    }

    /**
     * This method must be called at the end of a parameter negotiation task,
     * either to roll back any changes that must not take effect, or in order to
     * ensure that consecutive negotiations are carried out correctly.
     * 
     * @param commitChanges
     *            <code>true</code> if and only if the negotiated changes to the
     *            elements in {@link #entries} are to be remembered.
     */
    protected final void commitOrRollBackChanges(final boolean commitChanges) {
        if (commitChanges)
            for (Entry e : entries)
                // allow renegotiation where allowed
                e.resetAlreadyNegotiated();
        else
            entries = backUpEntries;// roll back
        backUpEntries = null;// won't be needed anymore
    }

    /**
     * Adds a properly initialized {@link Entry} object to {@link #entries} for
     * every parameter managed by this {@link SettingsNegotiator}.
     */
    protected abstract void initializeEntries();

    /**
     * Checks constraints that cannot be checked at the {@link Entry} level,
     * e.g. because they depend on the values of multiple parameters.
     * 
     * @return <code>true</code> if everything is fine, <code>false</code> if
     *         that is not the case
     */
    public abstract boolean checkConstraints();
}

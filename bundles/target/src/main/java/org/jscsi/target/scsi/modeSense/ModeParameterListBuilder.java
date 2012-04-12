package org.jscsi.target.scsi.modeSense;

/**
 * A builder object used during the initialization of new {@link ModeParameterList} objects.
 * 
 * @author Andreas Ergenzinger
 */
public final class ModeParameterListBuilder {

    /**
     * The {@link HeaderType} to be used by the {@link ModeParameterList}.
     */
    final HeaderType headerType;

    /**
     * Contains all elements to be included in the list of {@link LogicalBlockDescriptor} objects of the
     * {@link ModeParameterList}.
     */
    LogicalBlockDescriptor[] logicalBlockDescriptors;

    /**
     * If the {@link ModeParameterList} uses a {@link HeaderType#MODE_PARAMETER_HEADER_10} and this value is
     * <code>true</code>, then all contained MODE PARAMETER BLOCK DESCRIPTORs
     * will have the LONG LBA (LOGICAL BLOCK ADDRESS) format.
     * <p>
     * The jSCSI Target supports only the short format.
     */
    boolean longLba = false;

    /**
     * Contains all elements to be included in the list of {@link ModePage} objects of the
     * {@link ModeParameterList}.
     */
    ModePage[] modePages;

    public ModeParameterListBuilder(final HeaderType headerType) {
        this.headerType = headerType;
    }

    public void setLogicalBlockDescriptors(final ShortLogicalBlockDescriptor logicalBlockDescriptor) {
        final ShortLogicalBlockDescriptor[] array = new ShortLogicalBlockDescriptor[1];
        array[0] = logicalBlockDescriptor;
        setLogicalBlockDescriptors(array);
    }

    public void setLogicalBlockDescriptors(final ShortLogicalBlockDescriptor[] logicalBlockDescriptors) {
        this.logicalBlockDescriptors = logicalBlockDescriptors;
        longLba = false;
    }

    public void setLogicalBlockDescriptors(final LongLogicalBlockDescriptor[] logicalBlockDescriptors) {
        this.logicalBlockDescriptors = logicalBlockDescriptors;
        longLba = true;
    }

    public void setModePages(final ModePage modePage) {
        final ModePage[] array = new ModePage[1];
        array[0] = modePage;
        setModePages(array);
    }

    public void setModePages(final ModePage[] modePages) {
        this.modePages = modePages;
    }

    /**
     * This method is used for checking that all required members are
     * initialized and their respective values compatible with each other.
     * 
     * @return <code>true</code> if everything is fine, <code>false</code> if
     *         not
     */
    boolean checkIntegrity() {
        if (headerType == null)
            return false;
        if (headerType == HeaderType.MODE_PARAMETER_HEADER_6 && longLba)
            return false;
        return true;
    }
}

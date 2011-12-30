package org.jscsi.target.scsi.modeSense;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.IResponseData;

/**
 * The mode parameter list contains a header, followed by zero or more block
 * descriptors, followed by zero or more variable-length mode pages.
 * <p>
 * This class uses the builder pattern to minimize the number of constructors
 * and to avoid constructor use with a lot of <code>null</code> parameters.
 * 
 * @author Andreas Ergenzinger
 */
public final class ModeParameterList implements IResponseData {

    /**
     * The header contains information about the fields that follow.
     */
    private final ModeParameterHeader modeParameterHeader;

    /**
     * Contains the elements of the list of Logical Block Descriptors.
     */
    private final LogicalBlockDescriptor[] logicalBlockDescriptors;

    /**
     * Contains the elements of the list of Mode Pages.
     */
    private final ModePage[] modePages;

    /**
     * Returns a new {@link ModeParameterList} instance based the variables of
     * the <i>builder</i> object. If these variables are mutually exclusive,
     * then this method will return <code>null</code> instead.
     * 
     * @param builder
     *            contains all necessary information to create a
     *            {@link ModeParameterList} object
     * @return a new {@link ModeParameterList} or <code>null</code>
     */
    public static ModeParameterList build(final ModeParameterListBuilder builder) {
        if (!builder.checkIntegrity())
            return null;
        // everything is okay, so
        // - calculate block descriptor length
        int blockDescriptorLength;
        if (builder.logicalBlockDescriptors == null)
            blockDescriptorLength = 0;
        else {
            int singleLbdLength;
            if (builder.longLba)
                singleLbdLength = LongLogicalBlockDescriptor.SIZE;
            else
                singleLbdLength = ShortLogicalBlockDescriptor.SIZE;
            blockDescriptorLength = builder.logicalBlockDescriptors.length
                    * singleLbdLength;
        }
        // - calculate mode data length
        int modeDataLength;
        // -- calculate contribution of the header without bytes of MODE DATA
        // LENGTH field
        if (builder.headerType == HeaderType.MODE_PARAMETER_HEADER_6)
            modeDataLength = ModeParameterHeader6.SIZE
                    - ModeParameterHeader6.MODE_DATA_LENGTH_FIELD_SIZE;
        else
            modeDataLength = ModeParameterHeader10.SIZE
                    - ModeParameterHeader10.MODE_DATA_LENGTH_FIELD_SIZE;
        // -- add length of logical block descriptors
        modeDataLength += blockDescriptorLength;
        // -- add length of mode pages
        if (builder.modePages != null)
            for (ModePage mp : builder.modePages)
                modeDataLength += mp.size();

        /*
         * It might be nice to check the values for overflow here, however this
         * is not necessary, since the length of all available/returned elements
         * will always be less than 256.
         */

        ModeParameterHeader modeParameterHeader;
        if (builder.headerType == HeaderType.MODE_PARAMETER_HEADER_6)
            modeParameterHeader = new ModeParameterHeader6(modeDataLength,
                    blockDescriptorLength);
        else
            modeParameterHeader = new ModeParameterHeader10(modeDataLength,
                    blockDescriptorLength, builder.longLba);

        // create and return the ModeParameterList
        return new ModeParameterList(modeParameterHeader,
                builder.logicalBlockDescriptors, builder.modePages);
    }

    private ModeParameterList(final ModeParameterHeader modeParameterHeader,
            final LogicalBlockDescriptor[] logicalBlockDescriptors,
            final ModePage[] modePages) {
        this.modeParameterHeader = modeParameterHeader;
        this.logicalBlockDescriptors = logicalBlockDescriptors;
        this.modePages = modePages;
    }

    public void serialize(ByteBuffer byteBuffer, int index) {

        int offset = 0;

        // serialize header
        modeParameterHeader.serialize(byteBuffer, index);
        offset += modeParameterHeader.size();

        // serialize logical block descriptors
        if (logicalBlockDescriptors != null)
            for (LogicalBlockDescriptor lbd : logicalBlockDescriptors) {
                lbd.serialize(byteBuffer, index + offset);
                offset += lbd.size();
            }

        // serialize mode pages
        if (modePages != null)
            for (ModePage mp : modePages) {
                mp.serialize(byteBuffer, index + offset);
                offset += mp.size();
            }
    }

    public int size() {
        // size = header + logical block descriptors + mode pages
        int size = modeParameterHeader.size();
        if (logicalBlockDescriptors != null
                && logicalBlockDescriptors.length > 0)
            size += logicalBlockDescriptors[0].size()
                    * logicalBlockDescriptors.length;// all have the same size
        if (modePages != null)
            for (ModePage mp : modePages)
                size += mp.size();
        return size;
    }
}

package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.parser.scsi.SCSICommandParser;
import org.jscsi.target.scsi.sense.AdditionalSenseCodeAndQualifier;
import org.jscsi.target.scsi.sense.SenseData;
import org.jscsi.target.scsi.sense.SenseKey;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.FieldPointerSenseKeySpecificData;
import org.jscsi.target.util.BitManip;

/**
 * An abstract class for accessing the variables common to Command Descriptor
 * Blocks of all sizes.
 * <p>
 * A Command Descriptor Blocks is a blocks of multiple bytes that contains a {@link ScsiOperationCode},
 * specifying a command the SCSI initiator wants the SCSI target to perform and related fields, which
 * determine details of the ordered task.
 * <p>
 * Since all fields in the Control byte except for the Normal ACA bit are either reserved, obsolete, or vendor
 * specific, the NACA bit can be accessed directly from this class.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class CommandDescriptorBlock {

    /**
     * Not a CDB field. This array stores all {@link FieldPointerSenseKeySpecificData} that has to be sent
     * back to the
     * SCSI initiator as a reaction to the CDB {@link ByteBuffer} passed during
     * initialization of this {@link CommandDescriptorBlock} instance.
     * <p>
     * If, during the parsing process, an illegal value is detected, the array will be initialized and a
     * {@link FieldPointerSenseKeySpecificData} object, specifying the position of the illegal field, will be
     * added.
     */
    private FieldPointerSenseKeySpecificData[] illegalFieldPointers = null;

    /**
     * Determines which command to perform.
     */
    private ScsiOperationCode scsiOperationCode;

    /**
     * Specifies if Normal ACA shall be used. A value of <code>true</code> is
     * not supported by the jSCSI Target.
     */
    private boolean normalAutoContingentAllegiance;

    /**
     * The abstract constructor.
     * <p>
     * Deserializes the first byte containing the SCSI Operation Code and the Control byte.
     * <p>
     * The passed {@link ByteBuffer} parameter <b>must</b> have a capacity &ge; the length of the specific
     * command descriptor block. Since this is assured by the {@link SCSICommandParser#getCDB()} method, which
     * always returns {@link ByteBuffer} objects with capacity 16, no checks will be performed.
     * 
     * @param buffer
     *            contains the serialized CDB starting at index position zero
     */
    public CommandDescriptorBlock(ByteBuffer buffer) {
        // SCSI OpCode
        scsiOperationCode = ScsiOperationCode.valueOf(buffer.get(0));
        if (scsiOperationCode == null)// unsupported OpCode
            addIllegalFieldPointer(0);
        /*
         * The above if block should never be entered, since unsupported
         * operation codes in the CDB would have been discovered during
         * TargetFullFeatureStage selection in TargetFullFeaturePhase.execute().
         */

        // Control (NACA bit, all other fields are obsolete, reserved, or vendor
        // specific)
        final CdbType cdbType = scsiOperationCode.getCdbType();
        int controlByteIndex;

        switch (cdbType) {
        case SIX_BYTE_COMMANDS:
            controlByteIndex = 5;
            break;
        case TEN_BYTE_COMMANDS:
            controlByteIndex = 9;
            break;
        case TWELVE_BYTE_COMMANDS:
            controlByteIndex = 11;
            break;
        case SIXTEEN_BYTE_COMMANDS:
            controlByteIndex = 15;
            break;
        default:
            controlByteIndex = -1;
            /*
             * Would lead to ArrayOutOfBoundsException, however, this will not
             * happen, since unsupported group codes will be filtered in
             * TargetFullFeaturePhase.execute() (see above).
             */
        }

        normalAutoContingentAllegiance = BitManip.getBit(buffer.get(controlByteIndex), 2);
        if (normalAutoContingentAllegiance) {// normalACA is not supported
            addIllegalFieldPointer(controlByteIndex, 2);
        }
    }

    public final ScsiOperationCode getScsiOperationCode() {
        return scsiOperationCode;
    }

    /**
     * The NACA (Normal ACA) bit specifies whether an auto contingent allegiance
     * (ACA) is established if the command terminates with CHECK CONDITION
     * status. A NACA bit set to one specifies that an ACA shall be established.
     * A NACA bit set to zero specifies that an ACA shall not be established.
     * 
     * @return <code>true</code> if the Normal ACA bit in the CDB's Control byte
     *         is set and <code>false</code> if it is not.
     */
    public final boolean isNormalACA() {
        return normalAutoContingentAllegiance;
    }

    /**
     * Adds an instance {@link FieldPointerSenseKeySpecificData}, which
     * specifies an illegal field by the position of its first byte, to {@link #illegalFieldPointers}.
     * 
     * @param byteNumber
     *            index of the first byte of the illegal field
     */
    protected final void addIllegalFieldPointer(int byteNumber) {
        final FieldPointerSenseKeySpecificData fp = new FieldPointerSenseKeySpecificData(true,// senseKeySpecificDataValid
            true,// commandData (i.e. invalid field in CDB)
            false,// bitPointerValid
            0,// bitPointer
            byteNumber);// fieldPointer
        addIllegalFieldPointer(fp);
    }

    /**
     * Adds an instance {@link FieldPointerSenseKeySpecificData}, which
     * specifies an illegal field by the position of its first byte and its
     * first bit, to {@link #illegalFieldPointers}.
     * 
     * @param byteNumber
     *            index of the first byte of the illegal field
     * @param bitNumber
     *            index of the first bit of the illegal field
     */
    protected final void addIllegalFieldPointer(int byteNumber, int bitNumber) {
        FieldPointerSenseKeySpecificData fp = new FieldPointerSenseKeySpecificData(true,// senseKeySpecificDataValid
            true,// commandData (i.e. invalid field in CDB)
            true,// bitPointerValid
            bitNumber,// bitPointer
            byteNumber);// fieldPointer
        addIllegalFieldPointer(fp);
    }

    /**
     * Adds a {@link FieldPointerSenseKeySpecificData} object to {@link #illegalFieldPointers}. Initializes
     * and grows the array if
     * necessary.
     * 
     * @param illegalFieldPointer
     *            the object to add
     */
    private final void addIllegalFieldPointer(final FieldPointerSenseKeySpecificData illegalFieldPointer) {
        // grow array?
        if (illegalFieldPointers == null)
            illegalFieldPointers = new FieldPointerSenseKeySpecificData[10];
        final int size = getIllegalFieldPointerSize();
        if (size >= illegalFieldPointers.length) {
            // grow
            FieldPointerSenseKeySpecificData[] temp =
                new FieldPointerSenseKeySpecificData[illegalFieldPointers.length + 1];
            for (int i = 0; i < size; ++i) {
                temp[i] = illegalFieldPointers[i];
            }
            illegalFieldPointers = temp;
        }
        // add new element
        illegalFieldPointers[size] = illegalFieldPointer;
    }

    /**
     * Returns the number of elements stored in {@link #illegalFieldPointers}.
     * 
     * @return the number of elements stored in {@link #illegalFieldPointers}
     */
    private final int getIllegalFieldPointerSize() {
        if (illegalFieldPointers == null)
            return 0;
        int size = 0;
        while (size < illegalFieldPointers.length) {
            if (illegalFieldPointers[size] != null)
                ++size;
            else
                break;
        }
        return size;
    }

    /**
     * Returns <code>null</code> if there were no illegal fields in the
     * serialized CDB passed to the constructor, or an array of appropriate
     * {@link FieldPointerSenseKeySpecificData}, that have to be enclosed in the {@link SenseData} returned to
     * the initiator (with sense key {@link SenseKey#ILLEGAL_REQUEST} and additional sense code and sense code
     * qualifier {@link AdditionalSenseCodeAndQualifier#INVALID_FIELD_IN_CDB}).
     * 
     * @return <code>null</code> or an array of appropriate {@link FieldPointerSenseKeySpecificData}
     */
    public final FieldPointerSenseKeySpecificData[] getIllegalFieldPointers() {
        if (illegalFieldPointers == null)
            return null;
        // returned trimmed array without null values
        final int size = getIllegalFieldPointerSize();
        FieldPointerSenseKeySpecificData[] result = new FieldPointerSenseKeySpecificData[size];
        for (int i = 0; i < size; ++i) {
            result[i] = illegalFieldPointers[i];
        }
        return result;
    }
}

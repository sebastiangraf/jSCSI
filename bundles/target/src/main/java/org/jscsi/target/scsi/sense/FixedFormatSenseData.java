package org.jscsi.target.scsi.sense;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.cdb.ScsiOperationCode;
import org.jscsi.target.scsi.sense.information.FourByteInformation;
import org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific.SenseKeySpecificData;
import org.jscsi.target.util.BitManip;
import org.jscsi.target.util.ReadWrite;

/**
 * Instances of this class represent sense data using the fixed format.
 * 
 * @see SenseDataFormat#FIXED
 * @author Andreas Ergenzinger
 */
public class FixedFormatSenseData extends SenseData {

    /**
     * The position of the INFORMATION field.
     */
    private static final int INFORMATION_FIELD_INDEX = 3;

    /**
     * The position of the ADDITIONAL SENSE LENGTH field.
     */
    private static final int ADDITIONAL_SENSE_LENGTH_INDEX = 7;

    /**
     * The position of the COMMAND SPECIFIC INFORMATION field.
     */
    private static final int COMMAND_SPECIFIC_INFORMATION_FIELD_INDEX = 8;

    /**
     * The position of the ADDITIONAL SENSE CODE field.
     */
    private static final int ADDITIONAL_SENSE_CODE_INDEX = 12;

    /**
     * The position of the FIELD REPLACEABLE UNIT CODE field.
     */
    private static final int FIELD_REPLACEABLE_UNIT_CODE_INDEX = 14;

    /**
     * The position of the SENSE KEY SPECIFIC DATA field.
     */
    private static final int SENSE_KEY_SPECIFIC_DATA_INDEX = 15;

    /**
     * The minimum length in bytes of a serialized fixed format sense data
     * object.
     */
    private static final int MIN_SIZE = 18;

    /**
     * The minimum value of the ADDITIONAL SENSE LENGTH field.
     */
    private static final int MIN_ADDITIONAL_SENSE_LENGTH = 10;

    /**
     * A VALID bit set to <code>false</code> indicates that the {@link #information} field is not defined in
     * SPC or any other command
     * standard. A VALID bit set to <code>true</code> indicates the INFORMATION
     * field contains valid information as defined in the SPC or a command
     * standard.
     */
    private final boolean valid;

    /**
     * See the SSC-2 READ and SPACE commands for examples of FILEMARK bit usage.
     */
    private final boolean fileMark;

    /**
     * See the SSC-2 READ, SPACE, and WRITE commands for examples of
     * end-of-medium (EOM) bit usage.
     */
    private final boolean endOfMedium;

    /**
     * See the SBC-2 READ LONG, SBC-2 WRITE LONG, and SSC-2 READ commands and
     * for examples of incorrect length indicator (ILI) bit usage.
     */
    private final boolean incorrectLengthIndicator;

    /**
     * If {@link #valid} == <code>true</code> the INFORMATION field contains
     * valid information as defined in the SPC or SBC standard.
     */
    private final FourByteInformation information;

    /**
     * Command-specific information, if any.
     */
    private final FourByteInformation commandSpecificInformation;

    /**
     * Non-zero values in the FIELD REPLACEABLE UNIT CODE field are used to
     * identify a component that has failed. A value of zero in this field
     * indicates that no specific component has been identified to have failed
     * or that the data is not available.
     * <p>
     * The format of this information is not specified by the SPC. Additional information about the field
     * replaceable unit may be available in the ASCII Information VPD page, if supported by the device server.
     */
    private final byte fieldReplaceableUnitCode;

    /**
     * The SenseKeySpecificData sub-class MUST match the senseKey.
     * 
     * @see SenseKeySpecificData
     */
    private final SenseKeySpecificData senseKeySpecificData;

    /**
     * The additional sense bytes may contain vendor specific data that further
     * defines the nature of the exception condition.
     */
    private final AdditionalSenseBytes additionalSenseBytes;

    /**
     * The {@link #additionalSenseLength} field indicates the number of
     * additional sense bytes that follow. The additional sense length shall be
     * less than or equal to 244 (i.e., limiting the total length of the sense
     * data to 252 bytes). If the sense data is being returned as parameter data
     * by a {@link ScsiOperationCode#REQUEST_SENSE} command, then the
     * relationship between the {@link #additionalSenseLength} field and the CDB
     * ALLOCATION LENGTH field is defined in SPC-3.
     */
    final private int additionalSenseLength;

    /**
     * The constructor. All parameters without additional description are used
     * to initialize the member variables with the same name.
     * 
     * @param valid
     * @param errorType
     *            the type of error that necessitated the sending of sense data
     * @param fileMark
     * @param endOfMedium
     * @param incorrectLengthIndicator
     * @param senseKey
     * @param information
     * @param commandSpecificInformation
     * @param additionalSenseCodeAndQualifier
     * @param fieldReplaceableUnitCode
     * @param senseKeySpecificData
     * @param additionalSenseBytes
     */
    public FixedFormatSenseData(final boolean valid, final ErrorType errorType, final boolean fileMark,
        final boolean endOfMedium, final boolean incorrectLengthIndicator, final SenseKey senseKey,
        final FourByteInformation information, final FourByteInformation commandSpecificInformation,
        final AdditionalSenseCodeAndQualifier additionalSenseCodeAndQualifier,
        final byte fieldReplaceableUnitCode, final SenseKeySpecificData senseKeySpecificData,
        final AdditionalSenseBytes additionalSenseBytes) {
        super(errorType, SenseDataFormat.FIXED, senseKey, additionalSenseCodeAndQualifier);
        this.valid = valid;
        this.fileMark = fileMark;
        this.endOfMedium = endOfMedium;
        this.incorrectLengthIndicator = incorrectLengthIndicator;
        this.information = information;
        this.commandSpecificInformation = commandSpecificInformation;
        this.fieldReplaceableUnitCode = fieldReplaceableUnitCode;
        this.senseKeySpecificData = senseKeySpecificData;
        this.additionalSenseBytes = additionalSenseBytes;
        // additional sense length
        int asl = MIN_ADDITIONAL_SENSE_LENGTH;
        if (additionalSenseBytes != null)
            asl += additionalSenseBytes.size();
        additionalSenseLength = asl;
    }

    public void serialize(ByteBuffer byteBuffer, int index) {

        byteBuffer.position(index);

        // *** byte 0 ***
        // response code
        byte b = (byte)getReponseCodeFor(errorType, SenseDataFormat.FIXED);
        // valid flag
        b = BitManip.getByteWithBitSet(b, 7, valid);
        byteBuffer.put(b);

        // *** byte 1 - is obsolete ***
        byteBuffer.put((byte)0);

        // *** byte 2 ***
        // file mark
        b = BitManip.getByteWithBitSet((byte)0, 7, fileMark);

        // EOM
        b = BitManip.getByteWithBitSet(b, 6, endOfMedium);

        // ILI
        b = BitManip.getByteWithBitSet(b, 5, incorrectLengthIndicator);

        // sense key
        b = (byte)(15 & senseKey.getValue());
        byteBuffer.put(b);

        // *** bytes 3 - 6 ***
        // information
        if (information != null)
            information.serialize(byteBuffer, index + INFORMATION_FIELD_INDEX);

        // additional sense length
        byteBuffer.put(index + ADDITIONAL_SENSE_LENGTH_INDEX, (byte)additionalSenseLength);

        // command specific information
        if (commandSpecificInformation != null)
            commandSpecificInformation
                .serialize(byteBuffer, index + COMMAND_SPECIFIC_INFORMATION_FIELD_INDEX);

        // additional sense code and additional sense code qualifier
        ReadWrite.writeTwoByteInt(byteBuffer, additionalSenseCodeAndQualifier.getValue(), index
            + ADDITIONAL_SENSE_CODE_INDEX);

        // field replaceable unit code
        byteBuffer.put(FIELD_REPLACEABLE_UNIT_CODE_INDEX, fieldReplaceableUnitCode);

        // sense key specific data
        if (senseKeySpecificData != null)
            senseKeySpecificData.serialize(byteBuffer, index + SENSE_KEY_SPECIFIC_DATA_INDEX);

        // additional sense bytes
        if (additionalSenseBytes != null)
            additionalSenseBytes.serialize(byteBuffer, index + MIN_SIZE);
    }

    public final int getAdditionalSenseLength() {
        return additionalSenseLength;
    }

    public int size() {
        int size = MIN_SIZE;
        if (additionalSenseBytes != null)
            size += additionalSenseBytes.size();
        return size;
    }
}

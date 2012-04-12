package org.jscsi.target.scsi.sense;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.sense.senseDataDescriptor.SenseDataDescriptor;
import org.jscsi.target.util.BitManip;
import org.jscsi.target.util.ReadWrite;

/**
 * Instances of this class represent sense data using the descriptor format.
 * 
 * @see SenseDataFormat#DESCRIPTOR
 * @author Andreas Ergenzinger
 */
public final class DescriptorFormatSenseData extends SenseData {

    /**
     * The length in bytes of all fixed fields of descriptor format sense data.
     */
    private static final int HEADER_LENGTH = 8;

    /**
     * The byte position of the ADDITIONAL SENSE CODE field.
     */
    private static final int ADDITIONAL_SENSE_CODE_INDEX = 2;

    /**
     * The position of the first reserved byte.
     * 
     * @see #RESERVED_BYTES_MAX_INDEX
     */
    private static final int RESERVED_BYTES_MIN_INDEX = 4;

    /**
     * The position of the last reserved byte.
     * 
     * @see #RESERVED_BYTES_MIN_INDEX
     */
    private static final int RESERVED_BYTES_MAX_INDEX = 6;

    /**
     * The position of the ADDITIONAL SENSE LENGTH field.
     */
    private static final int ADDITIONAL_SENSE_LENGTH_INDEX = 7;

    /**
     * All sense data descriptors that are a part of this sense data object.
     */
    private final SenseDataDescriptor[] senseDataDescriptors;

    /**
     * The constructor.
     * 
     * @param errorType
     *            the error type
     * @param senseKey
     *            a general description of what caused the error
     * @param additionalSenseCodeAndQualifier
     *            a more specific description of the error
     * @param senseDataDescriptors
     *            more specific error information
     */
    public DescriptorFormatSenseData(final ErrorType errorType, final SenseKey senseKey,
        AdditionalSenseCodeAndQualifier additionalSenseCodeAndQualifier,
        SenseDataDescriptor... senseDataDescriptors) {
        super(errorType, SenseDataFormat.DESCRIPTOR, senseKey, additionalSenseCodeAndQualifier);
        this.senseDataDescriptors = senseDataDescriptors;
    }

    public void serialize(ByteBuffer byteBuffer, int index) {

        byteBuffer.position(index);

        // response code and valid
        byte b = (byte)getReponseCodeFor(errorType, SenseDataFormat.DESCRIPTOR);
        b = BitManip.getByteWithBitSet(b, 7, false);// bit 7 is reserved
        byteBuffer.put(b);// index

        // sense key
        b = (byte)(senseKey.getValue() & 15);
        byteBuffer.put(b);// index + 1

        // additional sense code and additional sense code qualifier
        ReadWrite.writeTwoByteInt(byteBuffer, additionalSenseCodeAndQualifier.getValue(), index
            + ADDITIONAL_SENSE_CODE_INDEX);

        // bytes 4-6 are reserved
        for (int i = index + RESERVED_BYTES_MIN_INDEX; i < index + RESERVED_BYTES_MAX_INDEX; ++i)
            byteBuffer.put(i, (byte)0);

        // additional sense length
        byteBuffer.put(index + ADDITIONAL_SENSE_LENGTH_INDEX, (byte)getAdditionalSenseLength());

        // sense data descriptors
        int descriptorIndex = HEADER_LENGTH;
        for (int i = 0; i < senseDataDescriptors.length; ++i) {
            if (senseDataDescriptors[i] != null) {
                senseDataDescriptors[i].serialize(byteBuffer, descriptorIndex);
                descriptorIndex += senseDataDescriptors[i].size();
            }
        }
    }

    /**
     * Returns the value of the ADDITIONAL SENSE LENGTH field.
     * <p>
     * This is the total length of all included {@link SenseDataDescriptor} objects.
     * 
     * @return the value of the ADDITIONAL SENSE LENGTH field
     */
    private int getAdditionalSenseLength() {
        int additionalSenseLength = 0;
        if (senseDataDescriptors != null) {
            for (int i = 0; i < senseDataDescriptors.length; ++i)
                if (senseDataDescriptors[i] != null)
                    additionalSenseLength += senseDataDescriptors[i].size();
        }
        return additionalSenseLength;
    }

    public int size() {
        return getAdditionalSenseLength()// is never negative
            + HEADER_LENGTH;
    }

}

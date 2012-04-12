package org.jscsi.target.scsi.sense.senseDataDescriptor;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.ISerializable;

/**
 * Sense data descriptors are part of sense data and provide specific sense
 * information. A given type of sense data descriptor shall be included in the
 * sense data only when the information it contains is valid.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class SenseDataDescriptor implements ISerializable {

    /**
     * This value plus the length of all sub-class-specific fields equal the
     * length in bytes of the serialized sense data descriptor.
     */
    private static final int COMMON_FIELDS_LENGTH = 2;

    /**
     * The descriptor type field determines sense data descriptor's
     * (non-abstract) class.
     * 
     * @see SenseDataDescriptorType
     */
    private final SenseDataDescriptorType descriptorType;

    /**
     * The ADDITIONAL LENGTH field indicates the number of sense data descriptor
     * specific bytes that follow in the sense data descriptor.
     */
    private final int additionalLength;

    /**
     * The abstract constructor.
     * 
     * @param descriptorType
     *            determines the specific type of the sense data descriptor
     * @param additionalLength
     *            the length in bytes of all additional fields
     * @see #COMMON_FIELDS_LENGTH
     */
    public SenseDataDescriptor(final SenseDataDescriptorType descriptorType, final int additionalLength) {
        this.descriptorType = descriptorType;
        this.additionalLength = additionalLength;
    }

    /**
     * Serializes the fields common to all sense data descriptors.
     * 
     * @param byteBuffer
     *            where the serialized fields will be stored
     * @param index
     *            the position of the first byte of the sense data descriptor in
     *            the {@link ByteBuffer}
     */
    private final void serializeCommonFields(final ByteBuffer byteBuffer, final int index) {
        byteBuffer.position(index);
        byteBuffer.put(descriptorType.getValue());
        byteBuffer.put((byte)additionalLength);
    }

    /**
     * Serializes all fields which are not common to all sense data descriptors,
     * which means those that are sub-type-specific.
     * 
     * @param byteBuffer
     *            where the serialized fields will be stored
     * @param index
     *            the position of the first byte of the sense data descriptor in
     *            the {@link ByteBuffer}
     */
    protected abstract void serializeSpecificFields(ByteBuffer byteBuffer, final int index);

    private final int getAdditionalLength() {
        return additionalLength;
    }

    public void serialize(ByteBuffer byteBuffer, int index) {
        serializeCommonFields(byteBuffer, index);
        serializeSpecificFields(byteBuffer, index);
    }

    public int size() {
        return COMMON_FIELDS_LENGTH + getAdditionalLength();
    }
}

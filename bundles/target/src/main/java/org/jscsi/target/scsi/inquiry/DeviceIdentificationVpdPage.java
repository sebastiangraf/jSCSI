package org.jscsi.target.scsi.inquiry;

import java.nio.ByteBuffer;

import org.jscsi.target.TargetServer;
import org.jscsi.target.scsi.IResponseData;
import org.jscsi.target.util.ReadWrite;

/**
 * The Device Identification VPD page provides the means to retrieve
 * identification descriptors applying to the logical unit. Logical units may
 * have more than one identification descriptor (e.g., if several types or
 * associations of identifier are supported).
 * <p>
 * Device identifiers consist of one or more of the following:
 * <ul>
 * <li>Logical unit names</li>
 * <li>SCSI target port identifiers</li>
 * <li>SCSI target port names</li>
 * <li>SCSI target device names</li>
 * <li>Relative target port identifiers</li>
 * <li>SCSI target port group number or</li>
 * <li>Logical unit group number.</li>
 * </ul>
 * Identification descriptors shall be assigned to the peripheral device (e.g.,
 * a disk drive) and not to the currently mounted media, in the case of
 * removable media devices. Operating systems are expected to use the
 * identification descriptors during system configuration activities to
 * determine whether alternate paths exist for the same peripheral device.
 * <p>
 * This class uses the singleton pattern since the content of the DEVICE
 * IDENTIFICATION VPD PAGE will never change.
 * 
 * @author Andreas Ergenzinger
 */
public class DeviceIdentificationVpdPage implements IResponseData {

    /**
     * The total length of all mandatory fields in bytes.
     */
    private static final int HEADER_LENGTH = 4;

    /**
     * The position of the PAGE LENGTH field's most significant byte.
     */
    private static final int PAGE_LENGTH_FIELD_INDEX = 2;

    /**
     * The joint content of the PERIPHERAL QUALIFIER and the PERIPHERAL DEVICE
     * TYPE fields with a total length of one byte.
     * <p>
     * These values have the following meaning:
     * <p>
     * A peripheral device having the specified peripheral device type is
     * connected to this logical unit. If the device server is unable to
     * determine whether or not a peripheral device is connected, it also shall
     * use this peripheral qualifier. This peripheral qualifier does not mean
     * that the peripheral device connected to the logical unit is ready for
     * access.
     * <p>
     * The Logical Unit is a direct access block device (e.g., magnetic disk).
     */
    private final byte peripheralQualifierAndPeripheralDeviceType = 0;

    /**
     * Identifies this PAGE as a DEVICE IDENTIFICATION VPD PAGE.
     */
    private final byte pageCode = (byte) 0x83;

    private TargetServer target;
    private IdentificationDescriptor[] identificationDescriptors = new IdentificationDescriptor[0];
    
    public DeviceIdentificationVpdPage(TargetServer target) {
        this.target = target;

    }


    /**
     * Returns the combined length of all contained IDENTIFICATION DESCRIPTORs.
     * 
     * @return the combined length of all contained IDENTIFICATION DESCRIPTORs
     */
    private short getPageLength() {
        short pageLength = 0;
        for (int i = 0; i < identificationDescriptors.length; ++i) {
            pageLength += (identificationDescriptors[i].size());
        }
        return pageLength;
    }

    public void serialize(ByteBuffer byteBuffer, int index) {
        // serialize header
        byteBuffer.position(index);
        byteBuffer.put(peripheralQualifierAndPeripheralDeviceType);
        byteBuffer.put(pageCode);
        
        /*
         * For each logical unit that is not a well known logical unit, the
         * Device Identification VPD page shall include at least one
         * identification descriptor in which a logical unit name (see SAM-3) is
         * indicated.
         */
        final ProtocolIdentifier protocolIdentifier = ProtocolIdentifier.INTERNET_SCSI;
        final CodeSet codeSet = CodeSet.UTF8_CODES;
        final boolean protocolIdentifierValid = true;
        final Association association = Association.SCSI_TARGET_DEVICE;
        final IdentifierType identifierType = IdentifierType.SCSI_NAME_STRING;

        String [] targetNames = target.getTargetNames();
        identificationDescriptors = new IdentificationDescriptor[targetNames.length];
        for (int curTargetNum = 0; curTargetNum < targetNames.length; curTargetNum++)
        {
            final IdentificationDescriptor identDescriptor = new IdentificationDescriptor(
                    protocolIdentifier, codeSet, protocolIdentifierValid,
                    association, identifierType,
                    new ScsiNameStringIdentifier(targetNames[curTargetNum]));


            identificationDescriptors[curTargetNum] = identDescriptor;
        }
        ReadWrite.writeInt(getPageLength(),// value
                byteBuffer,// buffer
                index + PAGE_LENGTH_FIELD_INDEX);// index

        // identification descriptor list
        int iddIndex = index + HEADER_LENGTH;// index of the current ident.
                                             // descr.
        for (int i = 0; i < identificationDescriptors.length; ++i) {
            identificationDescriptors[i].serialize(byteBuffer, iddIndex);
            iddIndex += identificationDescriptors[i].size();
        }
    }

    public int size() {
        return getPageLength() + HEADER_LENGTH;
    }

}

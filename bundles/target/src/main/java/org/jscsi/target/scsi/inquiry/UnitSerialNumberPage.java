package org.jscsi.target.scsi.inquiry;


import java.nio.ByteBuffer;

import org.jscsi.target.scsi.IResponseData;


/**
 * The Unit Serial Number page provides a product serial number for the target or logical unit.
 * <p>
 * This class uses the singleton pattern since the content of the Unit Serial Number page will never change.
 *
 * @author CHEN Qingcan
 */
public class UnitSerialNumberPage implements IResponseData {

    /**
     * The singleton.
     */
    private static UnitSerialNumberPage instance;

    /**
     * Returns the singleton.
     *
     * @return the singleton
     */
    public static synchronized UnitSerialNumberPage getInstance () {
        if (instance == null) instance = new UnitSerialNumberPage ();
        return instance;
    }

    private UnitSerialNumberPage () {
        // private due to singleton pattern
    }

    private static final byte PAGE_CODE     = (byte) 0x80;
    private static final byte PAGE_LENGTH   = (byte) 0x08;

    @Override
    public void serialize (ByteBuffer out, int index) {
        out.position (index);
        // PERIPHERAL QUALIFIER and PERIPHERAL DEVICE TYPE
        // See DeviceIdentificationVpdPage.peripheralQualifierAndPeripheralDeviceType for details.
        out.put ((byte) 0);
        // PAGE CODE (80h)
        out.put (PAGE_CODE);
        // Reserved
        out.put ((byte) 0);
        // PAGE LENGTH
        // The PAGE LENGTH field specifies the length in bytes of the product serial number page.
        // Older products that only support the Product Serial Number parameter will have a page length of 08h,
        // while newer products that support both parameters will have a page length of 14h.
        out.put (PAGE_LENGTH);
        // Product Serial Number
        // The Product Serial Number field contains ASCII data that is vendor-assigned serial number.
        // The least significant ASCII character of the serial number shall appear as the last byte in the Data-In Buffer.
        // If the product serial number is not available, the target shall return ASCII spaces (20h) in this field.
        for (int i = 0 ; i < PAGE_LENGTH ; i++) out.put ((byte) 0x20);
    }

    @Override
    public int size () {
        return 4 + PAGE_LENGTH;
    }

}

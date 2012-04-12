package org.jscsi.target.scsi.sense.senseDataDescriptor.senseKeySpecific;

import java.nio.ByteBuffer;

/**
 * This kind of sense-key-specific data can be used instead of all the other {@link SenseKeySpecificData}
 * subclasses, if no valid sense-key-specific data
 * is to be transmitted to the SCSI initiator.
 * <p>
 * All serialized objects of this class proclaims to contain no valid data.
 * 
 * @author Andreas Ergenzinger
 */
public class InvalidSenseKeySpecificData extends SenseKeySpecificData {

    public InvalidSenseKeySpecificData() {
        super(false);// sense key specific data not valid
    }

    @Override
    protected void serializeSpecificFields(ByteBuffer byteBuffer, int index) {
        // do nothing
    }

}

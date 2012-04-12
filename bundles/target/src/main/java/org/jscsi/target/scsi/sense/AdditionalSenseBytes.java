package org.jscsi.target.scsi.sense;

import java.nio.ByteBuffer;

import org.jscsi.target.scsi.ISerializable;

/**
 * In additional to the fixed fields that are part of both fixed format and
 * descriptor format sense data, additional data can be included in the optional
 * ADDITIONAL SENSE BYTES field.
 * <p>
 * Since the jSCSI Target currently does not have any additional information to send, the serialized length in
 * bytes of all ADDITTIONAL SENSE BYTES objects is fixed to zero.
 * 
 * @author Andreas Ergenzinger
 */
public final class AdditionalSenseBytes implements ISerializable {

    private static final int SIZE = 0;

    public void serialize(ByteBuffer byteBuffer, int index) {
        // do nothing
    }

    public int size() {
        return SIZE;
    }

}

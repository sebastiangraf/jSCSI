package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.target.util.BitManip;

/**
 * This class represents Command Descriptor Blocks for the <code>READ CAPACITY (16)</code> SCSI command.
 * 
 * @author Andreas Ergenzinger
 */
public class ReadCapacity16Cdb extends ReadCapacityCdb {

    /**
     * The mandatory value of the SERVICE ACTION field.
     */
    private static final byte SERVICE_ACTION = 0x10;

    /**
     * The value of the SERVICE ACTION field.
     * <p>
     * The value of this 5-bit field must equal {@link #SERVICE_ACTION}. Its meaning is described in SPC-4.
     */
    private final byte serviceAction;

    public ReadCapacity16Cdb(ByteBuffer buffer) {
        super(buffer);
        // deserialize SERVICE ACTION field
        serviceAction = (byte)(buffer.get(1) & 31);
        if (serviceAction != SERVICE_ACTION)
            addIllegalFieldPointer(1, 4);
    }

    @Override
    protected long deserializeLogicalBlockAddress(ByteBuffer buffer) {
        return buffer.getLong(2);
    }

    @Override
    protected boolean deserializePartialMediumIndicator(ByteBuffer buffer) {
        return BitManip.getBit(buffer.get(14),// byte
            0);// bitNumber
    }

    public byte getServiceAction() {
        return serviceAction;
    }
}

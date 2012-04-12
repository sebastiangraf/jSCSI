package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.target.util.BitManip;
import org.jscsi.target.util.ReadWrite;

/**
 * This class represents Command Descriptor Blocks for the <code>READ CAPACITY (10)</code> SCSI command.
 * 
 * @author Andreas Ergenzinger
 */
public class ReadCapacity10Cdb extends ReadCapacityCdb {

    public ReadCapacity10Cdb(ByteBuffer buffer) {
        super(buffer);
    }

    @Override
    protected long deserializeLogicalBlockAddress(ByteBuffer buffer) {
        return ReadWrite.readUnsignedInt(buffer, 2);
    }

    @Override
    protected boolean deserializePartialMediumIndicator(ByteBuffer buffer) {
        return BitManip.getBit(buffer.get(8),// byte
            0);// bitNumber
    }

}

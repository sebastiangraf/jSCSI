package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.target.util.BitManip;

/**
 * This class represents Command Descriptor Blocks for the <code>REQUEST SENSE</code> SCSI command, which
 * requests that the device
 * server transfers sense data to the application client.
 * 
 * @author Andreas Ergenzinger
 */
public final class RequestSenseCdb extends CommandDescriptorBlock {

    /**
     * The descriptor format (DESC) bit specifies which sense data format shall
     * be returned. If DESC is set to zero, fixed format sense data shall be
     * returned. If DESC is set to one and descriptor format sense data is
     * supported, descriptor format sense data shall be returned.
     */
    private final boolean descriptorFormat;

    /**
     * The ALLOCATION LENGTH field specifies the maximum number of bytes that an
     * application client has allocated in the Data-In Buffer.
     */
    private final int allocationLength;

    public RequestSenseCdb(ByteBuffer buffer) {
        super(buffer);

        // descriptor format
        descriptorFormat = BitManip.getBit(buffer.get(1), 0);

        // allocation length
        allocationLength = buffer.get(4) & 255;
    }

    public final boolean getDescriptorFormat() {
        return descriptorFormat;
    }

    public final int getAllocationLength() {
        return allocationLength;
    }
}

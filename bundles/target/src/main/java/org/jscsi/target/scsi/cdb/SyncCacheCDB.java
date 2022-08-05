package org.jscsi.target.scsi.cdb;


import java.nio.ByteBuffer;

import org.jscsi.exception.InternetSCSIException;
import org.jscsi.target.util.ReadWrite;


/**
 * This class represents Command Descriptor Blocks for the <code>SYNCHRONIZE CACHE (10), (16)</code> SCSI command.
 *
 * @author CHEN Qingcan
 */
public class SyncCacheCDB extends CommandDescriptorBlock {

    private final byte inOperationCode;
    private final long inLogicalBlockAddress;
    private final int  inNumberOfBlocks;

    public SyncCacheCDB (ByteBuffer buffer)
    throws InternetSCSIException {
        super (buffer);
        inOperationCode = (byte) ReadWrite.readOneByteInt (buffer, 0);
        if (inOperationCode == ScsiOperationCode.SYNCHRONIZE_CACHE_16.value ()) {
            inLogicalBlockAddress = ReadWrite.readUnsignedInt  (buffer, 2);
            inNumberOfBlocks      = ReadWrite.readTwoByteInt   (buffer, 7);
        } else if (inOperationCode == ScsiOperationCode.SYNCHRONIZE_CACHE_10.value ()) {
            inLogicalBlockAddress = ReadWrite.readEightByteInt (buffer, 2);
            inNumberOfBlocks      = ReadWrite.readFourByteInt  (buffer, 10);
        } else {
            throw new InternetSCSIException("SYNCHRONIZE_CACHE only supports (10) and (16) but request is " +
                                            Integer.toHexString (inOperationCode) + "h.");
        }
    }

    /** LOGICAL BLOCK ADDRESS field */
    public long getLogicalBlockAddress () {
        return inLogicalBlockAddress;
    }

    /** NUMBER OF BLOCKS field */
    public int getNumberOfBlocks () {
        return inNumberOfBlocks;
    }

}

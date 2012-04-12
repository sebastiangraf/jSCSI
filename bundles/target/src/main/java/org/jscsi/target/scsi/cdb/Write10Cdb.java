package org.jscsi.target.scsi.cdb;

import java.nio.ByteBuffer;

import org.jscsi.target.util.BitManip;
import org.jscsi.target.util.ReadWrite;

/**
 * This class represents Command Descriptor Blocks for the <code>WRITE (6)</code> SCSI command.
 * 
 * @author Andreas Ergenzinger
 */
public class Write10Cdb extends WriteCdb {

    /**
     * The value of the WRPROTECT field determines which checks on the
     * protection information resulting from the attempted write operation to
     * the medium the target shall perform before returning status for the
     * command associated with this command descriptor block.
     * <p>
     * Since the jSCSI Target simulates a logical unit formatted without any protection information (that can
     * be checked), the value of this field is inconsequential.
     */
    private final int writeProtect;

    /**
     * This variable represents the value of the DPO bit. Since the jSCSI Target
     * does not support advanced caching strategies, the value of this variable
     * is ignored.
     * <p>
     * A disable page out (DPO) bit set to zero specifies that the retention priority shall be determined by
     * the RETENTION PRIORITY fields in the Caching mode page (see 6.4.5). A DPO bit set to one specifies that
     * the device server shall assign the logical blocks accessed by this command the lowest retention
     * priority for being fetched into or retained by the cache. A DPO bit set to one overrides any retention
     * priority specified in the Caching mode page. All other aspects of the algorithm implementing the cache
     * replacement strategy are not defined by this standard.
     * <p>
     * NOTE 11 - The DPO bit is used to control replacement of logical blocks in the cache when the
     * application client has information on the future usage of the logical blocks. If the DPO bit is set to
     * one, then the application client is specifying that the logical blocks accessed by the command are not
     * likely to be accessed again in the near future and should not be put in the cache nor retained by the
     * cache. If the DPO bit is set to zero, then the application client is specifying that the logical blocks
     * accessed by this command are likely to be accessed again in the near future.
     */
    private final boolean disablePageOut;

    /**
     * The FUA ({@link #forceUnitAccess}) and FUA_NV ( {@link #forceUnitAccessNonVolatileCache}) bits together
     * determine where
     * exactly the transmitted data shall be written (cache, non-volatile cache,
     * or medium) before sending the response.
     * <p>
     * <table border="1">
     * <tr>
     * <th>FUA</th>
     * <th>FUA_NV</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>0</td>
     * <td>The device server may read the logical blocks from volatile cache, non-volatile cache, and/or the
     * medium.</td>
     * </tr>
     * <tr>
     * <td>0</td>
     * <td>1</td>
     * <td>If the NV_SUP bit is set to one in the Extended INQUIRY Data VPD page (see SPC-4), then the device
     * server shall read the logical blocks from non-volatile cache or the medium. If a non-volatile cache is
     * present and a volatile cache contains a more recent version of a logical block, then the device server
     * shall write the logical block to:<br/>
     * a) non-volatile cache; and/or<br/>
     * b) the medium,<br/>
     * before reading it. If the NV_SUP bit is set to zero in the Extended INQUIRY Data VPD page (see SPC-4),
     * then the device server may write the logical blocks to volatile cache, non-volatile cache, and/or the
     * medium.</td>
     * </tr>
     * <tr>
     * <td>1</td>
     * <td>0 or 1</td>
     * <td>The device server may read the logical blocks from volatile cache, non-volatile cache, and/or the
     * medium.</td>
     * </tr>
     * </table>
     */
    private final boolean forceUnitAccess;

    /**
     * The FUA ({@link #forceUnitAccess}) and FUA_NV ( {@link #forceUnitAccessNonVolatileCache}) bits together
     * determine where
     * exactly the transmitted data shall be written (cache, non-volatile cache,
     * or medium) before sending the response.
     * 
     * @see #forceUnitAccess
     */
    private final boolean forceUnitAccessNonVolatileCache;

    /**
     * The GROUP NUMBER field can specify a particular grouping function, which
     * is a function that collects information about attributes associated with
     * commands (i.e., information about commands with the same group value are
     * collected into the specified group).
     * <p>
     * The collection of this information is outside the scope of the SCSI standard (as of SBC3R25) and also
     * not part of the iSCSI specification, so the value of this field will be ignored.
     * <p>
     * Support for the grouping function is indicated in the GROUP_SUP bit in the Extended INQUIRY Data VPD
     * page (see SPC-4).
     * <p>
     * 
     */
    private final int groupNumber;

    public Write10Cdb(ByteBuffer buffer) {
        super(buffer);// OPERATION CODE + CONTROL

        // RDPROTECT
        byte b = buffer.get(1);
        writeProtect = (b >> 5) & 7;

        // DPO
        disablePageOut = BitManip.getBit(b, 4);

        // FUA
        forceUnitAccess = BitManip.getBit(b, 3);

        // FUA_NV
        forceUnitAccessNonVolatileCache = BitManip.getBit(b, 1);

        // GROUP NUMBER
        b = buffer.get(6);
        groupNumber = b & 31;
    }

    @Override
    protected long deserializeLogicalBlockAddress(ByteBuffer buffer) {
        return ReadWrite.readUnsignedInt(buffer, 2);
    }

    @Override
    protected int deserializeTransferLength(ByteBuffer buffer) {
        return ReadWrite.readTwoByteInt(buffer, 7);
    }

    public int getWriteProtect() {
        return writeProtect;
    }

    public boolean disablePageOut() {
        return disablePageOut;
    }

    public boolean getForceUnitAccess() {
        return forceUnitAccess;
    }

    public boolean getForceUnitAccessNonVolatile() {
        return forceUnitAccessNonVolatileCache;
    }

    public int getGroupNumber() {
        return groupNumber;
    }

    @Override
    protected int getLogicalBlockAddressFieldIndex() {
        return 2;
    }

    @Override
    protected int getTransferLengthFieldIndex() {
        return 7;
    }

}

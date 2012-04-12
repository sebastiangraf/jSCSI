package org.jscsi.target.scsi.modeSense;

import java.nio.ByteBuffer;

import org.jscsi.target.util.BitManip;
import org.jscsi.target.util.ReadWrite;

/**
 * The Caching mode page defines the parameters that affect the use of the data
 * cache by the storage medium.
 * 
 * @author Andreas Ergenzinger
 */
public final class CachingModePage extends Page_0FormatModePage {

    /**
     * An initiator control (IC) enable bit set to one specifies that the device
     * server use one of the following fields to control the caching algorithm
     * rather than the device server’s own adaptive algorithm:
     * <ol>
     * <li>
     * the NUMBER OF CACHE SEGMENTS field, if the SIZE bit is set to zero; or</li>
     * <li>
     * the CACHE SEGMENT SIZE field, if the SIZE bit is set to one.</li>
     * </ol>
     */
    private final boolean initiatorControl;

    /**
     * An abort pre-fetch (ABPF) bit set to one when the DRA bit is set to zero
     * specifies that the device server abort a pre-fetch upon receipt of a new
     * command. An ABPF bit set to one takes precedence over the value specified
     * in the MINIMUM PRE-FETCH field. An ABPF bit set to zero when the DRA bit
     * set to zero specifies that the termination of any active pre-fetch is
     * dependent upon Caching mode page bytes 4 through 11 and is vendor
     * specific.
     */
    private final boolean abortPrefetch;

    /**
     * A caching analysis permitted (CAP) bit set to one specifies that the
     * device server perform caching analysis during subsequent operations. A
     * CAP bit set to zero specifies that caching analysis be disabled (e.g., to
     * reduce overhead time or to prevent nonpertinent operations from impacting
     * tuning values).
     */
    private final boolean cachingAnalysisPermitted;

    /**
     * A discontinuity (DISC) bit set to one specifies that the device server
     * continue the pre-fetch across time discontinuities (e.g., across
     * cylinders) up to the limits of the buffer, or segment, space available
     * for the pre-fetch. A DISC bit set to zero specifies that pre-fetches be
     * truncated or wrapped at time discontinuities.
     */
    private final boolean discontinuity;

    /**
     * A size enable (SIZE) bit set to one specifies that the CACHE SEGMENT SIZE
     * field be used to control caching segmentation. A SIZE bit set to zero
     * specifies that the NUMBER OF CACHE SEGMENTS field be used to control
     * caching segmentation. Simultaneous use of both the number of segments and
     * the segment size is vendor specific.
     */
    private final boolean sizeEnable;

    /**
     * A writeback cache enable (WCE) bit set to zero specifies that the device
     * server shall complete a WRITE command with GOOD status only after writing
     * all of the data to the medium without error. A WCE bit set to one
     * specifies that the device server may complete a WRITE command with GOOD
     * status after receiving the data without error and prior to having written
     * the data to the medium.
     */
    private final boolean writebackCacheEnable;

    /**
     * A multiplication factor (MF) bit set to zero specifies that the device
     * server shall interpret the MINIMUM PRE-FETCH field and the MAXIMUM
     * PRE-FETCH field in terms of the number of logical blocks for each of the
     * respective types of pre-fetch. An MF bit set to one specifies that the
     * device server shall interpret the MINIMUM PRE-FETCH field and the MAXIMUM
     * PRE-FETCH field to be specified in terms of a scalar number that, when
     * multiplied by the number of logical blocks to be transferred for the
     * current command, yields the number of logical blocks for each of the
     * respective types of pre-fetch.
     */
    private final boolean multiplicationFactor;

    /**
     * A read cache disable (RCD) bit set to zero specifies that the device
     * server may return data requested by a READ command by accessing either
     * the cache or medium. A RCD bit set to one specifies that the device
     * server shall transfer all of the data requested by a READ command from
     * the medium (i.e., data shall not be transferred from the cache).
     */
    private final boolean readCacheDisable;

    /**
     * The DEMAND READ RETENTION PRIORITY field specifies the retention priority
     * the device server should assign for data read into the cache that has
     * also been transferred to the data-in buffer.
     * <table border="1">
     * <tr>
     * <th>Code</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0x0</td>
     * <td>The device server should not distinguish between retaining the<br/>
     * indicated data and data placed into the cache by other means <br/>
     * (e.g., pre-fetch).</td>
     * </tr>
     * <tr>
     * <td>0x1</td>
     * <td>The device server should replace data put into the cache via a<br/>
     * READ command sooner (i.e., read data has lower priority) than data<br/>
     * placed into the cache by other means (e.g., pre-fetch).</td>
     * </tr>
     * <tr>
     * <td>0x2 to 0xE</td>
     * <td>Reserved</td>
     * </tr>
     * </tr>
     * <tr>
     * <td>0xF</td>
     * <td>The device server should not replace data put into the cache<br/>
     * via a READ command if there is other data in the cache that was<br/>
     * placed into the cache by other means (e.g., pre-fetch) and the<br/>
     * data in the cache may be replaced.</td>
     * </tr>
     * </table>
     */
    private final byte demandReadRetentionPriority;

    /**
     * The WRITE RETENTION PRIORITY field (see table 155) specifies the
     * retention priority the device server should assign for data written into
     * the cache that has also been transferred from the cache to the medium.
     * <table border="1">
     * <tr>
     * <th>Code</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>0x0</td>
     * <td>The device server should not distinguish between retaining the<br/>
     * indicated data and data placed into the cache by other means<br/>
     * (e.g., pre-fetch).</td>
     * </tr>
     * <tr>
     * <td>0x1</td>
     * <td>The device server should replace data put into the cache during a<br/>
     * WRITE command or a WRITE AND VERIFY command sooner (i.e., has lower<br/>
     * priority) than data placed into the cache by other means (e.g., pre-fetch).</td>
     * </tr>
     * <tr>
     * <td>0x2 to 0xE</td>
     * <td>Reserved</td>
     * </tr>
     * </tr>
     * <tr>
     * <td>0xF</td>
     * <td>The device server should not replace data put into the cache during<br/>
     * a WRITE command or a WRITE AND VERIFY command if there is other data in<br/>
     * the cache that was placed into the cache by other means (e.g., pre-fetch) <br/>
     * and the data in the cache may be replaced.</td>
     * </tr>
     * </table>
     */
    private final byte writeRetentionPriority;

    /**
     * The DISABLE PRE-FETCH TRANSFER LENGTH field specifies the selective
     * disabling of anticipatory pre-fetch on long transfer lengths. The value
     * in this field is compared to the transfer length requested by a READ
     * command. If the transfer length is greater than the disable pre-fetch
     * transfer length, then an anticipatory pre-fetch is not done for the
     * command. Otherwise the device server should attempt an anticipatory
     * pre-fetch. If the DISABLE PRE-FETCH TRANSFER LENGTH field is set to zero,
     * then all anticipatory pre-fetching is disabled for any request for data,
     * including those with a transfer length of zero.
     * <p>
     * An anticipatory pre-fetch occurs when data is placed in the cache that has not been requested. This may
     * happen in conjunction with the reading of data that has been requested. The DISABLE PRE-FETCH TRANSFER
     * LENGTH field, the MINIMUM PRE-FETCH field, the MAXIMUM PRE-FETCH field, and the MAXIMUM PRE-FETCH
     * CEILING field give an indication to the device server how it should manage the cache based on the most
     * recent READ command. An anticipatory pre-fetch may occur based on other information. These fields are
     * only recommendations to the device server and should not cause a CHECK CONDITION to occur if the device
     * server is not able to satisfy the request.
     */
    private final int disablePrefetchTransferLength;

    /**
     * The MINIMUM PRE-FETCH field specifies the number of logical blocks to
     * pre-fetch regardless of the delays it might cause in processing
     * subsequent commands. The field contains either:
     * <ol>
     * <li>a number of logical blocks, if the MF bit is set to zero; or</li>
     * <li>a scalar multiplier of the value in the TRANSFER LENGTH field, if the MF bit is set to one.</li>
     * </ol>
     * The pre-fetching operation begins at the logical block after the last
     * logical block of a READ command.
     * <p>
     * Pre-fetching shall always halt when it reaches the last logical block on the medium. Errors that occur
     * during the pre-fetching operation shall not be reported to the application client unless the device
     * server is unable to process subsequent commands correctly as a result of the error. In this case the
     * error may be reported either as an error for that subsequent command, or as a deferred error, at the
     * discretion of the device server and according to the rules for reporting deferred errors (see SPC-4).
     * <p>
     * If the pre-fetch has read more than the amount of data specified by the MINIMUM PRE-FETCH field, then
     * pre-fetching should be terminated whenever another command enters the enabled state (see SAM-4). This
     * requirement is ignored when the MINIMUM PRE-FETCH field value is equal to the MAXIMUM PRE-FETCH field
     * value.
     * 
     * @see #disablePrefetchTransferLength
     */
    private final int minimumPrefetch;

    /**
     * A complementary field to the {@link #minimumPrefetch} field.
     * 
     * @see #disablePrefetchTransferLength
     * @see #minimumPrefetch
     */
    private final int maximumPrefetch;

    /**
     * The MAXIMUM PRE-FETCH CEILING field specifies an upper limit on the
     * number of logical blocks computed as the maximum pre-fetch. If this
     * number of logical blocks is greater than the value in the MAXIMUM
     * PRE-FETCH field, then the number of logical blocks to pre-fetch shall be
     * truncated to the value stored in the MAXIMUM PRE-FETCH CEILING field.
     * 
     * @see #disablePrefetchTransferLength
     */
    private final int maximumPrefetchCeiling;

    /**
     * A force sequential write (FSW) bit set to one specifies that, for
     * commands writing to more than one logical block, the device server shall
     * write the logical blocks to the medium in ascending sequential order. An
     * FSW bit set to zero specifies that the device server may reorder the
     * sequence of writing logical blocks (e.g., in order to achieve faster
     * command completion).
     */
    private final boolean forceSequentialWrite;

    /**
     * A logical block cache segment size (LBCSS) bit set to one specifies that
     * the CACHE SEGMENT SIZE field units shall be interpreted as logical
     * blocks. An LBCSS bit set to zero specifies that the CACHE SEGMENT SIZE
     * field units shall be interpreted as bytes. The LBCSS shall not impact the
     * units of other fields.
     */
    private final boolean logicalBlockCacheSegmentSize;

    /**
     * A disable read-ahead (DRA) bit set to one specifies that the device
     * server shall not read into the pre-fetch buffer any logical blocks beyond
     * the addressed logical block(s). A DRA bit set to zero specifies that the
     * device server may continue to read logical blocks into the pre-fetch
     * buffer beyond the addressed logical block(s).
     */
    private final boolean disableReadAhead;

    /**
     * An NV_DIS bit set to one specifies that the device server shall disable a
     * non-volatile cache and indicates that a non-volatile cache is supported
     * but disabled. An NV_DIS bit set to zero specifies that the device server
     * may use a non-volatile cache and indicates that a non-volatile cache may
     * be present and enabled.
     */
    private final boolean nonVolatileCacheDisabled;

    /**
     * The NUMBER OF CACHE SEGMENTS field specifies the number of segments into
     * which the device server shall divide the cache.
     */
    private final short numberOfCacheSegments;

    /**
     * The CACHE SEGMENT SIZE field specifies the segment size in bytes if the
     * LBCSS bit is set to zero or in logical blocks if the LBCSS bit is set to
     * one. The CACHE SEGMENT SIZE field is valid only when the SIZE bit is set
     * to one.
     */
    private final int cacheSegmentSize;

    /**
     * The constructor.
     * <p>
     * The meaning of all parameters is described in the member descriptions of the variables with the same
     * name.
     * 
     * @param parametersSaveable
     * @param initiatorControl
     * @param abortPrefetch
     * @param cachingAnalysisPermitted
     * @param discontinuity
     * @param sizeEnable
     * @param writebackCacheEnable
     * @param multiplicationFactor
     * @param readCacheDisable
     * @param demandReadRetentionPriority
     * @param writeRetentionPriority
     * @param disablePrefetchTransferLength
     * @param minimumPrefetch
     * @param maximumPrefetch
     * @param maximumPrefetchCeiling
     * @param forceSequentialWrite
     * @param logicalBlockCacheSegmentSize
     * @param disableReadAhead
     * @param nonVolatileCacheDisabled
     * @param numberOfCacheSegments
     * @param cacheSegmentSize
     */
    public CachingModePage(final boolean parametersSaveable, final boolean initiatorControl,
        final boolean abortPrefetch, final boolean cachingAnalysisPermitted, final boolean discontinuity,
        final boolean sizeEnable, final boolean writebackCacheEnable, final boolean multiplicationFactor,
        final boolean readCacheDisable, final int demandReadRetentionPriority,
        final int writeRetentionPriority, final int disablePrefetchTransferLength, final int minimumPrefetch,
        final int maximumPrefetch, final int maximumPrefetchCeiling, final boolean forceSequentialWrite,
        final boolean logicalBlockCacheSegmentSize, final boolean disableReadAhead,
        final boolean nonVolatileCacheDisabled, final int numberOfCacheSegments, final int cacheSegmentSize) {
        super(parametersSaveable,// PS
            0x08,// pageCode
            0x12);// pageLength
        this.initiatorControl = initiatorControl;
        this.abortPrefetch = abortPrefetch;
        this.cachingAnalysisPermitted = cachingAnalysisPermitted;
        this.discontinuity = discontinuity;
        this.sizeEnable = sizeEnable;
        this.writebackCacheEnable = writebackCacheEnable;
        this.multiplicationFactor = multiplicationFactor;
        this.readCacheDisable = readCacheDisable;
        this.demandReadRetentionPriority = (byte)(demandReadRetentionPriority & 0xf);
        this.writeRetentionPriority = (byte)(writeRetentionPriority & 0xf);
        this.disablePrefetchTransferLength = disablePrefetchTransferLength & 0xffff;
        this.minimumPrefetch = minimumPrefetch & 0xffff;
        this.maximumPrefetch = maximumPrefetch & 0xffff;
        this.maximumPrefetchCeiling = maximumPrefetchCeiling & 0xffff;
        this.forceSequentialWrite = forceSequentialWrite;
        this.logicalBlockCacheSegmentSize = logicalBlockCacheSegmentSize;
        this.disableReadAhead = disableReadAhead;
        this.nonVolatileCacheDisabled = nonVolatileCacheDisabled;
        this.numberOfCacheSegments = (short)(numberOfCacheSegments & 0xff);
        this.cacheSegmentSize = cacheSegmentSize & 0xffff;
    }

    @Override
    protected void serializeModeParameters(ByteBuffer buffer, int index) {
        // serialize byte 2
        buffer.position(index + 2);
        byte b = 0;
        b = BitManip.getByteWithBitSet(b, 7, initiatorControl);
        b = BitManip.getByteWithBitSet(b, 6, abortPrefetch);
        b = BitManip.getByteWithBitSet(b, 5, cachingAnalysisPermitted);
        b = BitManip.getByteWithBitSet(b, 4, discontinuity);
        b = BitManip.getByteWithBitSet(b, 3, sizeEnable);
        b = BitManip.getByteWithBitSet(b, 2, writebackCacheEnable);
        b = BitManip.getByteWithBitSet(b, 1, multiplicationFactor);
        b = BitManip.getByteWithBitSet(b, 0, readCacheDisable);
        buffer.put(b);
        // serialize byte 3
        b = (byte)((demandReadRetentionPriority << 4) | writeRetentionPriority);
        buffer.put(b);
        // bytes 4 to 11 (unsigned short fields)
        ReadWrite.writeTwoByteInt(buffer,// buffer
            disablePrefetchTransferLength,// value
            index + 4);// index
        ReadWrite.writeTwoByteInt(buffer,// buffer
            minimumPrefetch,// value
            index + 6);// index
        ReadWrite.writeTwoByteInt(buffer,// buffer
            maximumPrefetch,// value
            index + 8);// index
        ReadWrite.writeTwoByteInt(buffer,// buffer
            maximumPrefetchCeiling,// value
            index + 10);// index
        // byte 12
        b = 0;
        b = BitManip.getByteWithBitSet(b, 7, forceSequentialWrite);
        b = BitManip.getByteWithBitSet(b, 6, logicalBlockCacheSegmentSize);
        b = BitManip.getByteWithBitSet(b, 5, disableReadAhead);
        b = BitManip.getByteWithBitSet(b, 0, nonVolatileCacheDisabled);
        buffer.position(index + 12);
        buffer.put(b);
        // byte 13
        buffer.put((byte)numberOfCacheSegments);
        // bytes 14 and 15
        ReadWrite.writeTwoByteInt(buffer,// buffer
            cacheSegmentSize,// value
            index + 14);// index
        // the remaining bytes are reserved or obsolete
    }

}

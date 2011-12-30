/**
 * Copyright (c) 2011, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the University of Konstanz nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.jscsi.parser.datasegment;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;

import org.jscsi.parser.Constants;
import org.jscsi.parser.exception.InternetSCSIException;

/**
 * <h1>DataSegment</h1>
 * <p>
 * This class represents a basic interface for a data segment contained in a
 * <code>ProtocolDataUnit</code> object.
 * 
 * @author Volker Wildi
 */
public abstract class AbstractDataSegment implements IDataSegment {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constant, which is used to partioning the <code>dataBuffer</code> of this
     * size. Typically, this is equal to the MaxRecvDataSegmentLength.
     */
    private final int maxChunkSize;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * The buffer of this data segment.
     */
    protected ByteBuffer dataBuffer;

    /** The number of bytes actually used by the <code>dataBuffer</code>. */
    protected int length;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new, empty <code>DataSegment</code> object with
     * the given chunk size.
     * 
     * @param initMaxChunkSize
     *            The maximum size (in bytes) of one chunk, which represents the
     *            <code>MaxRecvDataSegmentLength</code>.
     */
    public AbstractDataSegment(final int initMaxChunkSize) {

        maxChunkSize = initMaxChunkSize;
        length = 0;

        dataBuffer = ByteBuffer.allocate(length);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public int serialize(final ByteBuffer dst, final int off) {

        if (dst == null) {
            throw new NullPointerException();
        }

        dst.position(off);

        if (dst.remaining() % Constants.BYTES_PER_INT != 0) {
            throw new IllegalArgumentException(
                    "The buffer length must be a multiple of "
                            + Constants.BYTES_PER_INT + ".");
        }

        dataBuffer.rewind();

        if (dst.remaining() < dataBuffer.limit()) {
            throw new IllegalArgumentException("The data buffer is too small.");
        }

        dst.put(dataBuffer);

        return dataBuffer.limit();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public SettingsMap getSettings() throws InternetSCSIException {

        throw new UnsupportedOperationException();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns the total length (with padding), which is needed for the given
     * length <code>len</code>.
     * 
     * @param len
     *            The length of the <code>DataSegment</code>, which want to be
     *            stored.
     * @return The total length.
     */
    public static final int getTotalLength(final int len) {

        return len + calcPadding(len);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This method resizes the data buffer, if necessary. A resizing is not
     * needed, when the <code>neededLength</code> is greater than the capacity
     * of allocated data buffer. The flag <code>copyData</code> indicates, if
     * the old buffer has to be copied into the new bigger data buffer.
     * 
     * @param additionalLength
     *            The length, which is now needed to store all informations in
     *            the data buffer.
     * @param copyData
     *            <code>true</code>, if old buffer has to be copied into the new
     *            buffer. <code>false</code> indicates, that the new buffer is
     *            initialized with zeros.
     */
    protected void resizeBuffer(final int additionalLength,
            final boolean copyData) {

        if (additionalLength < 0) {
            throw new IllegalArgumentException(
                    "The length must be greater or equal than 0.");
        }

        dataBuffer.position(length);

        // reallocate a bigger dataBuffer, if needed
        if (length + additionalLength > dataBuffer.capacity()) {

            final ByteBuffer newBuffer = ByteBuffer
                    .allocate(getTotalLength(length + additionalLength));

            // copy old data...
            if (copyData) {
                dataBuffer.flip();
                newBuffer.put(dataBuffer);
            }

            dataBuffer = newBuffer;

            dataBuffer.limit(getTotalLength(length + additionalLength));
        }

        length += additionalLength;
    }

    /** {@inheritDoc} */
    public void clear() {

        dataBuffer.clear().flip();
        length = 0;
    }

    /** {@inheritDoc} */
    public final int getLength() {

        return length;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Compares this object with another object for equality.
     * 
     * @param anObject
     *            Another object.
     * @return <code>true</code>, if the <code>anObject</code> and this object
     *         are equal. Else <code>false</code> .
     */
    public boolean equals(final Object anObject) {

        // check for alias
        if (this == anObject) {
            return true;
        }

        if (anObject instanceof AbstractDataSegment) {
            final AbstractDataSegment anotherDS = (AbstractDataSegment) anObject;

            if (length != anotherDS.length) {
                return false;
            }

            dataBuffer.rewind();
            anotherDS.dataBuffer.rewind();

            return dataBuffer.equals(anotherDS.dataBuffer);
        }

        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {

        return super.hashCode();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final int setDataBuffer(final ByteBuffer src, final int off,
            final int len) {

        resizeBuffer(len, false);
        src.position(off);

        int n = 0;
        while (src.hasRemaining() && n++ < length) {
            dataBuffer.put(src.get());
        }

        dataBuffer.rewind();

        return n;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final String toString() {

        final StringBuilder sb = new StringBuilder();
        sb.append("Length: ");
        sb.append(length);
        sb.append(", MaxChunkSize: ");
        sb.append(maxChunkSize);
        sb.append(", dataBuffer: ");
        sb.append(dataBuffer);

        return sb.toString();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final DataSegmentIterator iterator() {

        return new DataSegmentIterator();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * This methods calculates the number of padding bytes of the given length.
     * This number of the padding bytes is in the range of
     * <code>0, ..., BYTES_PER_INT</code>.
     * 
     * @param len
     *            The length for which the padding bytes are calculated.
     * @return The number of padding bytes.
     */
    protected static final int calcPadding(final int len) {

        if (len < 0) {
            throw new IllegalArgumentException(
                    "Length must be a positive number");
        }

        final int rest = len % Constants.BYTES_PER_INT;
        if (rest > 0) {
            return Constants.BYTES_PER_INT - rest;
        } else {
            return 0;
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Iterator through the chunks of a <code>DataSegment</code>.
     * <p/>
     * <b>This class is not resistant against changes to the
     * <code>dataBuffer</code> .</b>
     */
    private final class DataSegmentIterator implements IDataSegmentIterator {

        /** The actual position of the iterator. */
        private int cursor;

        /** The data of a chunk. */
        private final ByteBuffer data;

        /**
         * Constructor to create an iterator through the chunks.
         */
        private DataSegmentIterator() {

            cursor = dataBuffer.position();
            data = ByteBuffer.allocate(maxChunkSize);
        }

        /** {@inheritDoc} */
        public final boolean hasNext() {

            return cursor < dataBuffer.limit();
        }

        /** {@inheritDoc} */
        public final IDataSegmentChunk next(final int chunkSize) {

            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (chunkSize < 0 && chunkSize > maxChunkSize) {
                throw new IllegalArgumentException();
            }

            final int pos = dataBuffer.position();
            dataBuffer.position(cursor);

            data.position(0).limit(chunkSize);

            while (data.hasRemaining() && dataBuffer.hasRemaining()) {
                data.put(dataBuffer.get());
            }

            final int dataSegmentLength = data.position();
            final int totalLength = dataSegmentLength
                    + calcPadding(dataSegmentLength);

            // set limit to the next nearest bound (with padding)
            data.limit(totalLength);
            data.rewind();
            dataBuffer.position(pos);

            // cursor += chunkSize;
            cursor += totalLength;

            return new DataSegmentChunk(dataSegmentLength, totalLength);
        }

        // ----------------------------------------------------------------------
        // ----
        // ----------------------------------------------------------------------
        // ----
        // ----------------------------------------------------------------------
        // ----
        // ----------------------------------------------------------------------
        // ----

        /**
         * <h1>DataSegmentChunk</h1>
         * <p/>
         * This is a chunk of a data segment.
         * 
         * @author Volker Wildi
         */
        private final class DataSegmentChunk implements IDataSegmentChunk {

            /** The real length (in bytes) of this chunk (excluding padding). */
            private final int usedLength;

            /**
             * The length (in bytes), which is needed by this chunk (including
             * padding).
             */
            private final int totalLength;

            /**
             * Constructor to create a new, empty <code>DataSegmentChunk</code>
             * instance with a given length and a total length of this data
             * segment chunk.
             * 
             * @param initLength
             *            The used length (in bytes) of this new
             *            <code>DataSegmentChunk</code> instance.
             * @param initTotalLength
             *            The total length (in bytes) of this new
             *            <code>DataSegmentChunk</code> instance.
             */
            private DataSegmentChunk(final int initLength,
                    final int initTotalLength) {

                usedLength = initLength;
                totalLength = initTotalLength;
            }

            /** {@inheritDoc} */
            public ByteBuffer getData() {

                return data;
            }

            /** {@inheritDoc} */
            public int getLength() {

                return usedLength;
            }

            /** {@inheritDoc} */
            public int getTotalLength() {

                return totalLength;
            }

        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the University of Konstanz nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
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

/**
 * <h1>DataSegmentFactory</h1>
 * <p>
 * This factory creates a instance to a <code>DataSegment</code> object.
 * 
 * @author Volker Wildi
 */
public final class DataSegmentFactory {

    /**
     * This enumeration defines the valid stati of the data segment.
     */
    public enum DataSegmentFormat {
        /** The data segment has to be interpreted as text (login parameters). */
        TEXT,
        /** The data segment has to be interpreted as binary data. */
        BINARY,
        /**
         * The data segment has to be interpreted as a SCSI response data
         * segment.
         */
        SCSI_RESPONSE,
        /** There is no data segment allowed. */
        NONE;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Make this contructor hidden to any class.
     */
    private DataSegmentFactory() {

    }

    /**
     * Creates a data segment of the given format and with the given chunk size
     * (in bytes). This data segment is initialized with the data of <code>buffer</code>.
     * 
     * @param buffer
     *            The initialization buffer.
     * @param format
     *            A format from the <code>DataSegmentFormat</code> enumeration.
     * @param maxChunkSize
     *            The size (in bytes) of one chunk, which represents the <code>MaxRecvDataSegmentLength</code>
     *            .
     * @return The instance of an <codE>DataSegment</code> object.
     */
    public static final IDataSegment create(final ByteBuffer buffer, final DataSegmentFormat format,
        final int maxChunkSize) {

        return create(buffer, buffer.position(), buffer.remaining(), format, maxChunkSize);
    }

    /**
     * Creates a data segment of the given format and with the given chunk size
     * (in bytes). This data segment is initialized with the data of <code>buffer</code>.
     * 
     * @param buffer
     *            The initialization buffer.
     * @param position
     *            The position of the variable <code>buffer</code>.
     * @param length
     *            The length (in bytes) to read from <code>buffer</code>.
     * @param format
     *            A format from the <code>DataSegmentFormat</code> enumeration.
     * @param maxChunkSize
     *            The size (in bytes) of one chunk, which represents the <code>MaxRecvDataSegmentLength</code>
     *            .
     * @return The instance of an <codE>DataSegment</code> object.
     */
    public static final IDataSegment create(final ByteBuffer buffer, final int position, final int length,
        final DataSegmentFormat format, final int maxChunkSize) {

        final IDataSegment dataSegment = DataSegmentFactory.create(format, maxChunkSize);
        dataSegment.setDataBuffer(buffer, position, length);

        return dataSegment;
    }

    /**
     * Creates a data segment of the given format and with the given chunk size
     * (in bytes).
     * 
     * @param format
     *            A format from the <code>DataSegmentFormat</code> enumeration.
     * @param maxChunkSize
     *            The size (in bytes) of one chunk, which represents the <code>MaxRecvDataSegmentLength</code>
     *            .
     * @return The instance of an <codE>DataSegment</code> object.
     */
    public static final IDataSegment create(final DataSegmentFormat format, final int maxChunkSize) {

        final IDataSegment dataSegment;
        switch (format) {
        case TEXT:
            dataSegment = new TextParameterDataSegment(maxChunkSize);
            break;

        case BINARY:
            dataSegment = new BinaryDataSegment(maxChunkSize);
            break;

        case NONE:
            dataSegment = new NullDataSegment(maxChunkSize);
            break;

        default:
            throw new IllegalArgumentException("Unknown data segment format.");
        }

        return dataSegment;
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

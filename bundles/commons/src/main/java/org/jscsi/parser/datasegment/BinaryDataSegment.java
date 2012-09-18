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
 * <h1>BinaryDataSegment</h1>
 * <p>
 * This class represents a binary data segment, which is attached by several <code>ProtocolDataUnit</code>
 * objects.
 * 
 * @author Volker Wildi
 */
final class BinaryDataSegment extends AbstractDataSegment {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Constructor to create a new, empty <code>BinaryDataSegment</code> object
     * with the given chunk size.
     * 
     * @param chunkSize
     *            The maximum number of bytes of a chunk.
     */
    public BinaryDataSegment(final int chunkSize) {

        super(chunkSize);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /** {@inheritDoc} */
    public final int deserialize(final ByteBuffer src, final int len) {

        resizeBuffer(src.remaining(), false);
        dataBuffer.rewind();

        transferBytes(src, dataBuffer, len);

        return dataBuffer.limit();
    }

    /** {@inheritDoc} */
    public final int append(final ByteBuffer src, final int len) {

        if (src == null) {
            throw new NullPointerException();
        }

        dataBuffer.position(length);
        resizeBuffer(length + len, true);

        transferBytes(src, dataBuffer, len);

        return dataBuffer.limit();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    private final void transferBytes(final ByteBuffer src, final ByteBuffer dst, final int len) {

        if (dst.remaining() < len) {
            throw new IllegalArgumentException(
                "The given length must be less or equal than the remaining bytes in the destination buffer.");
        }
        for (int i = 0; i < len; i++) {
            if (src.hasRemaining() && dst.hasRemaining()) {
                dst.put(src.get());
            } else {
                throw new RuntimeException("Error by transferring the bytes in this data segment.");
            }
        }
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

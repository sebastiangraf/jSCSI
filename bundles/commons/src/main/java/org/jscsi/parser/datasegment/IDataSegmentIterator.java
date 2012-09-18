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
 * <h1>IDataSegmentIterator</h1>
 * <p/>
 * Interface for iterator of a data segment.
 * 
 * @author Volker Wildi
 */
public interface IDataSegmentIterator {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Returns true if the iteration has more elements. (In other words, returns
     * true if next would return an element rather than throwing an exception.)
     * 
     * @return <code>true</code> if the iterator has more elements.
     */
    public boolean hasNext();

    /**
     * Returns the next element with the given size (in bytes) in the iteration.
     * Calling this method repeatedly until the hasNext() method returns false
     * will return each element in the underlying collection exactly once.
     * 
     * @param chunkSize
     *            The requested size of the next data segment chunk.
     * @return the next chunk in the iteration.
     */
    public IDataSegmentChunk next(final int chunkSize);

    /**
     * <h1>IDataSegmentChunk</h1>
     * <p/>
     * This class represents a chunk of a data segment, which is sent with a Protocol Data Unit.
     * 
     * @author Volker Wildi
     */
    public interface IDataSegmentChunk {

        /**
         * Returns the stored data buffer of this <code>IDataSegmentChunk</code> instance.
         * 
         * @return The store data buffer.
         */
        public ByteBuffer getData();

        /**
         * Returns the total length, which includes the padding.
         * 
         * @return The total length (in bytes) of this <code>IDataSegmentChunk</code> instance.
         */
        public int getTotalLength();

        /**
         * The length of the data excluding padding.
         * 
         * @return The length (in bytes) of the contained data.
         */
        public int getLength();
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

}

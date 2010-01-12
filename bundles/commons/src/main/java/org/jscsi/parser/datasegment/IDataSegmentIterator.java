/*
 * Copyright 2007 Marc Kramis Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. $Id:
 * IDataSegmentIterator.java 2498 2007-03-05 12:32:43Z lemke $
 */

package org.jscsi.parser.datasegment;

import java.nio.ByteBuffer;

/**
 * <h1>IDataSegmentIterator</h1> <p/> Interface for iterator of a data segment.
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
   *          The requested size of the next data segment chunk.
   * @return the next chunk in the iteration.
   */
  public IDataSegmentChunk next(final int chunkSize);

  /**
   * <h1>IDataSegmentChunk</h1> <p/> This class represents a chunk of a data
   * segment, which is sent with a Protocol Data Unit.
   * 
   * @author Volker Wildi
   */
  public interface IDataSegmentChunk {

    /**
     * Returns the stored data buffer of this <code>IDataSegmentChunk</code>
     * instance.
     * 
     * @return The store data buffer.
     */
    public ByteBuffer getData();

    /**
     * Returns the total length, which includes the padding.
     * 
     * @return The total length (in bytes) of this
     *         <code>IDataSegmentChunk</code> instance.
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

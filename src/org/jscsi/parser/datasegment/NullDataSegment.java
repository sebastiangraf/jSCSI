/*
 * Copyright 2007 Marc Kramis
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * $Id: NullDataSegment.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.parser.datasegment;

import java.nio.ByteBuffer;

/**
 * <h1>NullDataSegment</h1>
 * <p>
 * This class represents an empty data segment. So, this class does nothing!
 * 
 * @author Volker Wildi
 */
final class NullDataSegment extends AbstractDataSegment {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>NullDataSegment</code> object.
   * 
   * @param initChunkSize
   *          The size (in bytes) of one chunk, which represents the
   *          <code>MaxRecvDataSegmentLength</code>.
   */
  public NullDataSegment(final int initChunkSize) {

    super(initChunkSize);
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  public int deserialize(final ByteBuffer src, final int len) {

    return 0;
  }

  /** {@inheritDoc} */
  public int append(final ByteBuffer src, final int len) {

    return 0;
  }

  /** {@inheritDoc} */
  public int serialize(final ByteBuffer dst, final int off) {

    return 0;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

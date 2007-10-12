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
 * $Id: BinaryDataSegment.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.parser.datasegment;

import java.nio.ByteBuffer;

/**
 * <h1>BinaryDataSegment</h1>
 * <p>
 * This class represents a binary data segment, which is attached by several
 * <code>ProtocolDataUnit</code> objects.
 * 
 * @author Volker Wildi
 * 
 */
final class BinaryDataSegment extends AbstractDataSegment {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Constructor to create a new, empty <code>BinaryDataSegment</code> object
   * with the given chunk size.
   * 
   * @param chunkSize
   *          The maximum number of bytes of a chunk.
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

  private final void transferBytes(final ByteBuffer src, final ByteBuffer dst,
      final int len) {

    if (dst.remaining() < len) {
      throw new IllegalArgumentException(
          "The given length must be less or equal than the remaining bytes in the destination buffer.");
    }
    for (int i = 0; i < len; i++) {
      if (src.hasRemaining() && dst.hasRemaining()) {
        dst.put(src.get());
      } else {
        throw new RuntimeException(
            "Error by transferring the bytes in this data segment.");
      }
    }
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

}

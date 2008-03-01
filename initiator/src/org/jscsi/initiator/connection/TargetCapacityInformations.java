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
 * $Id: TargetCapacityInformations.java 2498 2007-03-05 12:32:43Z lemke $
 * 
 */

package org.jscsi.initiator.connection;

import java.nio.ByteBuffer;

/**
 * <h1>TargetCapacityInformations</h1>
 * <p>
 * This class encapsulates the informations about the capacity of an iSCSI
 * Target.
 * 
 * @author Volker Wildi
 */
final class TargetCapacityInformations {

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** Multiplicator from bytes to mega bytes. */
  private static final int MEGA_BYTES = 1024 * 1024;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** The number of blocks. */
  private long size;

  /** The block size (in bytes). */
  private long blockSize;

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Default constructor to create a new, empty
   * <code>TargetCapacityInformations</code> object.
   * 
   */
  public TargetCapacityInformations() {

  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * Returns the number of blocks of the connected target.
   * 
   * @return Number of blocks.
   */
  public final long getSize() {

    return size;
  }

  /**
   * Returns the block size (in bytes).
   * 
   * @return The size of one block (in bytes).
   */
  public final long getBlockSize() {

    return blockSize;
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /**
   * This method deserializes from <code>buf</code> the capacity informations
   * of the iSCSI Target.
   * 
   * @param buf
   *          The input buffer to read from.
   */
  public final void deserialize(final ByteBuffer buf) {

    size = buf.getInt();
    blockSize = buf.getInt();
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------

  /** {@inheritDoc} */
  @Override
  public final String toString() {

    return "Block Size: " + blockSize + "B, Size: " + size
        + " blocks, Total Capacity: " + (size * blockSize) / MEGA_BYTES + " MB";
  }

  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
  // --------------------------------------------------------------------------
}

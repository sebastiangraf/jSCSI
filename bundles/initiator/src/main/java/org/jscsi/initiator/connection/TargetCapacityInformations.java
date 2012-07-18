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
package org.jscsi.initiator.connection;

import java.nio.ByteBuffer;

/**
 * <h1>TargetCapacityInformations</h1>
 * <p>
 * This class encapsulates the informations about the capacity of an iSCSI Target.
 * 
 * @author Volker Wildi
 */
public final class TargetCapacityInformations {

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
     * Default constructor to create a new, empty <code>TargetCapacityInformations</code> object.
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
     *            The input buffer to read from.
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

        return "Block Size: " + blockSize + "B, Size: " + size + " blocks, Total Capacity: "
            + (size * blockSize) / MEGA_BYTES + " MB";
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
}

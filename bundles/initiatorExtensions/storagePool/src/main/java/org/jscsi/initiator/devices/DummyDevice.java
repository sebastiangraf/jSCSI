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
package org.jscsi.initiator.devices;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>DummyDevice</h1>
 * <p>
 * Implements a Device that simulates reads and writes.
 * </p>
 * 
 * @author Bastian Lemke
 */
public class DummyDevice implements Device {

    private Date creationDate;

    private final int blockSize;

    private final long blockCount;

    private boolean opened = false;

    /** The Logger interface. */
    private static final Logger LOGGER = LoggerFactory.getLogger(DummyDevice.class);

    /**
     * Constructor to create an DummyDevice. The Device has to be initialized
     * before it can be used.
     * 
     * @param initBlockSize
     *            block size of the dummy device
     * @param initBlockCount
     *            number of blocks of the dummy device
     * @throws Exception
     *             if any error occurs
     */
    public DummyDevice(final int initBlockSize, final long initBlockCount) throws Exception {

        blockSize = initBlockSize;
        blockCount = initBlockCount;
    }

    /** {@inheritDoc} */
    public void close() throws Exception {

        LOGGER.info("Closed " + getName() + ".");
        opened = false;
    }

    /** {@inheritDoc} */
    public int getBlockSize() {

        if (!opened) {
            throw new IllegalStateException("You first have to open the Device!");
        }

        return blockSize;
    }

    /** {@inheritDoc} */
    public String getName() {

        if (!opened) {
            throw new IllegalStateException("You first have to open the Device!");
        }

        return "DummyDevice(" + creationDate.getTime() + ")";
    }

    /** {@inheritDoc} */
    public long getBlockCount() {

        if (!opened) {
            throw new IllegalStateException("You first have to open the Device!");
        }

        return blockCount;
    }

    /** {@inheritDoc} */
    public void open() throws Exception {

        if (opened) {
            throw new IllegalStateException("DummyDevice is already opened!");
        }

        opened = true;
        creationDate = new Date();

        LOGGER.info("Initialized " + getName() + ".");
    }

    /** {@inheritDoc} */
    public void read(final long address, final byte[] data) throws Exception {

        if (!opened) {
            throw new IllegalStateException("You first have to open the Device!");
        }

        long blocks = data.length / blockSize;

        if (address < 0 || address + blocks > blockCount) {
            long adr;
            if (address < 0) {
                adr = address;
            } else {
                adr = address + blocks - 1;
            }
            throw new IllegalArgumentException("Address " + adr + " out of range!");
        }

        if (data.length % blockSize != 0) {
            throw new IllegalArgumentException("Number of bytes is not a multiple of the blocksize!");
        }

        // do nothing
    }

    /** {@inheritDoc} */
    public void write(final long address, final byte[] data) throws Exception {

        if (!opened) {
            throw new IllegalStateException("You first have to open the Device!");
        }

        long blocks = data.length / blockSize;

        if (address < 0 || address + blocks > blockCount) {
            long adr;
            if (address < 0) {
                adr = address;
            } else {
                adr = address + blocks - 1;
            }
            throw new IllegalArgumentException("Address " + adr + " out of range.");
        }

        if (data.length % blockSize != 0) {
            throw new IllegalArgumentException("Number of bytes is not a multiple of the blocksize!");
        }

        // do nothing
    }

}

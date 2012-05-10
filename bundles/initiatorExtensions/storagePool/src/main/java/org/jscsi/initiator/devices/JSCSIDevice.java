/**
 * Copyright (c) 2012, University of Konstanz, Distributed Systems Group
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
package org.jscsi.initiator.devices;

import java.nio.ByteBuffer;

import org.jscsi.initiator.Configuration;
import org.jscsi.initiator.Initiator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>JSCSIDevice</h1>
 * <p>
 * Implements a jSCSI Device i.e. an initiator that can connect to one target.
 * </p>
 * 
 * @author Bastian Lemke
 */
public class JSCSIDevice implements Device {

    private final Initiator initiator;

    private final String target;

    private int blockSize = -1;

    private long blockCount = -1;

    /** The Logger interface. */
    private static final Logger LOGGER = LoggerFactory.getLogger(JSCSIDevice.class);

    /**
     * Constructor to create an JSCSIDevice. The Device has to be initialized
     * before it can be used.
     * 
     * @param targetName
     *            name of the target to connect to
     * @throws Exception
     *             if any error occurs
     */
    public JSCSIDevice(final String targetName) throws Exception {

        initiator = new Initiator(Configuration.create());
        target = targetName;
    }

    /** {@inheritDoc} */
    public void close() throws Exception {

        if (initiator == null) {
            throw new NullPointerException();
        }

        initiator.closeSession(target);
        blockSize = -1;
        blockCount = -1;
        LOGGER.info("Closed " + getName() + ".");
    }

    /** {@inheritDoc} */
    public int getBlockSize() {

        if (blockSize == -1) {
            throw new IllegalStateException("You first have to open the Device!");
        }

        return blockSize;
    }

    /** {@inheritDoc} */
    public String getName() {

        return "JSCSIDevice(" + target + ")";
    }

    /** {@inheritDoc} */
    public long getBlockCount() {

        if (blockCount == -1) {
            throw new IllegalStateException("You first have to open the Device!");
        }

        return blockCount;
    }

    /** {@inheritDoc} */
    public void open() throws Exception {

        if (blockCount != -1) {
            throw new IllegalStateException("JSCSIDevice is already opened!");
        }

        initiator.createSession(target);
        blockSize = (int)initiator.getBlockSize(target);
        blockCount = initiator.getCapacity(target);

        LOGGER.info("Initialized " + getName() + ".");
    }

    /** {@inheritDoc} */
    public void read(final long address, final byte[] data) throws Exception {

        if (blockCount == -1) {
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

        final ByteBuffer dst = ByteBuffer.allocate(data.length);
        initiator.read(target, dst, (int)address, data.length);
        dst.rewind();
        dst.get(data);
    }

    /** {@inheritDoc} */
    public void write(final long address, final byte[] data) throws Exception {

        if (blockCount == -1) {
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

        final ByteBuffer src = ByteBuffer.wrap(data);
        initiator.write(target, src, (int)address, data.length);
    }

}

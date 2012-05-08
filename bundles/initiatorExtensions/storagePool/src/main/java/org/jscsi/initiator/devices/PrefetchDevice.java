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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jscsi.utils.SoftHashMap;

/**
 * <h1>Prefetcher</h1>
 * <p>
 * A simple Prefetcher for an Device.
 * </p>
 * 
 * @author Bastian Lemke
 */
public class PrefetchDevice implements Device {

    private final Device device;

    /** Thread pool for prefetching-threads. */
    private final ExecutorService executor;

    private SoftHashMap<Long, byte[]> buffer;

    private long lastBlockAddress;

    private static final int PREFETCH_LENGTH_EXPONENT = 3;

    /**
     * Constructor to create an Prefetcher. The Device has to be initialized
     * before it can be used.
     * 
     * @param initDevice
     *            Device to prefetch
     * @throws Exception
     *             if any error occurs
     */
    public PrefetchDevice(final Device initDevice) throws Exception {

        this.device = initDevice;
        executor = Executors.newCachedThreadPool();
        buffer = new SoftHashMap<Long, byte[]>();
    }

    /** {@inheritDoc} */
    public void close() throws Exception {

        executor.shutdownNow();
        device.close();
    }

    /** {@inheritDoc} */
    public int getBlockSize() {

        return device.getBlockSize();
    }

    /** {@inheritDoc} */
    public String getName() {

        return device.getName();
    }

    /** {@inheritDoc} */
    public long getBlockCount() {

        return device.getBlockCount();
    }

    /** {@inheritDoc} */
    public void open() throws Exception {

        device.open();
    }

    /**
     * Return the actual number of blocks that are prefetched.
     * 
     * @return PREFETCH_LENGTH
     */
    public int getPrefetchLength() {

        return PREFETCH_LENGTH_EXPONENT;
    }

    /** {@inheritDoc} */
    public void read(final long address, final byte[] data) throws Exception {

        if (data.length % getBlockSize() != 0) {
            throw new IllegalArgumentException(
                    "Number of bytes is not a multiple of the blocksize!");
        }

        final long extentSize = data.length / getBlockSize();
        final long minAddress = address
                - (extentSize << PREFETCH_LENGTH_EXPONENT);
        final long maxAddress = address
                + (extentSize << PREFETCH_LENGTH_EXPONENT);

        byte[] prefetchedData = (byte[]) buffer.get(address);

        if (prefetchedData == null || prefetchedData.length != data.length) {
            if (lastBlockAddress == (address - extentSize)
                    && maxAddress < getBlockCount()) {
                prefetchedData = new byte[(data.length << PREFETCH_LENGTH_EXPONENT)
                        + data.length];

                device.read(address, prefetchedData);
                // copy first part to out parameter
                System.arraycopy(prefetchedData, 0, data, 0, data.length);
                // split prefetched data to store into buffer
                byte[] chunkedPrefetchedData;
                for (int i = 1; i <= (1 << PREFETCH_LENGTH_EXPONENT); i++) {
                    chunkedPrefetchedData = new byte[data.length];
                    System.arraycopy(prefetchedData, i * data.length,
                            chunkedPrefetchedData, 0, data.length);
                    buffer.put((address + i * extentSize),
                            chunkedPrefetchedData);
                }
            } else if (lastBlockAddress == (address + extentSize)
                    && minAddress >= 0) {
                prefetchedData = new byte[(data.length << PREFETCH_LENGTH_EXPONENT)
                        + data.length];

                device.read(minAddress, prefetchedData);
                // copy first part to out parameter
                System.arraycopy(prefetchedData, prefetchedData.length
                        - data.length, data, 0, data.length);
                // split prefetched data to store into buffer
                byte[] chunkedPrefetchedData;
                for (int i = 0; i < (1 << PREFETCH_LENGTH_EXPONENT); i++) {
                    chunkedPrefetchedData = new byte[data.length];
                    System.arraycopy(prefetchedData, i * data.length,
                            chunkedPrefetchedData, 0, data.length);
                    buffer.put((minAddress + i * extentSize),
                            chunkedPrefetchedData);
                }
            } else {
                device.read(address, data);
            }
        } else {
            System.arraycopy(prefetchedData, 0, data, 0, data.length);
        }
        lastBlockAddress = address;
    }

    /** {@inheritDoc} */
    public void write(final long address, final byte[] data) throws Exception {

        device.write(address, data);
    }
}

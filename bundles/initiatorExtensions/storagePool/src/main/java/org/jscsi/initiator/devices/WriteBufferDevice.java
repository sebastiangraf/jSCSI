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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <h1>Prefetcher</h1>
 * <p>
 * A simple Prefetcher for an Device.
 * </p>
 * 
 * @author Bastian Lemke
 */
public class WriteBufferDevice implements Device {

    private final Device device;

    private static final int MAX_WRITE_COUNT = 10;

    private final Map<Long, byte[]> buffer;

    private int writeCount;

    /**
     * Constructor to create an Prefetcher. The Device has to be initialized
     * before it can be used.
     * 
     * @param initDevice
     *            Device to prefetch
     * @throws Exception
     *             if any error occurs
     */
    public WriteBufferDevice(final Device initDevice) throws Exception {

        this.device = initDevice;
        buffer = new HashMap<Long, byte[]>();
        // flushThread = new FlushThread();
        // flushThread.start();
        writeCount = 0;
    }

    /** {@inheritDoc} */
    public void close() throws Exception {

        // flushThread.interrupt();
        flush();
        device.close();
    }

    /** {@inheritDoc} */
    public int getBlockSize() {

        int size = device.getBlockSize();
        return size;

    }

    /** {@inheritDoc} */
    public String getName() {

        String name = device.getName();
        try {
            flush();
        } catch (Exception e) {
            new RuntimeException(e);
        }
        return name;
    }

    /** {@inheritDoc} */
    public long getBlockCount() {

        long count = device.getBlockCount();
        try {
            flush();
        } catch (Exception e) {
            new RuntimeException(e);
        }
        return count;
    }

    /** {@inheritDoc} */
    public void open() throws Exception {

        flush();
        device.open();
    }

    /**
     * Flush the buffer to the target.
     * 
     * @throws Exception
     *             for any error
     */
    public final synchronized void flush() throws Exception {

        List<Long> sortedKeys = new ArrayList<Long>(buffer.keySet());
        Collections.sort(sortedKeys);

        while (sortedKeys.size() > 0) {
            long firstKey = sortedKeys.get(0);
            int firstDataLength = buffer.get(firstKey).length;
            int i = 1;
            while (i < sortedKeys.size()
                    && sortedKeys.get(i) == sortedKeys.get(i - 1)
                            + (firstDataLength / getBlockSize())) {
                i++;
            }
            byte[] data = new byte[firstDataLength * i];
            for (int j = 0; j < i; j++) {
                System.arraycopy(buffer.get(sortedKeys.get(j)), 0, data, j
                        * firstDataLength, firstDataLength);
            }
            device.write(firstKey, data);
            if (sortedKeys.size() != 1) {
                sortedKeys = sortedKeys.subList(i, sortedKeys.size());
            } else {
                sortedKeys.clear();
            }
        }
        buffer.clear();
    }

    /** {@inheritDoc} */
    public void read(final long address, final byte[] data) throws Exception {

        byte[] bufferedData = buffer.get(address);
        if (bufferedData != null && bufferedData.length == data.length) {
            System.arraycopy(bufferedData, 0, data, 0, data.length);
        } else {
            device.read(address, data);
        }
    }

    /** {@inheritDoc} */
    public void write(final long address, final byte[] data) throws Exception {

        buffer.put(address, data);
        writeCount++;
        if (writeCount >= MAX_WRITE_COUNT) {
            writeCount = 0;
            flush();
        }
    }

}

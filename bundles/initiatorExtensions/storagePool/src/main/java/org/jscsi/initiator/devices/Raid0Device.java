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

import java.util.List;
import java.util.Vector;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Raid0Device</h1>
 * <p>
 * Implements a RAID 0 Device with several Devices.
 * </p>
 * 
 * @author Bastian Lemke
 */
public class Raid0Device implements Device {

    private final Device[] devices;

    private int blockSize = -1;

    private long blockCount = -1;

    /** The Logger interface. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Raid0Device.class);

    /**
     * Size of the parts, that are distributed between the Devices. Must be a
     * multiple of blockSize.
     */
    private final int extendSize;

    /** Thread pool for write- and read-threads. */
    private final ExecutorService executor;

    /** Thread barrier for write- and read-threads. */
    private CyclicBarrier barrier;

    /**
     * Constructor to create a Raid0Device. The Device has to be initialized
     * before it can be used.
     * 
     * @param initDevices
     *            devices to use
     * @throws Exception
     *             if any error occurs
     */
    public Raid0Device(final Device[] initDevices) throws Exception {

        devices = initDevices;
        // create one thread per device
        executor = Executors.newFixedThreadPool(devices.length);
        extendSize = 8192;
    }

    /**
     * Constructor to create a Raid0Device. The Device has to be initialized
     * before it can be used.
     * 
     * @param initDevices
     *            devices to use
     * @param initExtendSize
     *            extend size to use
     * @throws Exception
     *             if any error occurs
     */
    public Raid0Device(final Device[] initDevices, final int initExtendSize) throws Exception {

        devices = initDevices;
        // create one thread per device
        executor = Executors.newFixedThreadPool(devices.length);
        extendSize = initExtendSize;
    }

    /** {@inheritDoc} */
    public void close() throws Exception {

        if (blockCount == -1) {
            throw new NullPointerException();
        }

        executor.shutdown();
        for (Device device : devices) {
            device.close();
        }
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

        String name = "Raid0Device(";
        for (Device device : devices) {
            name += device.getName() + "+";
        }
        return name.substring(0, name.length() - 1) + ")";
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
            throw new IllegalStateException("Raid0Device is already opened!");
        }

        for (Device device : devices) {
            device.open();
        }

        // check if all devices have the same block size
        blockSize = 0;
        for (Device device : devices) {
            if (blockSize == 0) {
                blockSize = device.getBlockSize();
            } else if (blockSize != device.getBlockSize()) {
                throw new IllegalArgumentException("All devices must have the same block size!");
            }
        }

        if (extendSize % blockSize != 0) {
            throw new IllegalArgumentException("extendSize must be a multiple of the blocksize!");
        }

        // available space = size of smallest device * #devices
        blockCount = Long.MAX_VALUE;
        for (Device device : devices) {
            blockCount = Math.min(blockCount, device.getBlockCount());
        }
        blockCount = (blockCount * blockSize / extendSize) * extendSize / blockSize;
        blockCount *= devices.length;
        LOGGER.info("Opened " + getName() + ".");
    }

    /** {@inheritDoc} */
    public void read(final long address, final byte[] data) throws Exception {

        if (blockCount == -1) {
            throw new IllegalStateException("You first have to open the Device!");
        }

        if (data.length % extendSize != 0) {
            throw new IllegalArgumentException("Number of bytes is not a multiple of the extend size ("
                + extendSize + ")!");
        }

        int fragments = data.length / extendSize;
        int blocks = data.length / blockSize;
        int blockFactor = extendSize / blockSize;

        if (address < 0 || address + blocks > blockCount) {
            long adr = address < 0 ? address : address + blocks - 1;
            throw new IllegalArgumentException("Address " + adr + " out of range.");
        }

        int parts = (fragments >= devices.length) ? devices.length : fragments;
        barrier = new CyclicBarrier(parts + 1);
        long actualAddress = ((address / blockFactor) / devices.length) * blockFactor;
        int actualDevice = (int)((address / blockFactor) % devices.length);
        List<byte[]> deviceData = new Vector<byte[]>();
        int deviceBlockCount;
        for (int i = 0; i < parts; i++) {
            deviceBlockCount = fragments / devices.length * blockFactor;
            if (i < (fragments % devices.length)) {
                deviceBlockCount += blockFactor;
            }
            deviceData.add(new byte[deviceBlockCount * blockSize]);

            if (deviceBlockCount != 0) {
                executor.execute(new ReadThread(devices[actualDevice], actualAddress, deviceData.get(i)));
            }
            if (actualDevice == devices.length - 1) {
                actualDevice = 0;
                actualAddress += blockFactor;
            } else {
                actualDevice++;
            }
        }
        barrier.await();

        /** Merge the results. */
        for (int i = 0; i < fragments; i++) {
            System.arraycopy(deviceData.get(i % devices.length), i / devices.length * extendSize, data, i
                * extendSize, extendSize);
        }
    }

    /** {@inheritDoc} */
    public void write(final long address, final byte[] data) throws Exception {

        if (blockCount == -1) {
            throw new IllegalStateException("You first have to open the Device!");
        }

        int fragments = data.length / extendSize;
        int blocks = data.length / blockSize;
        int blockFactor = extendSize / blockSize;

        if (address < 0 || address + blocks > blockCount) {
            long adr = address < 0 ? address : address + blocks - 1;
            throw new IllegalArgumentException("Address " + adr + " out of range.");
        }

        if (data.length % extendSize != 0) {
            throw new IllegalArgumentException("Number of bytes is not a multiple of the extend size ("
                + extendSize + ")!");
        }

        int parts = (fragments >= devices.length) ? devices.length : fragments;
        List<byte[]> deviceData = new Vector<byte[]>();
        long actualAddress = ((address / blockFactor) / devices.length) * blockFactor;
        int actualDevice = (int)((address / blockFactor) % devices.length);
        int deviceBlockCount;
        barrier = new CyclicBarrier(parts + 1);
        for (int i = 0; i < parts; i++) {
            deviceBlockCount = fragments / devices.length * blockFactor;
            if (i < (fragments % devices.length)) {
                deviceBlockCount += blockFactor;
            }
            deviceData.add(new byte[deviceBlockCount * blockSize]);
        }
        for (int i = 0; i < fragments; i++) {
            System.arraycopy(data, i * extendSize, deviceData.get(i % devices.length), i / devices.length
                * extendSize, extendSize);
        }
        for (int i = 0; i < parts; i++) {
            executor.execute(new WriteThread(devices[actualDevice], actualAddress, deviceData.get(i)));
            if (actualDevice == devices.length - 1) {
                actualDevice = 0;
                actualAddress += blockFactor;
            } else {
                actualDevice++;
            }
        }
        barrier.await();
    }

    /**
     * Private class to represent one single read-action in an own thread for
     * one target.
     * 
     * @author bastian
     */
    private final class ReadThread implements Runnable {

        private final Device device;

        private final long address;

        private final byte[] data;

        private ReadThread(final Device readDevice, final long readBlockAddress, final byte[] readData) {

            device = readDevice;
            address = readBlockAddress;
            data = readData;
        }

        public void run() {

            try {
                device.read(address, data);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Read " + data.length / blockSize + " blocks from address " + address
                        + " from " + device.getName());
                }
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Private class to represent one single write-action in an own thread for
     * one target.
     * 
     * @author Bastian Lemke
     */
    private final class WriteThread implements Runnable {

        private final Device device;

        private final long address;

        private final byte[] data;

        private WriteThread(final Device writeDevice, final long writeBlockAddress, final byte[] writeData) {

            device = writeDevice;
            address = writeBlockAddress;
            data = writeData;
        }

        public void run() {

            try {
                device.write(address, data);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Wrote " + data.length / blockSize + " blocks to address " + address
                        + " to " + device.getName());
                }
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}

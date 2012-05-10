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

import java.util.List;
import java.util.Vector;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <h1>Raid1Device</h1>
 * <p>
 * Implements a RAID 1 Device with several Devices.
 * </p>
 * 
 * @author Bastian Lemke
 */
public class Raid1Device implements Device {

    private final Device[] devices;

    private int blockSize = -1;

    private long blockCount = -1;

    /** The Logger interface. */
    private static final Logger LOGGER = LoggerFactory.getLogger(Raid1Device.class);

    /**
     * Pointer to next device to read data from. Used to distribute reads
     * between the devices.
     */
    private int nextTarget;

    /** Thread pool for write- and read-threads. */
    private final ExecutorService executor;

    /** Thread barrier for write- and read-threads. */
    private CyclicBarrier barrier;

    /**
     * Constructor to create an Raid1Device. The Device has to be initialized
     * before it can be used.
     * 
     * @param initDevices
     *            devices to use
     * @throws Exception
     *             if any error occurs
     */
    public Raid1Device(final Device[] initDevices) throws Exception {

        devices = initDevices;
        nextTarget = 0;
        // create one thread per device
        executor = Executors.newFixedThreadPool(devices.length);
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

        String name = "Raid1Device(";
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
            throw new IllegalStateException("Raid1Device is already opened!");
        }

        for (Device device : devices) {
            device.open();
        }

        // check if all devices have the same block size
        blockSize = 0;
        for (Device device : devices) {
            if (blockSize == 0) {
                blockSize = (int)device.getBlockSize();
            } else if (blockSize != (int)device.getBlockSize()) {
                throw new IllegalArgumentException("All devices must have the same block size!");
            }
        }

        // find the smallest device
        blockCount = Long.MAX_VALUE;
        for (Device device : devices) {
            blockCount = Math.min(blockCount, device.getBlockCount());
        }

    }

    /** {@inheritDoc} */
    public void read(final long address, final byte[] data) throws Exception {

        if (blockCount == -1) {
            throw new IllegalStateException("You first have to open the Device!");
        }

        int blocks = data.length / blockSize;

        if (address < 0 || address + blocks > blockCount) {
            long adr = address < 0 ? address : address + blocks - 1;
            throw new IllegalArgumentException("Address " + adr + " out of range.");
        }

        if (data.length % blockSize != 0) {
            throw new IllegalArgumentException("Number of bytes is not a multiple of the blocksize!");
        }

        int parts = (blocks >= devices.length) ? devices.length : (int)blocks;
        barrier = new CyclicBarrier(parts + 1);
        int targetBlockCount;
        List<byte[]> targetData = new Vector<byte[]>();
        int targetBlockAddress = (int)address;

        for (int i = 0; i < parts; i++) {
            targetBlockCount = blocks / devices.length;
            if (i < (blocks % devices.length)) {
                targetBlockCount++;
            }
            targetData.add(new byte[targetBlockCount * blockSize]);

            /** Start Thread to read Data from Target */
            if (targetBlockCount != 0) {
                executor.execute(new ReadThread(devices[nextTarget], targetBlockAddress, targetData.get(i)));
            }
            targetBlockAddress += targetBlockCount;
            nextTarget = (nextTarget < devices.length - 1) ? nextTarget + 1 : 0;
        }
        barrier.await();

        /** Merge the results. */
        int pos = 0;
        for (int i = 0; i < targetData.size(); i++) {
            System.arraycopy(targetData.get(i), 0, data, pos, targetData.get(i).length);
            pos += targetData.get(i).length;
        }
    }

    /** {@inheritDoc} */
    public void write(final long address, final byte[] data) throws Exception {

        if (blockCount == -1) {
            throw new IllegalStateException("You first have to open the Device!");
        }

        long blocks = data.length / blockSize;

        if (address < 0 || address + blocks > blockCount) {
            long adr = address < 0 ? address : address + blocks - 1;
            throw new IllegalArgumentException("Address " + adr + " out of range.");
        }

        if (data.length % blockSize != 0) {
            throw new IllegalArgumentException("Number of bytes is not a multiple of the blocksize!");
        }

        barrier = new CyclicBarrier(devices.length + 1);
        for (int i = 0; i < devices.length; i++) {
            executor.execute(new WriteThread(devices[i], (int)address, data));
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

        private final int address;

        private final byte[] data;

        private ReadThread(final Device readDevice, final int readBlockAddress, final byte[] readData) {

            device = readDevice;
            address = readBlockAddress;
            data = readData;
        }

        public void run() {

            try {
                device.read(address, data);
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

        private final int address;

        private final byte[] data;

        private WriteThread(final Device writeDevice, final int writeBlockAddress, final byte[] writeData) {

            device = writeDevice;
            address = writeBlockAddress;
            data = writeData;
        }

        public void run() {

            try {
                device.write(address, data);
                barrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

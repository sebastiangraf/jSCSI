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

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Lack of testembed, removing")
public class Raid1DeviceTest {

    /** previous targetNames - "disk5", "disk6" */
    private static final String[] targetNames = { "testing-xen2-disk1",
            "testing-xen2-disk2" };

    private static final Device[] jSCSIDevices = new Device[targetNames.length];

    /** Number of Blocks to write */
    private static final int TEST_DATA_SIZE = 1;

    private static byte[] testData;

    private static long address;

    private static Device device;

    private static Random randomGenerator;

    public Raid1DeviceTest() throws Exception {

        for (int i = 0; i < targetNames.length; i++) {
            jSCSIDevices[i] = new JSCSIDevice(targetNames[i]);
        }
    }

    @Before
    public final void setUp() throws Exception {

        device = new Raid1Device(jSCSIDevices);
        device.open();
        randomGenerator = new Random(System.currentTimeMillis());
        testData = new byte[TEST_DATA_SIZE * device.getBlockSize()];
        randomGenerator.nextBytes(testData);
    }

    @After
    public final void tearDown() throws Exception {

        device.close();
    }

    /**
     * Test write on the beginning of a Device.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test
    public final void testReadWriteBeginning() throws Exception {

        address = 0;

        device.write(address, testData);
        byte[] result = new byte[TEST_DATA_SIZE * device.getBlockSize()];
        device.read(address, result);
        for (int i = 0; i < TEST_DATA_SIZE * device.getBlockSize(); i++) {
            assertEquals(result[i], testData[i]);
        }
    }

    /**
     * Tests, if an exception is thrown, when the requested address is less than
     * 0.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWriteAddressToLow() throws Exception {

        address = -1;
        device.write(address, testData);
    }

    /**
     * Tests, if an exception is thrown, when the requested address is less than
     * 0.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testReadAddressToLow() throws Exception {

        address = -1;
        device.read(address, testData);
    }

    /**
     * Tests, if an exception is thrown, when data would exceed the maximum
     * available address.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWriteOverTheEnd() throws Exception {

        address = device.getBlockCount() - TEST_DATA_SIZE;
        byte[] data = new byte[TEST_DATA_SIZE * device.getBlockSize() * 2];
        randomGenerator.nextBytes(data);
        device.write(address, data);
    }

    /**
     * Tests, if an exception is thrown, when data would exceed the maximum
     * available address.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testReadOverTheEnd() throws Exception {

        address = device.getBlockCount() - TEST_DATA_SIZE;
        byte[] result = new byte[TEST_DATA_SIZE * device.getBlockSize() * 2];
        device.read(address, result);
    }

    /**
     * Test write on the end of a Device.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test
    public final void testReadWriteEnd() throws Exception {

        address = device.getBlockCount() - TEST_DATA_SIZE;

        device.write(address, testData);
        byte[] result = new byte[TEST_DATA_SIZE * device.getBlockSize()];
        device.read(address, result);
        for (int i = 0; i < TEST_DATA_SIZE; i++) {
            assertEquals(result[i], testData[i]);
        }
    }

    /**
     * Tests, if an exception is thrown, when the requested address higher than
     * the highest available address.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWriteAddressToHigh() throws Exception {

        address = device.getBlockCount() - TEST_DATA_SIZE + 1;
        device.write(address, testData);
    }

    /**
     * Tests, if an exception is thrown, when the requested address higher than
     * the highest available address.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testReadAddressToHigh() throws Exception {

        address = device.getBlockCount() - TEST_DATA_SIZE + 1;
        device.read(address, testData);
    }

    /**
     * Tests, if an exception is thrown, when data isn't a multiple of
     * blockSize.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testWriteIllegalSize() throws Exception {

        address = 0;
        byte[] data = new byte[1];
        randomGenerator.nextBytes(data);
        device.write(address, data);
    }

    /**
     * Tests, if an exception is thrown, when data isn't a multiple of
     * blockSize.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testReadIllegalSize() throws Exception {

        address = 0;
        byte[] result = new byte[1];
        device.read(address, result);
    }

    /**
     * Random write and read.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test
    public void testRandomReadWrite() throws Exception {

        address = randomGenerator.nextInt((int) device.getBlockCount());
        device.write(address, testData);
        final byte[] result = new byte[TEST_DATA_SIZE * device.getBlockSize()];

        device.read(address, result);
        for (int i = 0; i < TEST_DATA_SIZE * device.getBlockSize(); i++) {
            assertEquals(result[i], testData[i]);
        }
    }

    /**
     * Tests, if an exception is thrown, when data is written to an unopened
     * Raid1 Device.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(expected = IllegalStateException.class)
    public void testWriteNotOpened() throws Exception {

        Device newDevice = new Raid1Device(jSCSIDevices);
        address = 0;
        newDevice.write(address, testData);
    }

    /**
     * Tests, if an exception is thrown, when data is read from an unopened
     * Raid1 Device.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test(expected = IllegalStateException.class)
    public void testReadNotOpened() throws Exception {

        Device newDevice = new Raid1Device(jSCSIDevices);
        address = 0;
        byte[] result = new byte[TEST_DATA_SIZE * device.getBlockSize()];
        newDevice.read(address, result);
    }
}

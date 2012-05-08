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
public class WriteBufferDeviceTest {

    /** previous Target_Name - "disk6" */
    private static final String TARGET_NAME = "testing-xen2-disk1";

    private static WriteBufferDevice device;

    /* Byte arrays (for all blocksizes) which are written to the targets. */
    private final byte[] testDataBlock8kb;

    private final byte[] testDataBlock128kb;

    Random randomGenerator;

    // ---------------------------------------------------------------------------
    // ---------------------------------------------------------------------------

    public WriteBufferDeviceTest() {

        // create byte arrays with random values for all blocksizes.
        randomGenerator = new Random(System.currentTimeMillis());
        testDataBlock8kb = new byte[8 * 1024];
        randomGenerator.nextBytes(testDataBlock8kb);
        testDataBlock128kb = new byte[128 * 1024];
        randomGenerator.nextBytes(testDataBlock128kb);
    }

    @Before
    public final void setUp() throws Exception {

        device = new WriteBufferDevice(new JSCSIDevice(TARGET_NAME));
        device.open();
    }

    @After
    public final void tearDown() throws Exception {

        device.close();
    }

    /**
     * Test buffered write with 8kb blocks on the device.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test
    public final void testReadWriteBeginning8kb() throws Exception {

        int writeBlockCount = 50;
        int blockFactor = testDataBlock8kb.length / device.getBlockSize();
        long address = 0;

        for (int i = 0; i < writeBlockCount; i++) {
            device.write(address + i * blockFactor, testDataBlock8kb);
        }
        device.flush();
        for (int j = 0; j < writeBlockCount; j++) {
            byte[] result = new byte[testDataBlock8kb.length];
            device.read(address + j * blockFactor, result);
            for (int i = 0; i < testDataBlock8kb.length; i++) {
                assertEquals(result[i], testDataBlock8kb[i]);
            }
        }
    }

    /**
     * Test buffered write with 128kb blocks on the device.
     * 
     * @throws Exception
     *             These should never be thrown.
     */
    @Test
    public final void testReadWriteBeginning128kb() throws Exception {

        int writeBlockCount = 50;
        int blockFactor = testDataBlock128kb.length / device.getBlockSize();
        long address = 0;

        for (int i = 0; i < writeBlockCount; i++) {
            device.write(address + i * blockFactor, testDataBlock128kb);
        }
        device.flush();
        for (int j = 0; j < writeBlockCount; j++) {
            byte[] result = new byte[testDataBlock128kb.length];
            device.read(address + j * blockFactor, result);
            for (int i = 0; i < testDataBlock128kb.length; i++) {
                assertEquals(result[i], testDataBlock128kb[i]);
            }
        }
    }

}

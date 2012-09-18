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
package org.jscsi.parser.datasegment;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.nio.ByteBuffer;

import org.jscsi.parser.datasegment.DataSegmentFactory.DataSegmentFormat;

/**
 * Testing the correctness of the BinaryDataSegment.
 * 
 * @author Volker Wildi
 */
public final class BinaryDataSegmentTest {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    private static final int CHUNK_SIZE = 4;

    /** Test case arrays. */
    private static final byte[] TEST_CASE_1_ARRAY = {
        0x34, 0x54, 0x77, 0x32, (byte)0xAF
    };

    private static final byte[] TEST_CASE_1_ARRAY_LONG = {
        0x34, 0x54, 0x77, 0x32, (byte)0xAF, 0x00, 0x00, 0x00
    };

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    @Test
    public final void testDeserializeBuffer() {

        final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);
        assertNotNull(dataSegment);
        assertEquals(0, dataSegment.getLength());

        final ByteBuffer testBuffer = ByteBuffer.wrap(TEST_CASE_1_ARRAY);
        dataSegment.deserialize(testBuffer, TEST_CASE_1_ARRAY.length);
        assertEquals(TEST_CASE_1_ARRAY.length, dataSegment.getLength());

        final ByteBuffer expectedResult = ByteBuffer.wrap(TEST_CASE_1_ARRAY_LONG);
        dataSegment.dataBuffer.rewind();
        assertTrue(expectedResult.equals(dataSegment.dataBuffer));
    }

    @Test
    public final void testAppendBuffer() {

        final ByteBuffer testBuffer = ByteBuffer.wrap(TEST_CASE_1_ARRAY);
        assertNotNull(testBuffer);

        final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);
        dataSegment.append(testBuffer, testBuffer.remaining());

        assertNotNull(dataSegment);
        assertEquals(TEST_CASE_1_ARRAY.length, dataSegment.getLength());

        final ByteBuffer expectedResult = ByteBuffer.wrap(TEST_CASE_1_ARRAY_LONG);
        dataSegment.dataBuffer.rewind();
        assertTrue(expectedResult.equals(dataSegment.dataBuffer));
    }

    @Test
    public final void testSerializeBuffer() {

        final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);
        assertNotNull(dataSegment);
        assertEquals(0, dataSegment.getLength());

        final ByteBuffer testBuffer = ByteBuffer.wrap(TEST_CASE_1_ARRAY);
        dataSegment.deserialize(testBuffer, TEST_CASE_1_ARRAY.length);
        assertEquals(TEST_CASE_1_ARRAY.length, dataSegment.getLength());

        final ByteBuffer expectedResult = ByteBuffer.wrap(TEST_CASE_1_ARRAY_LONG);
        dataSegment.dataBuffer.rewind();

        final ByteBuffer exportedDataSegment =
            ByteBuffer.allocate(AbstractDataSegment.getTotalLength(dataSegment.getLength()));
        dataSegment.serialize(exportedDataSegment, 0);
        exportedDataSegment.rewind();

        assertTrue(expectedResult.equals(exportedDataSegment));
    }

    /**
     * Tests the clear method. The result has to be an empty object.
     */
    @Test
    public final void testClear() {

        final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);

        assertNotNull(dataSegment);
        assertEquals(0, dataSegment.getLength());

        final ByteBuffer testBuffer = ByteBuffer.wrap(TEST_CASE_1_ARRAY);
        dataSegment.deserialize(testBuffer, TEST_CASE_1_ARRAY.length);
        assertEquals(TEST_CASE_1_ARRAY.length, dataSegment.getLength());

        dataSegment.clear();

        assertNotNull(dataSegment);
        assertEquals(0, dataSegment.getLength());
    }

    /**
     * Tests the equals method. The result has to be <code>true</code>.
     */
    @Test
    public final void testEquals() {

        final ByteBuffer testBuffer = ByteBuffer.allocate(TEST_CASE_1_ARRAY.length);
        assertNotNull(testBuffer);
        testBuffer.put(TEST_CASE_1_ARRAY);
        testBuffer.rewind();

        final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);
        dataSegment.append(testBuffer, testBuffer.limit());
        assertNotNull(dataSegment);
        assertEquals(5, dataSegment.getLength());

        final BinaryDataSegment anotherDataSegment = new BinaryDataSegment(CHUNK_SIZE);
        testBuffer.rewind();
        anotherDataSegment.append(testBuffer, testBuffer.limit());
        assertNotNull(anotherDataSegment);
        assertEquals(5, anotherDataSegment.getLength());

        assertTrue(dataSegment.equals(anotherDataSegment));
    }

    /**
     * Tests the correct functionality of the serialize method. The given
     * destination buffer has size, which is not a multiple of
     * Constants.BYTES_PER_INT.
     */
    @Test(expectedExceptions = IllegalArgumentException.class)
    public final void testSerialize2() {

        final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);
        final ByteBuffer serialized = ByteBuffer.allocate(TEST_CASE_1_ARRAY.length);
        dataSegment.serialize(serialized, 0);
    }

    /**
     * Tests the correct functionality of the serialize and the deserialize
     * method.
     */
    @Test
    public final void testSerializeAndDeserialize() {

        final ByteBuffer testBuffer = ByteBuffer.wrap(TEST_CASE_1_ARRAY);
        final BinaryDataSegment dataSegment = new BinaryDataSegment(CHUNK_SIZE);
        assertEquals(0, dataSegment.getLength());

        dataSegment.append(testBuffer, testBuffer.remaining());
        assertNotNull(dataSegment);
        assertEquals(TEST_CASE_1_ARRAY.length, dataSegment.getLength());

        final int totalSize = AbstractDataSegment.getTotalLength(TEST_CASE_1_ARRAY.length);
        final ByteBuffer serialized = ByteBuffer.allocate(totalSize);
        assertEquals(totalSize, dataSegment.serialize(serialized, 0));
        serialized.rewind();

        final BinaryDataSegment anotherDataSegment = new BinaryDataSegment(CHUNK_SIZE);
        assertNotNull(anotherDataSegment);
        assertEquals(0, anotherDataSegment.getLength());

        assertEquals(totalSize, anotherDataSegment.deserialize(serialized, serialized.remaining()));
        assertNotNull(anotherDataSegment);
        assertEquals(totalSize, anotherDataSegment.getLength());
    }

    /**
     * Tests the correct functionality of the <code>DataSegmentFactory</code> and the corresponding
     * <code>DataSegmentIterator</code>.
     */
    @Test
    public final void testDataSegmentIterator() {

        final ByteBuffer testBuffer = ByteBuffer.allocate(10 * 16 * 1024);
        final IDataSegment dataSegment =
            DataSegmentFactory.create(testBuffer, 10000, 57344, DataSegmentFormat.BINARY, 8192);
        final IDataSegmentIterator iterator = dataSegment.iterator();

        int counter = 0;
        while (iterator.hasNext()) {
            iterator.next(8192);
            counter++;
        }

        assertEquals(7, counter);
    }

    /**
     * Tests the correct functionality of the <code>DataSegmentFactory</code> and the corresponding
     * <code>DataSegmentIterator</code>.
     */
    @Test
    public final void testDataSegmentIterator2() {

        final ByteBuffer testBuffer = ByteBuffer.allocate(10 * 16 * 1024);
        final IDataSegment dataSegment =
            DataSegmentFactory.create(testBuffer, 10000, 56344, DataSegmentFormat.BINARY, 8192);
        final IDataSegmentIterator iterator = dataSegment.iterator();

        int counter = 0;
        while (iterator.hasNext()) {
            iterator.next(8192);
            counter++;
        }

        assertEquals(7, counter);
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
}

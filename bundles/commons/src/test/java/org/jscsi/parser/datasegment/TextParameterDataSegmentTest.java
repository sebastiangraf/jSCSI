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

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import java.nio.ByteBuffer;

/**
 * Testing the correctness of the TextParameterDataSegment.
 * 
 * @author Volker Wildi
 */
public final class TextParameterDataSegmentTest {

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    private static final int CHUNK_SIZE = 3;

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Tests the constructor, if a valid maximum length is given.
     */
    @Test
    public final void testConstructor1() {

        final TextParameterDataSegment dataSegment = new TextParameterDataSegment(CHUNK_SIZE);
        assertNotNull(dataSegment);
        assertEquals(0, dataSegment.getLength());
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Tests the clear method. The result has to be an empty object.
     * 
     * @throws Exception
     *             should never be thrown.
     */
    @Test
    public final void testClear() throws Exception {

        final TextParameterDataSegment dataSegment = new TextParameterDataSegment(CHUNK_SIZE);
        assertNotNull(dataSegment);

        assertTrue(dataSegment.getSettings().entrySet().isEmpty());
        dataSegment.add(OperationalTextKey.HEADER_DIGEST, "Yes");
        assertFalse(dataSegment.getSettings().entrySet().isEmpty());

        dataSegment.clear();

        assertNotNull(dataSegment);
        assertTrue(dataSegment.getSettings().entrySet().isEmpty());
    }

    /**
     * Tests the correct functionality of the add method.
     * 
     * @throws Exception
     *             should never be thrown.
     */
    @Test
    public final void testAdd() throws Exception {

        final TextParameterDataSegment dataSegment = new TextParameterDataSegment(CHUNK_SIZE);
        assertNotNull(dataSegment);

        dataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
        dataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
        dataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "50");

        assertEquals(3, dataSegment.getSettings().entrySet().size());
        assertFalse(dataSegment.getSettings().entrySet().isEmpty());
    }

    /**
     * Tests the correct functionality of the addAll method.
     * 
     * @throws Exception
     *             should never be thrown.
     */
    @Test
    public final void testAddAll() throws Exception {

        final TextParameterDataSegment dataSegment = new TextParameterDataSegment(CHUNK_SIZE);

        assertNotNull(dataSegment);

        dataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
        dataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
        dataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "50");

        assertEquals(3, dataSegment.getSettings().entrySet().size());
        assertFalse(dataSegment.getSettings().entrySet().isEmpty());

        final TextParameterDataSegment anotherDataSegment = new TextParameterDataSegment(CHUNK_SIZE);
        assertNotNull(anotherDataSegment);

        anotherDataSegment.addAll(dataSegment.getSettings());
        assertNotNull(anotherDataSegment);
        assertEquals(3, anotherDataSegment.getSettings().entrySet().size());
        assertFalse(anotherDataSegment.getSettings().entrySet().isEmpty());
    }

    /**
     * Tests the correct functionality of the equals method. The two compared
     * <code>TextParameterDataSegment</code> has to be treated as equal.
     * 
     * @throws Exception
     *             should never be thrown.
     */
    @Test
    public final void testEquals() throws Exception {

        final TextParameterDataSegment dataSegment = new TextParameterDataSegment(CHUNK_SIZE);

        assertNotNull(dataSegment);
        assertEquals(0, dataSegment.getLength());

        dataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
        dataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
        dataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "50");

        assertEquals(3, dataSegment.getSettings().entrySet().size());
        assertFalse(dataSegment.getSettings().entrySet().isEmpty());

        final TextParameterDataSegment anotherDataSegment = new TextParameterDataSegment(CHUNK_SIZE);
        assertNotNull(anotherDataSegment);

        anotherDataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
        anotherDataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
        anotherDataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "50");

        assertEquals(3, anotherDataSegment.getSettings().entrySet().size());
        assertFalse(anotherDataSegment.getSettings().entrySet().isEmpty());

        assertTrue(dataSegment.equals(anotherDataSegment));
    }

    /**
     * Tests the correct functionality of the equals method. The two compared
     * <code>TextParameterDataSegment</code> has to be treated as unequal.
     * 
     * @throws Exception
     *             should never be thrown.
     */
    @Test
    public final void testUnEquals() throws Exception {

        final TextParameterDataSegment dataSegment = new TextParameterDataSegment(CHUNK_SIZE);

        assertNotNull(dataSegment);
        assertEquals(0, dataSegment.getLength());

        dataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
        dataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
        dataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "50");

        assertEquals(3, dataSegment.getSettings().entrySet().size());
        assertFalse(dataSegment.getSettings().entrySet().isEmpty());

        final TextParameterDataSegment anotherDataSegment = new TextParameterDataSegment(CHUNK_SIZE);
        assertNotNull(anotherDataSegment);

        anotherDataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
        anotherDataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
        anotherDataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "55");

        assertEquals(3, anotherDataSegment.getSettings().entrySet().size());
        assertFalse(anotherDataSegment.getSettings().entrySet().isEmpty());
        assertFalse(dataSegment.equals(anotherDataSegment));
    }

    /**
     * Tests the correct functionality of the serialize and the deserialize
     * method.
     */
    @Test
    public final void testSerializeAndDeserialize() {

        final TextParameterDataSegment dataSegment = new TextParameterDataSegment(CHUNK_SIZE);

        assertNotNull(dataSegment);
        dataSegment.add(OperationalTextKey.DATA_DIGEST, "Yes");
        dataSegment.add(OperationalTextKey.HEADER_DIGEST, "No");
        dataSegment.add(OperationalTextKey.MAX_CONNECTIONS, "50");

        final int totalSize = AbstractDataSegment.getTotalLength(dataSegment.getLength());
        final ByteBuffer serialized = ByteBuffer.allocate(totalSize);
        assertEquals(totalSize, dataSegment.serialize(serialized, 0));
        serialized.rewind();

        final TextParameterDataSegment anotherDataSegment =
            new TextParameterDataSegment(dataSegment.getLength());
        assertEquals(AbstractDataSegment.getTotalLength(dataSegment.getLength()), anotherDataSegment
            .deserialize(serialized, serialized.remaining()));
        assertNotNull(anotherDataSegment);
        assertEquals(AbstractDataSegment.getTotalLength(dataSegment.getLength()), anotherDataSegment
            .getLength());
        assertEquals(AbstractDataSegment.getTotalLength(dataSegment.getLength()), AbstractDataSegment
            .getTotalLength(anotherDataSegment.getLength()));

        assertFalse(dataSegment.equals(anotherDataSegment));
    }

    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
    // --------------------------------------------------------------------------
}

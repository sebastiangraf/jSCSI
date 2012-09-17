package org.jscsi.target.util;

import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

public class FastByteArrayProviderTest {

    private static final int CAPACITY = 4;

    private static final FastByteArrayProvider PROVIDER = new FastByteArrayProvider(CAPACITY);

    @Test
    public void testFastByteArrayProvider() {
        int capacity = 13;
        FastByteArrayProvider p = new FastByteArrayProvider(capacity);
        assert (p.getAll().length == capacity);
    }

    @Test
    public void testGetArray() {

        // fill all four slots ...
        PROVIDER.getArray(1);
        PROVIDER.getArray(2);
        PROVIDER.getArray(3);
        PROVIDER.getArray(4);
        // ... and test
        final byte[][] arrays = PROVIDER.getAll();
        for (int i = 0; i < CAPACITY; ++i)
            assertEquals(arrays[i].length, i + 1);// [1][2][3][4]

        // move last element to the front (3 swaps) ...
        for (int i = 0; i < 3; ++i)
            PROVIDER.getArray(4);
        // ... and test
        assert (arrays[0].length == 4);// [4][1][2][3]
        for (int i = 1; i < CAPACITY; ++i)
            assertEquals(arrays[i].length, i);

        // replace last element
        PROVIDER.getArray(5);// [4][1][2][5]
        assertEquals(4, arrays[0].length);
        assertEquals(1, arrays[1].length);
        assertEquals(2, arrays[2].length);
        assertEquals(5, arrays[3].length);
    }

    @Test
    public void testGetAll() {
        assertEquals(CAPACITY, PROVIDER.getAll().length);
    }

}

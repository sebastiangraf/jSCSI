package org.jscsi.target.util;

import org.junit.Test;

public class FastByteArrayProviderTest {

    private static final int CAPACITY = 4;

    private static final FastByteArrayProvider PROVIDER = new FastByteArrayProvider(
            CAPACITY);

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
            assert (arrays[i].length == i + 1);// [1][2][3][4]

        // move last element to the front (3 swaps) ...
        for (int i = 0; i < 3; ++i)
            PROVIDER.getArray(4);
        // ... and test
        assert (arrays[0].length == 4);// [4][1][2][3]
        for (int i = 1; i < CAPACITY; ++i)
            assert (arrays[i].length == i);

        // replace last element
        PROVIDER.getArray(5);// [4][1][2][5]
        assert (arrays[0].length == 4);
        assert (arrays[1].length == 1);
        assert (arrays[2].length == 2);
        assert (arrays[3].length == 5);
    }

    @Test
    public void testGetAll() {
        assert (PROVIDER.getAll().length == CAPACITY);
    }

}

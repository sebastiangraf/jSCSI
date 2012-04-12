package org.jscsi.target.util;

import static org.junit.Assert.fail;

import org.junit.Test;

public class ReadWriteTest {

    @Test
    public void testReadTwoByteIntByteArrayInt() {
        byte[] bytes = new byte[2];
        for (int i = 0; i < 65536; ++i)
            if (i != ReadWrite.readTwoByteInt(bytes, 0)) {
                System.out.println(i + " " + java.util.Arrays.toString(bytes)
                    + ReadWrite.readTwoByteInt(bytes, 0));
                fail("wrong number read");
            } else
                increment(bytes);
    }

    @Test
    public void testReadThreeByteIntByteArrayInt() {
        byte[] bytes = new byte[3];
        for (int i = 0; i < 16777216; ++i)
            if (i != ReadWrite.readThreeByteInt(bytes, 0)) {
                System.out.println(i + " " + java.util.Arrays.toString(bytes)
                    + ReadWrite.readThreeByteInt(bytes, 0));
                fail("wrong number read");
            } else
                increment(bytes);
    }

    // works, testing would take really long (~60s)
    // @Test
    public void testReadFourByteIntByteArrayInt() {
        byte[] bytes = new byte[4];
        // test number 0
        if (ReadWrite.readFourByteInt(bytes, 0) != 0)
            fail("wrong number read");
        // test remaining numbers
        bytes[3] = 1;
        int i = 1;
        while (i != 0) {
            if (i != ReadWrite.readFourByteInt(bytes, 0)) {
                System.out.println(i + " " + java.util.Arrays.toString(bytes)
                    + ReadWrite.readFourByteInt(bytes, 0));
                fail("wrong number read");
            } else
                increment(bytes);
            ++i;
        }
    }

    private void increment(byte[] bytes) {
        for (int i = bytes.length - 1; i >= 0; --i) {
            ++bytes[i];
            if (bytes[i] != 0)
                return;
        }
    }
}

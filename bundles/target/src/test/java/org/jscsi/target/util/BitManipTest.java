package org.jscsi.target.util;

import static org.testng.Assert.fail;
import org.testng.annotations.Test;

public class BitManipTest {

    @Test
    public void testSetBit() {
        // test by counting
        byte b = 0;

        for (int i = 1; i < 256; ++i) {
            b = increment(b);
            int control = b & 255;
            if (control != i)
                fail();
        }
    }

    private static byte increment(final byte b) {
        // return (byte)(b + 1);
        byte returnByte = b;

        for (int i = 0; i < 8; ++i) {
            if (BitManip.getBit(b, i))
                returnByte = BitManip.getByteWithBitSet(returnByte, i, false);
            else {
                returnByte = BitManip.getByteWithBitSet(returnByte, i, true);
                break;
            }
        }

        return returnByte;
    }

}

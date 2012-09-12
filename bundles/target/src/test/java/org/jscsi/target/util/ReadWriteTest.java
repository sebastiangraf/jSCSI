package org.jscsi.target.util;

import java.nio.ByteBuffer;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ReadWriteTest {

    @Test
    public void testReadOneByteIntByteArrayInt() {
        byte[] bytes = new byte[1];
        for (int i = 0; i < 256; i++) {
            int twoByte = ReadWrite.readOneByteInt(bytes, 0);
            if (i != twoByte) {
                System.out.println(i + " " + java.util.Arrays.toString(bytes)
                    + ReadWrite.readOneByteInt(bytes, 0));
                Assert.fail("wrong number read");
            } else
                increment(bytes);
        }
    }

    @Test
    public void testReadTwoByteIntByteArrayInt() {
        byte[] bytes = new byte[2];
        for (int i = 0; i < 65536; i++) {
            int twoByte = ReadWrite.readTwoByteInt(bytes, 0);
            if (i != twoByte) {
                System.out.println(i + " " + java.util.Arrays.toString(bytes)
                    + ReadWrite.readTwoByteInt(bytes, 0));
                Assert.fail("wrong number read");
            } else
                increment(bytes);
        }
    }

    @Test
    public void testReadThreeByteIntByteArrayInt() {
        byte[] bytes = new byte[3];
        for (int i = 0; i < 16777216; i++) {
            int threeByte = ReadWrite.readThreeByteInt(bytes, 0);
            if (i != threeByte) {
                System.out.println(i + " " + java.util.Arrays.toString(bytes)
                    + ReadWrite.readThreeByteInt(bytes, 0));
                Assert.fail("wrong number read");
            } else
                increment(bytes);
        }
    }

    // works, testing would take really long (~60s)
    @Test
    public void testReadFourByteIntByteArrayInt() {
        byte[] bytes = new byte[4];
        // test number 0
        if (ReadWrite.readFourByteInt(bytes, 0) != 0)
            Assert.fail("wrong number read");
        // test remaining numbers
        bytes[3] = 1;
        int i = 1;
        int fourByte;
        while (i != 0) {
            fourByte = ReadWrite.readFourByteInt(bytes, 0);
            if (i != fourByte) {
                System.out.println(i + " " + java.util.Arrays.toString(bytes)
                    + ReadWrite.readFourByteInt(bytes, 0));
                Assert.fail("wrong number read");
            } else
                increment(bytes);
            ++i;
        }
    }

    @Test
    public void testWriteInt() {
        ByteBuffer intBuffer = ByteBuffer.allocate(4);

        ReadWrite.writeInt(42, intBuffer, 0);
        Assert.assertTrue(ReadWrite.readUnsignedInt(intBuffer, 0) == 42);
    }

    @Test
    public void testWriteTwoByteIntByteArrayInt() {
        ByteBuffer intBuffer = ByteBuffer.allocate(4);

        ReadWrite.writeTwoByteInt(intBuffer, 42, 0);
        Assert.assertTrue(ReadWrite.readTwoByteInt(intBuffer, 0) == 42);
    }

    @Test
    public void testWriteThreeByteIntByteArrayInt() {
        ByteBuffer intBuffer = ByteBuffer.allocate(4);

        ReadWrite.writeThreeByteInt(intBuffer, 42, 0);
        Assert.assertTrue(ReadWrite.readThreeByteInt(intBuffer, 0) == 42);
    }

    @Test
    public void testWriteLong() {
        ByteBuffer longBuffer = ByteBuffer.allocate(8);
        ReadWrite.writeLong(longBuffer, 42, 0);
    }

    @Test
    public void testAppendTextDataSegmentToStringBuffer() {
        ByteBuffer stringBuffer = ByteBuffer.allocate(32);
        String hello = "hello world";

        stringBuffer.put(hello.getBytes());

        StringBuilder builder = new StringBuilder("...");
        ReadWrite.appendTextDataSegmentToStringBuffer(stringBuffer, builder);

        stringBuffer.clear();
        stringBuffer.put(hello.getBytes());

        Assert.assertEquals(builder.toString(), new StringBuilder("...").append(
            new String(stringBuffer.array())).toString());
    }

    @Test
    public void testByteBufferToString() {
        ByteBuffer stringBuffer = ByteBuffer.allocate(32);
        String hello = "hello world";

        stringBuffer.put(hello.getBytes());

        String s = ReadWrite.byteBufferToString(stringBuffer);
        Assert.assertEquals(hello, s.trim());
    }

    private void increment(byte[] bytes) {
        for (int i = bytes.length - 1; i >= 0; --i) {
            ++bytes[i];
            if (bytes[i] != 0)
                return;
        }
    }
}

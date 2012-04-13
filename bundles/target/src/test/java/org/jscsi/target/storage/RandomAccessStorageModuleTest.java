package org.jscsi.target.storage;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.jscsi.target.storage.AbstractStorageModule.VIRTUAL_BLOCK_SIZE;
public class RandomAccessStorageModuleTest {

    private static final String TEST_FILE_NAME = "storage_test_file.dat";

    private static AbstractStorageModule module = null;

    private static final int TEST_FILE_SIZE = 1024;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

        // make sure the file exists, has the correct length and is
        // accessible via module
        File file = new File(TEST_FILE_NAME);
        if (!file.exists())
            file.createNewFile();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.setLength(TEST_FILE_SIZE);
        raf.close();
        module = AbstractStorageModule.open(file);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        module.close();// must be closed for delete to work
        File file = new File(TEST_FILE_NAME);
        file.delete();
    }

    @Test
    public void testReadAndWrite() {

        // create and initialize arrays
        final byte[] writeArray = new byte[TEST_FILE_SIZE];
        for (int i = 0; i < TEST_FILE_SIZE; ++i)
            writeArray[i] = (byte)(Math.random() * 256);
        final byte[] readArray = new byte[TEST_FILE_SIZE];

        try {

            // write
            module.write(writeArray,// bytes (source)
                0,// bytesOffset
                TEST_FILE_SIZE,// length
                0);// storage index

            // read
            module.read(readArray,// bytes (destination)
                0,// bytesOffset
                TEST_FILE_SIZE,// length
                0);// storageIndex

        } catch (IOException e) {
            fail("IOException was thrown");
        }

        // check for errors
        for (int i = 0; i < TEST_FILE_SIZE; ++i)
            if (writeArray[i] != readArray[i])
                fail("values do not match");

    }

    @Test
    public void testCheckBounds0() {
        // should all be within bounds
        int result = module.checkBounds(0,// logicalBlockAddress
            2);// transferLengthInBlocks
        assert (result == 0);
        result = module.checkBounds(1,// logicalBlockAddress
            1);// transferLengthInBlocks
        assert (result == 0);
        result = module.checkBounds(1,// logicalBlockAddress
            1);// transferLengthInBlocks
        assert (result == 0);
        result = module.checkBounds(0,// logicalBlockAddress
            0);// transferLengthInBlocks
        assert (result == 0);
    }

    @Test
    public void testCheckBounds1() {
        // wrong logical block address
        int result = module.checkBounds(-1,// logicalBlockAddress
            1);// transferLengthInBlocks
        assert (result == 1);
        result = module.checkBounds(2,// logicalBlockAddress
            1);// transferLengthInBlocks
        assert (result == 1);
    }

    @Test
    public void testCheckBounds2() {
        int result = module.checkBounds(0,// logicalBlockAddress
            3);// transferLengthInBlocks
        assert (result == 2);
        result = module.checkBounds(0,// logicalBlockAddress
            -1);// transferLengthInBlocks
        assert (result == 2);
    }

    @Test
    public void testOpen() {
        // behavior to test is performed in setUpBeforeClass()
        assert (module != null);
    }

}

package org.jscsi.target.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SynchronizedRandomAccessStorageModule extends RandomAccessStorageModule implements IStorageModule {

    protected SynchronizedRandomAccessStorageModule(long sizeInBlocks, RandomAccessFile randomAccessFile)
        throws FileNotFoundException {
        super(sizeInBlocks, randomAccessFile);
    }

    @Override
    public synchronized void read(byte[] bytes, int bytesOffset, int length, long storageIndex)
        throws IOException {
        super.read(bytes, bytesOffset, length, storageIndex);
    }

    @Override
    public synchronized void write(byte[] bytes, int bytesOffset, int length, long storageIndex)
        throws IOException {
        super.write(bytes, bytesOffset, length, storageIndex);
    }

}

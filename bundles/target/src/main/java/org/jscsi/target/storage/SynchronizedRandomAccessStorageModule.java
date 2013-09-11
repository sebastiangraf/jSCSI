package org.jscsi.target.storage;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class SynchronizedRandomAccessStorageModule extends RandomAccessStorageModule implements IStorageModule {

    public SynchronizedRandomAccessStorageModule (long sizeInBlocks, File file) throws FileNotFoundException {
        super(sizeInBlocks, file);
    }

    @Override
    public synchronized void read (byte[] bytes, long storageIndex) throws IOException {
        super.read(bytes, storageIndex);
    }

    @Override
    public synchronized void write (byte[] bytes, long storageIndex) throws IOException {
        super.write(bytes, storageIndex);
    }

}

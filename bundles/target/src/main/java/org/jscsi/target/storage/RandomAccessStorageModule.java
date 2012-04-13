package org.jscsi.target.storage;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Instances of this class can be used for persistent storage of data. They are
 * backed by a {@link RandomAccessFile}, which will immediately write all
 * changes in the data to hard-disk.
 * <p>
 * This class is <b>not</b> thread-safe.
 * 
 * @see java.io.RandomAccessFile
 * @author Andreas Ergenzinger
 */
public class RandomAccessStorageModule extends AbstractStorageModule {

    /**
     * The {@link RandomAccessFile} used for accessing the storage medium.
     * 
     * @see #MODE
     */
    private RandomAccessFile randomAccessFile;

    /**
     * Creates a new {@link RandomAccessStorageModule} backed by the specified
     * file. If no such file exists, a {@link FileNotFoundException} will be
     * thrown.
     * 
     * @param sizeInBlocks
     *            blocksize for this module
     * @param randomAccessFile
     *            the path to the file serving as storage medium
     * @throws FileNotFoundException
     *             if the specified file does not exist
     */
    protected RandomAccessStorageModule(final long sizeInBlocks, final RandomAccessFile randomAccessFile)
        throws FileNotFoundException {
        super(sizeInBlocks);
        this.randomAccessFile = randomAccessFile;
    }

    @Override
    public void read(byte[] bytes, int bytesOffset, int length, long storageIndex) throws IOException {
        randomAccessFile.seek(storageIndex);
        randomAccessFile.read(bytes, bytesOffset, length);
    }

    @Override
    public void write(byte[] bytes, int bytesOffset, int length, long storageIndex) throws IOException {
        randomAccessFile.seek(storageIndex);
        randomAccessFile.write(bytes, bytesOffset, length);
    }

    /**
     * Closes the backing {@link RandomAccessFile}.
     * 
     * @throws IOException
     *             if an I/O Error occurs
     */
    public void close() throws IOException {
        randomAccessFile.close();
    }
}

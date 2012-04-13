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
public class RandomAccessStorageModule implements IStorageModule {

    /**
     * The size of the medium in blocks.
     * 
     * @see #VIRTUAL_BLOCK_SIZE
     */
    protected long sizeInBlocks;

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
        this.sizeInBlocks = sizeInBlocks;
        this.randomAccessFile = randomAccessFile;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void read(byte[] bytes, int bytesOffset, int length, long storageIndex) throws IOException {
        randomAccessFile.seek(storageIndex);
        randomAccessFile.read(bytes, bytesOffset, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void write(byte[] bytes, int bytesOffset, int length, long storageIndex) throws IOException {
        randomAccessFile.seek(storageIndex);
        randomAccessFile.write(bytes, bytesOffset, length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final long getSizeInBlocks() {
        return sizeInBlocks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int checkBounds(final long logicalBlockAddress, final int transferLengthInBlocks) {
        if (logicalBlockAddress < 0 || logicalBlockAddress >= sizeInBlocks)
            return 1;
        if (transferLengthInBlocks < 0 || logicalBlockAddress + transferLengthInBlocks > sizeInBlocks)
            return 2;
        return 0;
    }

    /**
     * Closes the backing {@link RandomAccessFile}.
     * 
     * @throws IOException
     *             if an I/O Error occurs
     */
    public final void close() throws IOException {
        randomAccessFile.close();
    }
}

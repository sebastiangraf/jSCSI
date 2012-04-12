package org.jscsi.target.storage;

import java.io.File;
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
     * The mode {@link String} parameter used during the instantiation of {@link #randomAccessFile}.
     * <p>
     * This will create a {@link RandomAccessFile} with both read and write privileges that will immediately
     * save all written data in the file.
     */
    private static final String MODE = "rwd";

    /**
     * The {@link RandomAccessFile} used for accessing the storage medium.
     * 
     * @see #MODE
     */
    private RandomAccessFile randomAccessFile;

    /**
     * This is the build method for creating instances of {@link RandomAccessStorageModule}. If there is no
     * file to be found at the
     * specified <code>filePath</code>, then a {@link FileNotFoundException} will be thrown.
     * 
     * @param file
     *            a path leading to the file serving as storage medium
     * @return a new instance of {@link RandomAccessStorageModule}
     * @throws FileNotFoundException
     *             if the specified file does not exist
     */
    public static synchronized final SynchronizedRandomAccessStorageModule open(final File file)
        throws FileNotFoundException {
        final long sizeInBlocks = file.length() / VIRTUAL_BLOCK_SIZE;
        final RandomAccessFile randomAccessFile = new RandomAccessFile(file, MODE);// throws exc. if
                                                                                   // !file.exists()
        return new SynchronizedRandomAccessStorageModule(sizeInBlocks, randomAccessFile);
    }

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

    @Override
    public String getHumanFriendlyMediumSize() {
        long size;
        try {
            size = randomAccessFile.length();
        } catch (IOException e) {
            return "unable to access the file";
        }
        return toHumanFriendlySize(size);
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

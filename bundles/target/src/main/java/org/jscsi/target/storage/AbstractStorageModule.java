package org.jscsi.target.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

import org.jscsi.target.scsi.cdb.CommandDescriptorBlock;

/**
 * This is an abstract super class offering methods for storage and retrieval of
 * data, as well as emulating some properties of block storage devices.
 * <p>
 * All index and length parameters used by the read and write methods are referring to bytes, unlike the
 * values sent in {@link CommandDescriptorBlock} s, which are based on the value reported by
 * {@link #getBlockSizeInBytes()}.
 * 
 * @author Andreas Ergenzinger
 */
public abstract class AbstractStorageModule {

    /**
     * A fictitious block size.
     */
    public static final int VIRTUAL_BLOCK_SIZE = 512;

    /**
     * The size of the medium in blocks.
     * 
     * @see #VIRTUAL_BLOCK_SIZE
     */
    protected long sizeInBlocks;

    /**
     * The abstract constructor that makes sure that the {@link #sizeInBlocks} variable is initialized.
     * 
     * @param sizeInBlocks
     *            the size of the medium in blocks
     */
    protected AbstractStorageModule(final long sizeInBlocks) {
        this.sizeInBlocks = sizeInBlocks;
    }

    /**
     * The mode {@link String} parameter used during the instantiation of {@link #randomAccessFile}.
     * <p>
     * This will create a {@link RandomAccessFile} with both read and write privileges that will immediately
     * save all written data in the file.
     */
    private static final String MODE = "rwd";

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
    public static synchronized final IStorageModule open(final File file) throws FileNotFoundException {
        final long sizeInBlocks = file.length() / VIRTUAL_BLOCK_SIZE;
        final RandomAccessFile randomAccessFile = new RandomAccessFile(file, MODE);// throws exc. if
                                                                                   // !file.exists()

        return new SynchronizedRandomAccessStorageModule(sizeInBlocks, randomAccessFile);
    }

}

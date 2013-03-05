package org.jscsi.target.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.channels.FileChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(RandomAccessStorageModule.class);

    /**
     * The mode {@link String} parameter used during the instantiation of {@link #randomAccessFile}.
     * <p>
     * This will create a {@link RandomAccessFile} with both read and write privileges that will immediately
     * save all written data in the file.
     */
    private static final String MODE = "rwd";

    /**
     * The size of the medium in blocks.
     * 
     * @see #VIRTUAL_BLOCK_SIZE
     */
    protected final long sizeInBlocks;

    /**
     * The {@link RandomAccessFile} used for accessing the storage medium.
     * 
     * @see #MODE
     */
    private final RandomAccessFile randomAccessFile;

    /**
     * Creates a new {@link RandomAccessStorageModule} backed by the specified
     * file. If no such file exists, a {@link FileNotFoundException} will be
     * thrown.
     * 
     * @param sizeInBlocks
     *            blocksize for this module
     * @param randomAccessFile
     *            the path to the file serving as storage medium
     * 
     * @throws FileNotFoundException
     *             if the specified file does not exist
     */
    public RandomAccessStorageModule(final long sizeInBlocks, final File file) throws FileNotFoundException {
        this.sizeInBlocks = sizeInBlocks;
        this.randomAccessFile = new RandomAccessFile(file, MODE);
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

    /**
     * This is the build method for creating instances of {@link RandomAccessStorageModule}. If there is no
     * file to be found at the
     * specified <code>filePath</code>, then a {@link FileNotFoundException} will be thrown.
     * 
     * @param file
     *            a path leading to the file serving as storage medium
     * @param storageLength
     *            length of storage (if not already existing)
     * @param create
     *            should the storage be created
     * @return a new instance of {@link RandomAccessStorageModule}
     * @throws IOException
     */
    public static synchronized final IStorageModule open(final File file, final long storageLength,
        final boolean create, Class<? extends IStorageModule> kind) throws IOException {
        long sizeInBlocks;
        sizeInBlocks = storageLength / VIRTUAL_BLOCK_SIZE;
        if (create && !(kind.equals(JCloudsStorageModule.class))) {
            createStorageVolume(file, storageLength);
        }
        // throws exc. if !file.exists()
        @SuppressWarnings("unchecked")
        Constructor<? extends IStorageModule> cons =
            (Constructor<? extends IStorageModule>)kind.getConstructors()[0];
        try {
            IStorageModule mod = cons.newInstance(sizeInBlocks, file);
            return mod;
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException exc) {
            throw new IOException(exc);
        }
    }

    /**
     * Creating a new file if not existing at the path defined in the config.
     * Note that it is advised to create the file beforehand.
     * 
     * @param pConf
     *            configuration to be updated
     * @return true if creation successful, false if file already exists.
     * @throws IOException
     *             if anything weird happens
     */
    private static synchronized boolean createStorageVolume(final File pToCreate, final long pLength)
        throws IOException {
        FileOutputStream outStream = null;
        try {
            // if file exists, remove it after questioning.
            if (pToCreate.exists()) {
                if (!pToCreate.delete()) {
                    LOGGER.debug("Removal of old storage " + pToCreate.toString() + " unsucessful.");
                    return false;
                }
                LOGGER.debug("Removal of old storage " + pToCreate.toString() + " sucessful.");
            }

            // create file
            final File parent = pToCreate.getCanonicalFile().getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new FileNotFoundException("Unable to create directory: " + parent.getAbsolutePath());
            }

            pToCreate.createNewFile();
            outStream = new FileOutputStream(pToCreate);
            final FileChannel fcout = outStream.getChannel();
            fcout.position(pLength);
            outStream.write(26); // Write EOF (not normally needed)
            fcout.force(true);
            LOGGER.debug("Creation of storage " + pToCreate.toString() + " sucessful.");
            return true;
        } catch (IOException e) {
            LOGGER.error("Exception creating storage volume " + pToCreate.getAbsolutePath() + ": "
                + e.getMessage(), e);
            throw e;
        } finally {
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    LOGGER.error("Exception closing storage volume: " + e.getMessage(), e);
                }
            }
        }

    }

    /**
     * Deleting a storage recursive. Used for deleting a databases
     * 
     * @param pFile
     *            which should be deleted included descendants
     * @return true if delete is valid
     */
    public static boolean recursiveDelete(final File pFile) {
        if (pFile.isDirectory()) {
            for (final File child : pFile.listFiles()) {
                if (!recursiveDelete(child)) {
                    return false;
                }
            }
        }
        return pFile.delete();
    }

}

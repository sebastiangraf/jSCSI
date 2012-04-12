package org.jscsi.target.util;

/**
 * Instances of this class can serve as a source for byte arrays of specified
 * lengths. Up to {@link #capacity} arrays of different lengths can be stored,
 * quickly retrievable with the {@link #getArray(int)} method. Frequently
 * requested arrays will be returned faster, less frequently used arrays might
 * have to be initialized first and are more likely to be removed.
 * 
 * @author Andreas Ergenzinger
 */
public final class FastByteArrayProvider {

    /**
     * Contains the stored byte arrays.
     */
    final byte[][] arrays;

    /**
     * The maximum number of array that can be stored.
     */
    final int capacity;

    /**
     * The total number of stored arrays.
     */
    int size = 0;

    /**
     * A temporary byte array reference used by {@link #getArray(int)};
     */
    byte[] tmp;

    /**
     * The constructor.
     * 
     * @param capacity
     *            the {@link #capacity} of the created object.
     */
    public FastByteArrayProvider(final int capacity) {
        this.capacity = capacity;
        arrays = new byte[capacity][];
    }

    /**
     * Returns a byte array of the specified length.
     * <p>
     * Note that the returned array may have been used before and therefore the array's values are not
     * guaranteed to be <code>0</code>.
     * <p>
     * The method consecutively checks {@link #arrays} for an array of the correct length. If such an array
     * exists, it will be moved to one index position closer to the front of the array (if possible), speeding
     * up future retrievals of the same array.
     * 
     * @param length
     *            the length of the returned array
     * @return a byte array of the specified length
     */
    public byte[] getArray(final int length) {
        for (int i = 0; i < size; ++i) {
            if (length == arrays[i].length) {
                // swap (if not already at the front) and return
                if (i > 0) {
                    tmp = arrays[i];
                    arrays[i] = arrays[i - 1];
                    arrays[i - 1] = tmp;
                    return tmp;
                }
                // no swapping, so element was and still is at the front of the
                // list
                return arrays[0];
            }
        }
        // requested array does not exist, add to tail of queue,
        tmp = new byte[length];
        if (size == capacity)
            --size;// replace last element
        arrays[size] = tmp;
        ++size;
        return tmp;
    }

    /**
     * Returns all stored byte arrays.
     * <p>
     * This method's primary purpose is to enable testing of byte array storing and reordering.
     * 
     * @return {@link #arrays}.
     */
    public byte[][] getAll() {
        return arrays;
    }
}

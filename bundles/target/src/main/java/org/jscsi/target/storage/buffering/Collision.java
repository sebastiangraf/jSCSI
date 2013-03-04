package org.jscsi.target.storage.buffering;

/**
 * If a write task hasn't been performed yet
 * but the newest version has to be read,
 * collisions determine which bytes to overwrite and
 * send back.
 * 
 * @author Andreas Rain
 * 
 */
public class Collision {
    private final int mStart;
    private final int mEnd;
    private final byte[] mBytes;

    /**
     * 
     * 
     * @param pStart
     * @param pEnd
     * @param pBytes
     */
    public Collision(int pStart, int pEnd, byte[] pBytes) {
        super();
        this.mStart = pStart;
        this.mEnd = pEnd;
        this.mBytes = pBytes;
    }

    /**
     * Retrieve the start index for this collision.
     * 
     * @return int - start index
     */
    public int getStart() {
        return mStart;
    }

    /**
     * Retrieve the end index of this collision.
     * 
     * @return int - end index
     */
    public int getEnd() {
        return mEnd;
    }

    /**
     * The values for the colliding indizes.
     * 
     * @return byte[] - value
     */
    public byte[] getBytes() {
        return mBytes;
    }

}

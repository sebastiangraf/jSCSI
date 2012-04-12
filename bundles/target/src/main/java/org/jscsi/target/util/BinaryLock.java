package org.jscsi.target.util;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Instances of {@link BinaryLock} can be used to prevent concurrent access to
 * the same resource, so, in essence, this is a very simplified {@link Lock} implementation, however lacking
 * many advanced capabilities.
 * <p>
 * A {@link BinaryLock} knows only two states, locked and unlocked. Attempts by the lock-holder to lock a
 * {@link BinaryLock} when locked or to unlock it when unlocked, will have no effect.
 * 
 * @author Andreas Ergenzinger
 */
public class BinaryLock {

    /**
     * The {@link ReentrantLock} which backs up the {@link BinaryLock} and takes
     * care of suspending and notifying waiting {@link Threads}.
     */
    private final ReentrantLock lock = new ReentrantLock();

    /**
     * This method is used to acquire the lock. It will block until no other {@link Thread} is holding the
     * lock and then return <code>true</code> to
     * indicate the successful lock acquisition, or return <code>false</code>,
     * if the calling {@link Thread} was interrupted while waiting for the lock.
     * <p>
     * If the caller is already holding the lock, the method will immediately return <code>true</code> without
     * any changes.
     * 
     * @return <code>true</code> if and only if the lock has been acquired
     */
    public boolean lock() {
        try {
            lock.lockInterruptibly();
            return true;
        } catch (InterruptedException e) {
            return false;
        }
    }

    /**
     * Releases the lock when called by the current lock holder;
     */
    public void unlock() {
        if (lock.isHeldByCurrentThread()) {
            while (lock.getHoldCount() > 0)
                lock.unlock();
        }
    }

}

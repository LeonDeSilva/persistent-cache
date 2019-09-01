package com.leondesilva.persistentcache.lock;

/**
 * Interface for read write lock provider
 *
 */
public interface ReadWriteLockProvider {
    /**
     * Acquire read lock
     *
     */
    public void acquireReadLock();

    /**
     * Release read lock
     *
     */
    public void releaseReadLock();

    /**
     * Acquire write lock
     *
     */
    public void acquireWriteLock();

    /**
     * Release write lock
     *
     */
    public void releaseWriteLock();

    /**
     * Method to check if write lock has been acquired by current thread
     * @return Whether Write Lock id acquired by current thread
     */
    public boolean isWriteLockAcquiredByCurrentThread();
}

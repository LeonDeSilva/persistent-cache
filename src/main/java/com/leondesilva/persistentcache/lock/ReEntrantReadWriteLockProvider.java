package com.leondesilva.persistentcache.lock;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Implementation of ReadWriteLock Provider interface
 */
public class ReEntrantReadWriteLockProvider implements ReadWriteLockProvider{
    private ReentrantReadWriteLock rwlock;
    private Lock readLock, writeLock;

    /**
     * Creates an instance of the ReEntrantReadWriteLockProvider
     *
     */
    public ReEntrantReadWriteLockProvider() {
        this.rwlock = new ReentrantReadWriteLock();
        readLock = rwlock.readLock();
        writeLock = rwlock.writeLock();
    }

    /**
     * Acquire read lock
     *
     */
    @Override
    public void acquireReadLock() {
        readLock.lock();
    }

    /**
     * Release read lock
     *
     */
    @Override
    public void releaseReadLock() {
        readLock.unlock();
    }

    /**
     * Acquire write lock
     *
     */
    @Override
    public void acquireWriteLock() {
        writeLock.lock();
    }

    /**
     * Release write lock
     *
     */
    @Override
    public void releaseWriteLock() {
        writeLock.unlock();
    }

    /**
     * Method to check if write lock has been acquired by current thread
     * @return Whether Write Lock id acquired by current thread
     */
    @Override
    public boolean isWriteLockAcquiredByCurrentThread() {
        return rwlock.isWriteLockedByCurrentThread();
    }
}

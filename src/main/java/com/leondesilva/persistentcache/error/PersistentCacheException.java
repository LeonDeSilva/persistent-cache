package com.leondesilva.persistentcache.error;

/**
 * Class for persistent cache related exceptions
 *
 */
public class PersistentCacheException extends Exception {

    /**
     * Constructor to instantiate PersistentCacheException
     *
     */
    public PersistentCacheException() {
        super();
    }

    /**
     * Constructor to instantiate PersistentCacheException with error message
     *
     * @param message error message
     */
    public PersistentCacheException(String message) {
        super(message);
    }

    /**
     * Constructor to instantiate PersistentCacheException with error message and cause
     *
     * @param message error message
     * @param cause cause of the error
     */
    public PersistentCacheException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor to instantiate PersistentCacheException with cause
     *
     * @param cause cause of the error
     */
    public PersistentCacheException(Throwable cause) {
        super(cause);
    }
}

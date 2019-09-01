package com.leondesilva.persistentcache.cache;

import com.leondesilva.persistentcache.cache.loaders.CacheLoader;
import com.leondesilva.persistentcache.error.PersistentCacheException;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;

/**
 * Interface for the persistent cache factory.
 */
public class PersistentCacheFactory {

    /**
     * Private constructor
     *
     */
    private PersistentCacheFactory() {
    }

    /**
     * Creates an instance of the PersistentCache
     *
     * @param dbName         database name of the persistent cache DB
     * @param dbFilePath     database file path of the persistent cache DB
     * @param maxLogFileSize database file size (of a single file) of the persistent cache DB
     * @param <K>            serializable type of object for key
     * @param <V>            serializable type of object for value
     *
     * @return               persistent cache
     */
    public static <K extends Serializable, V extends Serializable> PersistentCache<K, V> createCache(String dbName, String dbFilePath, long maxLogFileSize) throws PersistentCacheException {
        return new BdbPersistentCacheImpl<>(dbName, dbFilePath, maxLogFileSize);
    }

    /**
     * Creates an instance of the PersistentCache with TTL
     *
     * @param dbName              database name of the persistent cache DB
     * @param dbFilePath          database file path of the persistent cache DB
     * @param maxLogFileSize      database file size (of a single file) of the persistent cache DB
     * @param cacheExpiryTime     cache expiry time
     * @param cacheExpiryTimeUnit cache expiry time unit
     * @param <K>                 serializable type of object for key
     * @param <V>                 serializable type of object for value
     *
     * @return                    persistent cache with TTL
     */
    public static <K extends Serializable, V extends Serializable> PersistentCache<K, V> createTTLCache(String dbName, String dbFilePath, long maxLogFileSize, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) throws PersistentCacheException {
        return new BdbPersistentTTLCacheImpl<>(dbName, dbFilePath, maxLogFileSize, cacheExpiryTime, cacheExpiryTimeUnit);
    }

    /**
     * Creates an instance of the PersistentCache with per row TTL
     *
     * @param dbName         database name of the persistent cache DB
     * @param dbFilePath     database file path of the persistent cache DB
     * @param maxLogFileSize database file size (of a single file) of the persistent cache DB
     * @param <K>            serializable type of object for key
     * @param <V>            serializable type of object for value
     *
     * @return              persistent cache with per row TTL
     */
    public static <K extends Serializable, V extends Serializable> PersistentCache<K, V> createPerRowTTLCache(String dbName, String dbFilePath, long maxLogFileSize) throws PersistentCacheException {
        return new BdbPersistentPerRowTTLCacheImpl<>(dbName, dbFilePath, maxLogFileSize);
    }

    /**
     * Creates an instance of the Persistent loading cache with TTL
     *
     * @param dbName              database name of the persistent cache DB
     * @param dbFilePath          database file path of the persistent cache DB
     * @param maxLogFileSize      database file size (of a single file) of the persistent cache DB
     * @param cacheExpiryTime     cache expiry time
     * @param cacheExpiryTimeUnit cache expiry time unit
     * @param cacheLoader         cache loader to get value for keys when cache is expired
     * @param <K>                 serializable type of object for key
     * @param <V>                 serializable type of object for value
     * @return                    persistent loading cache with TTL
     */
    public static <K extends Serializable, V extends Serializable> PersistentCache<K, V> createLoadingCache(String dbName, String dbFilePath, long maxLogFileSize, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit, CacheLoader<K, V> cacheLoader) throws PersistentCacheException {
        return new BdbPersistentLoadingCacheImpl<>(dbName, dbFilePath, maxLogFileSize, cacheExpiryTime, cacheExpiryTimeUnit, cacheLoader);
    }
}

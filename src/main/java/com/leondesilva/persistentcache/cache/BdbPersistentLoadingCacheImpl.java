package com.leondesilva.persistentcache.cache;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Transaction;
import com.leondesilva.persistentcache.cache.loaders.CacheLoader;
import com.leondesilva.persistentcache.cache.model.pojo.CacheObject;
import com.leondesilva.persistentcache.error.PersistentCacheException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of the PersistentCache for Berkeley db operations.
 *
 */
public class BdbPersistentLoadingCacheImpl<K extends Serializable, V extends Serializable> extends BaseBdbPersistentCache<K,V> {
    private long cacheExpiryTime;
    private ChronoUnit cacheExpiryTimeUnit;
    private CacheLoader<K, V> cacheLoader;

    /**
     * Constructor to instantiate a BdbCacheImpl
     *
     * @param dbName         Database name.
     * @param dbFilePath     Path to store/open database.
     * @param maxLogFileSize Max log file size in bytes. Minimum allowed is 1,000,000 bytes (1MB).
     * @param cacheExpiryTime Cache expiry time.
     * @param cacheExpiryTimeUnit Cache expiry time unit.
     * @throws PersistentCacheException If an error occurs while creating the persistent cache.
     */
    public BdbPersistentLoadingCacheImpl(String dbName, String dbFilePath, long maxLogFileSize, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit, CacheLoader<K,V> cacheLoader) throws PersistentCacheException {
        super(dbName, dbFilePath, maxLogFileSize);
        this.cacheExpiryTime = cacheExpiryTime;
        this.cacheExpiryTimeUnit = cacheExpiryTimeUnit;
        this.cacheLoader = cacheLoader;
    }

    /**
     * Method to store a given key and a value. ( NOT SUPPORTED !!! )
     *
     * @param key   the key to store
     * @param value the value to store
     * @return true if success, false if the operation is failed
     * @throws PersistentCacheException if an error occurs while storing data.
     */
    @Override
    public boolean put(K key, V value) {
        throw new UnsupportedOperationException(PUT_IS_NOT_SUPPORTED_IN_LOADING_CACHE_ERROR_MSG);
    }

    /**
     * Method to store a given key and a value with cache expiry time. ( NOT SUPPORTED !!! )
     *
     * @param key                 the key to store
     * @param value               the value to store
     * @param cacheExpiryTime     the cache expiry time
     * @param cacheExpiryTimeUnit the cache expiry time unit
     * @return true if success, false if the operation is failed
     * @throws PersistentCacheException if an error occurs while storing data.
     */
    @Override
    public boolean put(K key, V value, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) {
        throw new UnsupportedOperationException(PUT_IS_NOT_SUPPORTED_IN_LOADING_CACHE_ERROR_MSG);
    }

    /**
     * Method to store a given key and a value if the key is absent.
     *
     * @param key   the key to store
     * @param value the value to store
     * @return true if success, false if the operation is failed or key already exists
     * @throws PersistentCacheException if an error occurs while storing data.
     */
    @Override
    public boolean putIfAbsent(K key, V value) {
        throw new UnsupportedOperationException(PUT_IS_NOT_SUPPORTED_IN_LOADING_CACHE_ERROR_MSG);
    }

    /**
     * Method to store a given key and a value with cache expiry time, if the key is absent.
     *
     * @param key                 the key to store
     * @param value               the value to store
     * @param cacheExpiryTime     the cache expiry time
     * @param cacheExpiryTimeUnit the cache expiry time unit
     * @return true if success, false if the operation is failed
     * @throws PersistentCacheException if an error occurs while storing data.
     */
    @Override
    public boolean putIfAbsent(K key, V value, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) {
        throw new UnsupportedOperationException(PUT_IS_NOT_SUPPORTED_IN_LOADING_CACHE_ERROR_MSG);
    }

    @Override
    public boolean putAll(Map<K, V> data) {
        throw new UnsupportedOperationException(PUT_IS_NOT_SUPPORTED_IN_LOADING_CACHE_ERROR_MSG);
    }

    @Override
    protected boolean processAndStoreData(Transaction transaction, K key, V value, boolean overwrite) {
        CacheObject<V> cacheObject = new CacheObject<>();
        cacheObject.setValueObject(value);
        cacheObject.setCachedDatetime(generateCacheExpiryDateTime(this.cacheExpiryTime, this.cacheExpiryTimeUnit));

        return storeData(transaction, key, cacheObject, overwrite);
    }

    @Override
    protected boolean processAndStoreDataWithCacheExpiryTime(K key, V value, boolean overwrite, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) {
        throw new UnsupportedOperationException(PER_ROW_CACHE_EXPIRY_NOT_SUPPORTED_ERROR_MSG);
    }

    @Override
    public boolean containsKey(K key) throws PersistentCacheException {
        throw new UnsupportedOperationException(CONTAINS_KEY_IS_NOT_SUPPROTED_IN_LOADING_CACHE_ERROR_MSG);
    }

    @Override
    protected V processAndGetData(K key) {
        CacheObject<V> cacheObject = getData(key);

        if (cacheObject == null) {
            return loadAndStoreToCache(key);
        }

        LocalDateTime cachedDateTime = cacheObject.getCachedDatetime();

        if (cachedDateTime != null && isCacheObjectOutDated(cachedDateTime)) {
            deleteRecord(key);
            return loadAndStoreToCache(key);
        }

        return cacheObject.getValueObject();
    }

    @Override
    protected Map<K, V> generateMapOfRecordsFromDatabaseEntries(Map<DatabaseEntry, DatabaseEntry> databaseEntryMap) {
        Map<K, V> records = new LinkedHashMap<>();

        for (Map.Entry<DatabaseEntry, DatabaseEntry> keyValue : databaseEntryMap.entrySet()) {
            K key = deserialize(keyValue.getKey().getData());
            V value;
            CacheObject<V> cacheObject = deserialize(keyValue.getValue().getData());

            if (cacheObject == null) {
                continue;
            }

            LocalDateTime cachedDateTime = cacheObject.getCachedDatetime();

            if (cachedDateTime != null && isCacheObjectOutDated(cachedDateTime)) {
                deleteRecord(key); // TODO : Check !!!!! can also be overwritten without delete.
                value = cacheLoader.load(key);
                processAndStoreData(null, key, value, true);
            } else {
                value = cacheObject.getValueObject();
            }

            if (key != null && value != null) {
                records.put(key, value);
            }
        }

        return records;
    }

    private V loadAndStoreToCache(K key) {
        V value = cacheLoader.load(key);

        if (value != null) {
            processAndStoreData(null, key, value, true);
        }

        return value;
    }
}

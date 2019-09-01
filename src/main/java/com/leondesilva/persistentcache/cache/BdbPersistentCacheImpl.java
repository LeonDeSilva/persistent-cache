package com.leondesilva.persistentcache.cache;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Transaction;
import com.leondesilva.persistentcache.error.PersistentCacheException;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of the PersistentCache for Berkeley db operations.
 *
 */
public class BdbPersistentCacheImpl<K extends Serializable, V extends Serializable> extends BaseBdbPersistentCache<K,V> {

    /**
     * Constructor to instantiate a BdbCacheImpl
     *
     * @param dbName         Database name.
     * @param dbFilePath     Path to store/open database.
     * @param maxLogFileSize Max log file size in bytes. Minimum allowed is 1,000,000 bytes (1MB).
     * @throws PersistentCacheException If an error occurs while creating the persistent cache.
     */
    public BdbPersistentCacheImpl(String dbName, String dbFilePath, long maxLogFileSize) throws PersistentCacheException {
        super(dbName, dbFilePath, maxLogFileSize);
    }

    /**
     * Method to put store data with cache expiry time ( NOT SUPPORTED !!! )
     *
     * @param key                 the key to store
     * @param value               the value to store
     * @param cacheExpiryTime     the cache expiry time
     * @param cacheExpiryTimeUnit the cache expiry time unit
     * @return true if success and false if not.
     * @throws PersistentCacheException if an error occurs while storing data to the cache
     */
    @Override
    public boolean put(K key, V value, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) {
        throw new UnsupportedOperationException(PER_ROW_CACHE_EXPIRY_NOT_SUPPORTED_ERROR_MSG);
    }

    /**
     * Method to store key and value with a given cache expiry time if the key is absent. ( NOT SUPPORTED !!! )
     *
     * @param key                 the key to store
     * @param value               the value to store
     * @param cacheExpiryTime     the cache expiry time
     * @param cacheExpiryTimeUnit the cache expiry time unit
     * @return true if success and false if not
     * @throws PersistentCacheException if an error occurs while storing data
     */
    @Override
    public boolean putIfAbsent(K key, V value, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) {
        throw new UnsupportedOperationException(PER_ROW_CACHE_EXPIRY_NOT_SUPPORTED_ERROR_MSG);
    }

    /**
     * Method to process and store data using a transaction.
     *
     * @param transaction the transaction to be used when processing data
     * @param key         the key to store
     * @param value       the value to store
     * @param overwrite   overwrite if true and does not overwrite if false
     * @return true if success and false if not
     */
    @Override
    protected boolean processAndStoreData(Transaction transaction, K key, V value, boolean overwrite) {
        return storeData(transaction, key, value, overwrite);
    }

    /**
     * Method to process and store data  with cache expiry time. ( NOT SUPPORTED !!! )
     *
     * @param key                 the key to store
     * @param value               the value to store
     * @param overwrite           overwrite if true and does not overwrite if false
     * @param cacheExpiryTime     the cache expiry time
     * @param cacheExpiryTimeUnit the cache expiry time unit
     * @return true if success and false if not
     */
    @Override
    protected boolean processAndStoreDataWithCacheExpiryTime(K key, V value, boolean overwrite, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) {
        throw new UnsupportedOperationException(PER_ROW_CACHE_EXPIRY_NOT_SUPPORTED_ERROR_MSG);
    }

    /**
     * Method to process and get data.
     *
     * @param key the key to get value
     * @return the value
     */
    @Override
    protected V processAndGetData(K key) {
        return getData(key);
    }

    /**
     * Method to generate map of records from database entries.
     *
     * @param databaseEntryMap the database entry map
     * @return generated key value map
     */
    @Override
    protected Map<K, V> generateMapOfRecordsFromDatabaseEntries(Map<DatabaseEntry, DatabaseEntry> databaseEntryMap) {
        Map<K, V> records = new LinkedHashMap<>();

        for (Map.Entry<DatabaseEntry, DatabaseEntry> keyValue : databaseEntryMap.entrySet()) {
            K key = deserialize(keyValue.getKey().getData());
            V value = deserialize(keyValue.getValue().getData());

            if (key != null && value != null) {
                records.put(key, value);
            }
        }

        return records;
    }
}

package com.leondesilva.persistentcache.cache;

import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Transaction;
import com.leondesilva.persistentcache.cache.model.pojo.CacheObject;
import com.leondesilva.persistentcache.error.PersistentCacheException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the PersistentCache for Berkeley db operations.
 *
 */
public class BdbPersistentPerRowTTLCacheImpl<K extends Serializable, V extends Serializable> extends BaseBdbPersistentCache<K,V> {

    /**
     * Constructor to instantiate a BdbCacheImpl
     *
     * @param dbName         Database name.
     * @param dbFilePath     Path to store/open database.
     * @param maxLogFileSize Max log file size in bytes. Minimum allowed is 1,000,000 bytes (1MB).
     * @throws PersistentCacheException If an error occurs while creating the persistent cache.
     */
    public BdbPersistentPerRowTTLCacheImpl(String dbName, String dbFilePath, long maxLogFileSize) throws PersistentCacheException {
        super(dbName, dbFilePath, maxLogFileSize);
    }

    @Override
    protected boolean processAndStoreData(Transaction transaction, K key, V value, boolean overwrite) {
        CacheObject<V> cacheObject = new CacheObject<>();
        cacheObject.setValueObject(value);

        /*
         * Through a put method this will be invoked. But as there is no cache expiry time, cache date time is set to null.
         * While retrieving data, objects without cache date time will not expire.
         */
        cacheObject.setCachedDatetime(null);

        return storeData(transaction, key, cacheObject, overwrite);
    }

    @Override
    protected boolean processAndStoreDataWithCacheExpiryTime(K key, V value, boolean overwrite, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) {
        CacheObject<V> cacheObject = new CacheObject<>();
        cacheObject.setValueObject(value);
        cacheObject.setCachedDatetime(generateCacheExpiryDateTime(cacheExpiryTime, cacheExpiryTimeUnit));

        return storeData(null, key, cacheObject, overwrite);
    }

    @Override
    protected V processAndGetData(K key) {
        CacheObject<V> cacheObject = getData(key);

        if (cacheObject == null) {
            return null;
        }

        LocalDateTime cachedDateTime = cacheObject.getCachedDatetime();

        if (cachedDateTime != null && isCacheObjectOutDated(cachedDateTime)) {
            deleteRecord(key);
            return null;
        }

        return cacheObject.getValueObject();
    }

    @Override
    protected Map<K, V> generateMapOfRecordsFromDatabaseEntries(Map<DatabaseEntry, DatabaseEntry> databaseEntryMap) {
        Map<K, V> records = new LinkedHashMap<>();
        List<K> expiredKeys = new LinkedList<>();

        for (Map.Entry<DatabaseEntry, DatabaseEntry> keyValue : databaseEntryMap.entrySet()) {
            K key = deserialize(keyValue.getKey().getData());
            CacheObject<V> cacheObject = deserialize(keyValue.getValue().getData());

            if (cacheObject == null) {
                continue;
            }

            LocalDateTime cachedDateTime = cacheObject.getCachedDatetime();

            if (cachedDateTime != null && isCacheObjectOutDated(cachedDateTime)) {
                expiredKeys.add(key);

            } else {
                V value = cacheObject.getValueObject();

                if (key != null && value != null) {
                    records.put(key, value);
                }
            }
        }

        return records;
    }
}

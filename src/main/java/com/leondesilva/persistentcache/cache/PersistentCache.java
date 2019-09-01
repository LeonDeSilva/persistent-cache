package com.leondesilva.persistentcache.cache;

import com.leondesilva.persistentcache.error.PersistentCacheException;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * Interface for berkeley db operations.
 *
 */
public interface PersistentCache<K extends Serializable, V extends Serializable> {
    /**
     * Method to store a given key and a value.
     *
     * @param key   the key to store
     * @param value the value to store
     * @return true if success, false if the operation is failed
     * @throws PersistentCacheException if an error occurs while storing data.
     */
    public boolean put(K key, V value) throws PersistentCacheException;

    /**
     * Method to store a given key and a value with cache expiry time.
     *
     * @param key                 the key to store
     * @param value               the value to store
     * @param cacheExpiryTime     the cache expiry time
     * @param cacheExpiryTimeUnit the cache expiry time unit
     * @return true if success, false if the operation is failed
     * @throws PersistentCacheException if an error occurs while storing data.
     */
    public boolean put(K key, V value, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) throws PersistentCacheException;

    /**
     * Method to store a given key and a value if the key is absent.
     *
     * @param key   the key to store
     * @param value the value to store
     * @return true if success, false if the operation is failed or key already exists
     * @throws PersistentCacheException if an error occurs while storing data.
     */
    public boolean putIfAbsent(K key, V value) throws PersistentCacheException;

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
    public boolean putIfAbsent(K key, V value, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) throws PersistentCacheException;

    /**
     * Method to store a given map into the cache.
     *
     * @param data the data to store
     * @return true if success and false if the operation is failed
     * @throws PersistentCacheException if an error occurs while storing data.
     */
    public boolean putAll(Map<K, V> data) throws PersistentCacheException;

    /**
     * Method to get the value of a given key.
     *
     * @param key the key to retrieve the value
     * @return the value for the given key
     * @throws PersistentCacheException if an error occurs while retrieving data.
     */
    public V get(K key) throws PersistentCacheException;

    /**
     * Method to get the value of a given key and delete after retrieving.
     *
     * @param key                         the key to retrieve the value
     * @param deleteRecordAfterRetrieving if true deletes the record after retrieving the value
     * @return the value for the given key
     * @throws PersistentCacheException if an error occurs while retrieving data.
     */
    public V get(K key, boolean deleteRecordAfterRetrieving) throws PersistentCacheException;

    /**
     * Method to get all records.
     *
     * @return all the records as a map
     * @throws PersistentCacheException if an error occurs while retrieving data.
     */
    public Map<K, V> getAll() throws PersistentCacheException;

    /**
     * Method to check whether the cache contains the key.
     *
     * @param key the key to check
     * @return true if available, false if not
     * @throws PersistentCacheException if an error occurs while checking for the key.
     */
    public boolean containsKey(K key) throws PersistentCacheException;

    /**
     * Method to delete the record for a given key.
     *
     * @param key the key to delete record
     * @return true if the success, false if the operation is failed
     * @throws PersistentCacheException if an error occurs while deleting data.
     */
    public boolean delete(K key) throws PersistentCacheException;

    /**
     * Method to delete a list of given keys.
     *
     * @param keys List of keys to delete.
     * @return true if the success, false if the operation is failed
     * @throws PersistentCacheException if the cache is in closed state.
     */
    public boolean delete(List<K> keys) throws PersistentCacheException;

    /**
     * Method to delete all records.
     *
     * @return true if the success, false if the operation is failed
     */
    public boolean deleteAll() throws PersistentCacheException;

    /**
     * Method to truncate.
     *
     * @return true if the success, false if the operation is failed
     */
    public boolean truncate() throws PersistentCacheException;

    /**
     * Method to close the cache.
     *
     * @throws PersistentCacheException if an error occurs while closing the cache
     */
    public void close() throws PersistentCacheException;
}

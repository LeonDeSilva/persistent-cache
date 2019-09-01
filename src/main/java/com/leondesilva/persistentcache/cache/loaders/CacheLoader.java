package com.leondesilva.persistentcache.cache.loaders;

import java.io.Serializable;

/**
 * Interface to represent Cache Loader.
 *
 */
@FunctionalInterface
public interface CacheLoader<K extends Serializable, V extends Serializable> {

    /**
     * Method to load cache.
     *
     * @param key the key to load
     * @return value for the given key
     */
    public V load(K key);
}

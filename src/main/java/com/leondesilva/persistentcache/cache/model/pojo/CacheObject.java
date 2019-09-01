package com.leondesilva.persistentcache.cache.model.pojo;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Class to represent a cache object.
 *
 */
public class CacheObject <T extends Serializable> implements Serializable {
    private T valueObject;
    private LocalDateTime cachedDatetime;

    /**
     * Method to get the value object.
     *
     * @return the value object
     */
    public T getValueObject() {
        return valueObject;
    }

    /**
     * Method to set the value object.
     *
     * @param valueObject the value object
     */
    public void setValueObject(T valueObject) {
        this.valueObject = valueObject;
    }

    /**
     * Method to get the cached date time.
     *
     * @return cached date time
     */
    public LocalDateTime getCachedDatetime() {
        return cachedDatetime;
    }

    /**
     * Method to set the cached date time.
     *
     * @param cachedDatetime cached date time
     */
    public void setCachedDatetime(LocalDateTime cachedDatetime) {
        this.cachedDatetime = cachedDatetime;
    }
}

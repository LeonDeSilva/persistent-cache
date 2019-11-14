# Persistent Cache

[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](https://github.com/LeonDeSilva/persistent-cache/blob/master/LICENSE)
[![Build Status](https://travis-ci.org/LeonDeSilva/persistent-cache.svg?branch=master)](https://travis-ci.org/LeonDeSilva/persistent-cache)


**A Persistent Cache With TTL and Loading Cache Features**


The existing solutions for persistent caches have their own limitations. 
Some require XML configurations and they does not support any other time units other than seconds, which can be difficult when going for smaller and larger time units. 
Some caches are embedded databases which can be used as a persistent cache, but does not have TTL cache and loading cache feature.

This library was built to overcome these challenges and is built by using a Berkeley DB as the base. 

The list of features of the persistent cache can be listed out as below.

- Any type of object can be used for key or value without depending on the library specific wrapper objects.

- The library does not require external configuration files.

- A common TTL or a per-row TTL can be specified for the cache.

- Loading cache features with TTL is supported.

- TTL can be specified as to any timeunit using Java 8’s ChronoUnit.

- Supports batch store of data and put if absent like features.


Types of Caches in detail

**Basic Persistent Cache**
	
This is the most basic type of persistent cache. There are no cache loaders and time to live features. Simply the data has to be inserted manually by put methods. Data in the cache will not be expired. Removing data from the cache should also be done using delete methods. When creating the cache, it has to be done using the PersistentCacheFactory, by calling the createCache method. The parameters to be passed are the cache name, file path and the max log file size. The log file size is the size of the single Berkeley DB log file. This should be specified using bytes. The minimum log file size allowed is 5000000 bytes, which means 5MB file chunks will be created in the given file path.


**Persistent Cache with TTL**

This cache allows the user to specify a cache expiration time while providing persistency. There are no cache loaders. Inserting data into the cache should be done manually by put methods. But the data will be deleted automatically when it reaches the cache expiry time. This type of cache should be created using a the PersistentCacheFactory by calling the createTTLCache method. The parameters to be passed are the cache name, file path, max log file size, cache expiry time and the cache expiry time unit. 


**Persistent Cache with per-row TTL**

This persistent cache supports per-row TTL. This means per each key value a cache expiry time can be specified. There are no loading cache features supported in this cache. Per-row TTL cache can be created using the PersistentCacheFactory by calling the createPerRowTTLCache method. Cache name, file path and the max log file size are the parameters that needs to be passed when creating the cache. The difference in this cache is that, when inserting data, it has to be done using the put method by passing additional parameters such as the cache expiry time and the time unit. 


**Persistent Loading Cache**

Persistent Loading Caching supports cache loader and TTL features. When the data in the cache is not available or expired, the data will be loaded from the cache loader to the cache. This cache should be created using the PersistentCacheFactory by calling the createLoadingCache method. Cache name, file path, max log file size, cache expiry time, cache expiry time unit and cache loader are the parameters that needs to be passed when creating the cache. The CacheLoader interface has a load method which needs to be implemented. The user can specify custom cache loaders as needed.

**PersistentCacheFactory**

|**Modifier and Type** | **Method and Description**|
|----------------------|---------------------------|
| static <K extends java.io.Serializable,V extends java.io.Serializable>com.leondesilva.persistentcache.cache.PersistentCache<K,V> | **createCache** (java.lang.String dbName, java.lang.String dbFilePath, long maxLogFileSize)Creates an instance of the PersistentCache |
| static <K extends java.io.Serializable,V extends java.io.Serializable>com.leondesilva.persistentcache.cache.PersistentCache<K,V> | **createLoadingCache** (java.lang.String dbName, java.lang.String dbFilePath, long maxLogFileSize, long cacheExpiryTime, java.time.temporal.ChronoUnit cacheExpiryTimeUnit, com.leondesilva.persistentcache.cache.loaders.CacheLoader<K,V> cacheLoader)Creates an instance of the Persistent loading cache with TTL |
| static <K extends java.io.Serializable,V extends java.io.Serializable>com.leondesilva.persistentcache.cache.PersistentCache<K,V> | **createPerRowTTLCache** (java.lang.String dbName, java.lang.String dbFilePath, long maxLogFileSize)Creates an instance of the PersistentCache with per row TTL |
| static <K extends java.io.Serializable,V extends java.io.Serializable>com.leondesilva.persistentcache.cache.PersistentCache<K,V> | **createTTLCache** (java.lang.String dbName, java.lang.String dbFilePath, long maxLogFileSize, long cacheExpiryTime, java.time.temporal.ChronoUnit cacheExpiryTimeUnit)Creates an instance of the PersistentCache with TTL |


**Persistent Cache Interface**

| **Modifier and Type** | **Method and Description** |
| ----------------------|----------------------------|
| void | **close** ()Method to close the cache. |
| boolean | **containsKey** ( **K** key)Method to check whether the cache contains the key. |
| boolean | **delete** ( **K** key)Method to delete the record for a given key. |
| boolean | **delete** (java.util.List< **K** > keys)Method to delete a list of given keys. |
| boolean | **deleteAll** ()Method to delete all records. |
| **V** | **get** ( **K** key)Method to get the value of a given key. |
| **V** | **get** ( **K** key, boolean deleteRecordAfterRetrieving)Method to get the value of a given key and delete after retrieving. |
| java.util.Map< **K** , **V** > | **getAll** ()Method to get all records. |
| boolean | **put** ( **K** key, **V** value)Method to store a given key and a value. |
| boolean | **put** ( **K** key, **V** value, long cacheExpiryTime, java.time.temporal.ChronoUnit cacheExpiryTimeUnit)Method to store a given key and a value with cache expiry time. |
| boolean | **putAll** (java.util.Map< **K** , **V** > data)Method to store a given map into the cache. |
| boolean | **putIfAbsent** ( **K** key, **V** value)Method to store a given key and a value if the key is absent. |
| boolean | **putIfAbsent** ( **K** key, **V** value, long cacheExpiryTime, java.time.temporal.ChronoUnit cacheExpiryTimeUnit)Method to store a given key and a value with cache expiry time, if the key is absent. |
| boolean | **truncate** ()Method to truncate. |


**CacheLoader Interface**

| **Modifier and Type** | **Method and Description** |
|-----------------------|----------------------------|
| **V** | **load** ( **K** key)Method to load cache. |



This library can be used whenever a caching mechanism is needed with state saving. 
The library was built according to a research and development done for a production running application of a Recommendation Generation Engine. 
This has simplified the persistent cache generation and is built with important features packed into one single library.

Where can I get the latest release?
-----------------------------------
You can download source and binaries from the [releases page](https://github.com/LeonDeSilva/persistent-cache/releases).

You can also pull it from the central Maven repositories:

```xml
<dependency>
  <groupId>com.leondesilva.persistentcache</groupId>
  <artifactId>persistent-cache</artifactId>
  <version>1.0.0</version>
</dependency>
```

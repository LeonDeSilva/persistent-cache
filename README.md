# Persistent Cache


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

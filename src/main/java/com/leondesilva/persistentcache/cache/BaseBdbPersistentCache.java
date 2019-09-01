package com.leondesilva.persistentcache.cache;

import com.sleepycat.je.*;
import com.leondesilva.persistentcache.error.PersistentCacheException;
import com.leondesilva.persistentcache.lock.ReEntrantReadWriteLockProvider;
import com.leondesilva.persistentcache.lock.ReadWriteLockProvider;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of the PersistentCache for Berkeley db operations.
 */
public abstract class BaseBdbPersistentCache<K extends Serializable, V extends Serializable> implements PersistentCache<K, V> {
    protected Environment dbEnvironment;
    protected Database database;
    private AtomicBoolean isOpen = new AtomicBoolean(false);

    private String dbName;
    private String dbFilePath;
    private long maxLogFileSize;
    private ReadWriteLockProvider lockProvider;

    private static Logger LOGGER = LoggerFactory.getLogger(BaseBdbPersistentCache.class.getName());

    protected static final String PUT_IS_NOT_SUPPORTED_IN_LOADING_CACHE_ERROR_MSG = "Put is not supported in loading cache.";
    protected static final String PER_ROW_CACHE_EXPIRY_NOT_SUPPORTED_ERROR_MSG = "Per row TTL is not supported in non-TTL persistent cache.";
    protected static final String CONTAINS_KEY_IS_NOT_SUPPROTED_IN_LOADING_CACHE_ERROR_MSG = "ContainesKey method is not supported in loading cache.";
    private static final String BDB_WRITE_ERROR_MSG = "BDB Error occurred while writing to the database.";
    private static final String BDB_READ_ERROR_MSG = "BDB Error occurred while retrieving data from the database.";
    private static final String BDB_DELETE_ERROR_MSG = "BDB Error occurred while deleting data from the database.";
    private static final String BDB_CLOSE_ERROR_MSG = "BDB Error occurred while closing the database.";
    private static final String BDB_ENTRY_CREATION_ERROR = "BDB Error occurred while creating database entry.";
    private static final String BDB_TRX_CREATION_ERROR = "BDB Error occurred while creating transaction.";
    private static final String BDB_TRX_ABORT_ERROR = "BDB Error occurred while aborting transaction.";
    private static final String BDB_VALUE_DESERIALIZATION_ERROR = "BDB Error occurred while de-serializing value of key : ";
    private static final String BDB_CURSOR_CLOSE_ERROR = "BDB Error occurred while closing read cursor.";
    private static final String BDB_TRUNCATE_ERROR = "BDB Error occurred while truncating database.";
    private static final String BDB_RE_OPEN_ERROR = "BDB Error occurred while re-opening database.";
    private static final long MIN_LOG_FILE_SIZE = 1000000;

    /**
     * Constructor to instantiate a BdbCacheImpl
     *
     * @param dbName         Database name.
     * @param dbFilePath     Path to store/open database.
     * @param maxLogFileSize Max log file size in bytes. Minimum allowed is 1,000,000 bytes (1MB).
     * @throws PersistentCacheException If an error occurs while creating the persistent cache.
     */
    public BaseBdbPersistentCache(String dbName, String dbFilePath, long maxLogFileSize) throws PersistentCacheException {
        this.dbName = dbName;
        this.dbFilePath = dbFilePath;
        this.maxLogFileSize = maxLogFileSize;

        if (maxLogFileSize < MIN_LOG_FILE_SIZE) {
            throw new PersistentCacheException("Minimum log file size allowed is " + MIN_LOG_FILE_SIZE);
        }

        lockProvider = new ReEntrantReadWriteLockProvider();
        open();
    }

    /**
     * Method to Open the database.
     *
     * @throws PersistentCacheException If an exception occurs while opening the database.
     */
    private void open() throws PersistentCacheException {
        try {
            EnvironmentConfig envConfig = new EnvironmentConfig();
            envConfig.setTransactional(true);
            // Setting the max je log file size. (Default 10MB)
            envConfig.setConfigParam(EnvironmentConfig.LOG_FILE_MAX, Long.toString(maxLogFileSize));

            envConfig.setAllowCreate(true);

            dbEnvironment = new Environment(new File(dbFilePath), envConfig);
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setTransactional(true);
            database = dbEnvironment.openDatabase(null, dbName, dbConfig);
            setToOpenState();
        } catch (Exception e) {
            throw new PersistentCacheException("Error occurred while creating persistent cache.", e);
        }
    }

    /**
     * Method to store data. If the key already exists the value will be overridden by the new value.
     *
     * @param key   Key to store.
     * @param value Value to store.
     * @return true if success and False if failed.
     * @throws PersistentCacheException if an error occurs while storing data
     */
    @Override
    public boolean put(K key, V value) throws PersistentCacheException {
        checkCacheIsOpen();
        checkKeyIsNull(key);
        boolean result = false;

        try {
            lockProvider.acquireReadLock();
            result = processAndStoreData(null, key, value, true);
        } catch (Exception e) {
            LOGGER.error(BDB_WRITE_ERROR_MSG, e);
        } finally {
            lockProvider.releaseReadLock();
        }

        return result;
    }

    /**
     * Method to put store data with cache expiry time
     *
     * @param key                 the key to store
     * @param value               the value to store
     * @param cacheExpiryTime     the cache expiry time
     * @param cacheExpiryTimeUnit the cache expiry time unit
     * @return true if success and false if not.
     * @throws PersistentCacheException if an error occurs while storing data to the cache
     */
    @Override
    public boolean put(K key, V value, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) throws PersistentCacheException {
        checkCacheIsOpen();
        checkKeyIsNull(key);
        boolean result = false;

        try {
            lockProvider.acquireReadLock();
            result = processAndStoreDataWithCacheExpiryTime(key, value, true, cacheExpiryTime, cacheExpiryTimeUnit);
        } catch (Exception e) {
            LOGGER.error(BDB_WRITE_ERROR_MSG, e);
        } finally {
            lockProvider.releaseReadLock();
        }

        return result;
    }

    /**
     * Method to store key and value if the key is not present.
     *
     * @param key   the key to store
     * @param value the value to store
     * @return true if success and false if not
     * @throws PersistentCacheException if an error occurs while storing data
     */
    @Override
    public boolean putIfAbsent(K key, V value) throws PersistentCacheException {
        checkCacheIsOpen();
        checkKeyIsNull(key);
        boolean result = false;

        try {
            lockProvider.acquireReadLock();
            result = processAndStoreData(null, key, value, false);
        } catch (Exception e) {
            LOGGER.error(BDB_WRITE_ERROR_MSG, e);
        } finally {
            lockProvider.releaseReadLock();
        }

        return result;
    }

    /**
     * Method to store key and value with a given cache expiry time if the key is absent
     *
     * @param key                 the key to store
     * @param value               the value to store
     * @param cacheExpiryTime     the cache expiry time
     * @param cacheExpiryTimeUnit the cache expiry time unit
     * @return true if success and false if not
     * @throws PersistentCacheException if an error occurs while storing data
     */
    @Override
    public boolean putIfAbsent(K key, V value, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) throws PersistentCacheException {
        checkCacheIsOpen();
        checkKeyIsNull(key);
        boolean result = false;

        try {
            lockProvider.acquireReadLock();
            result = processAndStoreDataWithCacheExpiryTime(key, value, false, cacheExpiryTime, cacheExpiryTimeUnit);
        } catch (Exception e) {
            LOGGER.error(BDB_WRITE_ERROR_MSG, e);
        } finally {
            lockProvider.releaseReadLock();
        }

        return result;
    }

    /**
     * Method to store data as a batch.
     *
     * @param data Data to be stored as a map.
     * @return true if success and False if failed.
     * @throws PersistentCacheException if an error occurs while storing data
     */
    @Override
    public boolean putAll(Map<K, V> data) throws PersistentCacheException {
        checkCacheIsOpen();
        boolean result = false;

        try {
            lockProvider.acquireReadLock();
            Transaction transaction = dbEnvironment.beginTransaction(null, null);
            result = storeDataUsingTransaction(transaction, data);
        } catch (DatabaseException e) {
            LOGGER.error(BDB_TRX_CREATION_ERROR, e);
        } finally {
            lockProvider.releaseReadLock();
        }

        return result;
    }

    /**
     * Method to get a value for a given key.
     *
     * @param key Key to retrieve the value.
     * @return Value corresponding to the key.
     * @throws PersistentCacheException if an error occurs while retrieving data from cache
     */
    @Override
    public V get(K key) throws PersistentCacheException {
        checkCacheIsOpen();
        checkKeyIsNull(key);
        V value = null;

        try {
            lockProvider.acquireReadLock();
            value = processAndGetData(key);
        } catch (Exception e) {
            LOGGER.error(BDB_READ_ERROR_MSG, e);
        } finally {
            lockProvider.releaseReadLock();
        }

        return value;
    }

    /**
     * Method to get the value for a given for a given key and delete after retrieving.
     *
     * @param key                         Key to retrieve the value.
     * @param deleteRecordAfterRetrieving If true deletes the record after retrieving the value.
     * @return Value corresponding to given key.
     * @throws PersistentCacheException if an error occurs while retrieving data
     */
    @Override
    public V get(K key, boolean deleteRecordAfterRetrieving) throws PersistentCacheException {
        checkCacheIsOpen();
        checkKeyIsNull(key);
        V value = null;

        try {
            lockProvider.acquireReadLock();
            value = processAndGetData(key);

            if (value != null) {
                if (deleteRecordAfterRetrieving) {
                    deleteRecord(key);
                }
            }

        } catch (Exception e) {
            LOGGER.error(BDB_READ_ERROR_MSG, e);
        } finally {
            lockProvider.releaseReadLock();
        }

        return value;
    }

    /**
     * Method to get all records.
     *
     * @return All records in the cache as a map.
     * @throws PersistentCacheException if an error occurs while retrieving data
     */
    @Override
    public Map<K, V> getAll() throws PersistentCacheException {
        checkCacheIsOpen();
        Map<K, V> records = new LinkedHashMap<>();

        try {
            lockProvider.acquireReadLock();
            Map<DatabaseEntry, DatabaseEntry> databaseEntryMap = getDatabaseEntriesMapFromBDB();
            records.putAll(generateMapOfRecordsFromDatabaseEntries(databaseEntryMap));
        } catch (Exception e) {
            LOGGER.error(BDB_READ_ERROR_MSG, e);
        } finally {
            lockProvider.releaseReadLock();
        }

        return records;
    }

    /**
     * Method to check whether the cache contains the key.
     *
     * @param key the key to check
     * @return true if the key is available and false if not
     * @throws PersistentCacheException if error occurs while checking for the key
     */
    @Override
    public boolean containsKey(K key) throws PersistentCacheException {
        checkCacheIsOpen();
        checkKeyIsNull(key);
        boolean result = false;

        try {
            lockProvider.acquireReadLock();
            DatabaseEntry keyEntry = createDatabaseEntry(key);
            DatabaseEntry valueEntry = createDatabaseEntry(null);
            result = (keyEntry != null) && (database.get(null, keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS);
        } catch (Exception e) {
            LOGGER.error(BDB_READ_ERROR_MSG, e);
        } finally {
            lockProvider.releaseReadLock();
        }

        return result;
    }

    /**
     * Method to delete a record for a given key.
     *
     * @param key Key to delete record.
     * @return true if success and False if failed.
     * @throws PersistentCacheException if an error occurs while deleting the data
     */
    @Override
    public boolean delete(K key) throws PersistentCacheException {
        checkCacheIsOpen();
        checkKeyIsNull(key);
        boolean result = false;

        try {
            lockProvider.acquireReadLock();
            result = deleteRecord(key);
        } finally {
            lockProvider.releaseReadLock();
        }

        return result;
    }

    /**
     * Method to delete the given list of keys.
     *
     * @param keys list of keys to delete
     * @return true if success and false if not
     * @throws PersistentCacheException if an error occurs while deleting the data
     */
    @Override
    public boolean delete(List<K> keys) throws PersistentCacheException {
        checkCacheIsOpen();
        boolean result = false;

        try {
            lockProvider.acquireReadLock();
            Transaction transaction = dbEnvironment.beginTransaction(null, null);
            result = deleteRecordsUsingTransaction(transaction, keys);
        } catch (DatabaseException e) {
            LOGGER.error(BDB_TRX_CREATION_ERROR, e);
        } finally {
            lockProvider.releaseReadLock();
        }

        return result;
    }

    /**
     * Method to delete all records.
     *
     * @return true if success and False if failed
     * @throws PersistentCacheException if an error occurs while deleting the data
     */
    @Override
    public boolean deleteAll() throws PersistentCacheException {
        checkCacheIsOpen();
        boolean result = false;

        try {
            lockProvider.acquireReadLock();
            Transaction transaction = dbEnvironment.beginTransaction(null, null);
            result = deleteAllRecordsUsingTransaction(transaction);
        } catch (DatabaseException e) {
            LOGGER.error(BDB_TRX_CREATION_ERROR, e);
        } finally {
            lockProvider.releaseReadLock();
        }

        return result;
    }

    /**
     * Method to truncate.
     *
     * @return true if success, false if not
     * @throws PersistentCacheException if an error occurs while truncating
     */
    @Override
    public boolean truncate() throws PersistentCacheException {
        checkCacheIsOpen();
        boolean result = false;

        try {
            lockProvider.acquireWriteLock();
            database.close();
            dbEnvironment.truncateDatabase(null, this.dbName, false);
            result = true;
            setToCloseState();
        } catch (DatabaseException e) {
            LOGGER.error(BDB_TRUNCATE_ERROR, e);
        } finally {
            reopenDatabase();
            lockProvider.releaseWriteLock();
        }

        return result;
    }

    /**
     * Method to close the database.
     *
     * @throws PersistentCacheException If an error occurs while closing the cache.
     */
    @Override
    public void close() throws PersistentCacheException {
        checkCacheIsOpen();

        try {
            if (database != null) {
                database.close();
            }

            if (dbEnvironment != null) {
                dbEnvironment.close();
            }

            setToCloseState();
        } catch (DatabaseException e) {
            throw new PersistentCacheException("Error occurred while closing cache.", e);
        }
    }

    /**
     * Method to store data.
     *
     * @param key       key to store.
     * @param value     value to store.
     * @param overwrite overwrite value if exists.
     * @return true if success and false if not.
     */
    protected <T extends Serializable> boolean storeData(Transaction transaction, K key, T value, boolean overwrite) {
        boolean result = false;

        if (key == null || value == null) {
            return true;
        }

        DatabaseEntry keyEntry = createDatabaseEntry(key);
        DatabaseEntry valueEntry = createDatabaseEntry(value);

        if (keyEntry == null || valueEntry == null) {
            return result;
        }

        if (overwrite) {
            result = database.put(transaction, keyEntry, valueEntry) == OperationStatus.SUCCESS;
        } else {
            result = database.putNoOverwrite(transaction, keyEntry, valueEntry) == OperationStatus.SUCCESS;
        }

        return result;
    }

    /**
     * Method to get data.
     *
     * @param key key to retrieve the value.
     * @return value for the given key.
     */
    protected <T extends Serializable> T getData(K key) {
        T value = null;
        DatabaseEntry keyEntry = createDatabaseEntry(key);
        DatabaseEntry valueEntry = createDatabaseEntry(null);

        if ((keyEntry != null) && (database.get(null, keyEntry, valueEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS)) {
            byte[] data = valueEntry.getData();
            value = deserialize(data);
        }

        return value;
    }

    /**
     * Method to store data using a transaction.
     *
     * @param transaction Transaction to be used.
     * @param data        Data to be stored as a map.
     * @return True if success and False if not.
     */
    protected boolean storeDataUsingTransaction(Transaction transaction, Map<K, V> data) {
        boolean result = false;
        try {
            for (Map.Entry<K, V> kv : data.entrySet()) {
                processAndStoreData(transaction, kv.getKey(), kv.getValue(), true);
            }

            transaction.commit();
            result = true;
        } catch (Exception e) {
            LOGGER.error(BDB_WRITE_ERROR_MSG, e);
            abortTransaction(transaction);
        }

        return result;
    }

    /**
     * Method to delete a record.
     *
     * @param key the key to delete
     * @return true if success, false if not
     */
    protected boolean deleteRecord(K key) {
        boolean result = false;

        try {
            Transaction transaction = dbEnvironment.beginTransaction(null, null);
            result = deleteRecordUsingTransaction(transaction, key);
        } catch (DatabaseException e) {
            LOGGER.error(BDB_TRX_CREATION_ERROR, e);
        }

        return result;
    }

    /**
     * Method to delete a record of given key using a transaction.
     *
     * @param transaction Transaction to be used.
     * @param key         Key to delete record.
     * @return True if success and False if not.
     */
    protected boolean deleteRecordUsingTransaction(Transaction transaction, K key) {
        boolean result = false;
        DatabaseEntry keyEntry = createDatabaseEntry(key);
        try {
            if (keyEntry != null) {
                database.delete(transaction, keyEntry);
                transaction.commit();
                result = true;
            }
        } catch (Exception e) {
            LOGGER.error(BDB_DELETE_ERROR_MSG, e);
            abortTransaction(transaction);
        }

        return result;
    }

    /**
     * Method to delete record using a transaction.
     *
     * @param transaction the transaction to be used when deleting data
     * @param keyList     the key list to delete data
     * @return true if success and false if not
     */
    protected boolean deleteRecordsUsingTransaction(Transaction transaction, List<K> keyList) {
        boolean result = true;

        if (CollectionUtils.isEmpty(keyList)) {
            return result;
        }

        try {
            for (K key : keyList) {
                DatabaseEntry keyEntry = createDatabaseEntry(key);

                if (keyEntry != null) {
                    database.delete(transaction, keyEntry);
                }
            }

            transaction.commit();
        } catch (Exception e) {
            LOGGER.error(BDB_DELETE_ERROR_MSG, e);
            abortTransaction(transaction);
            result = false;
        }

        return result;
    }

    /**
     * Method to be used to delete all records using a transaction.
     *
     * @param transaction transaction to be used.
     * @return true if success and False if not.
     */
    protected boolean deleteAllRecordsUsingTransaction(Transaction transaction) {
        boolean result = true;
        try {
            Map<K, V> records = getAll();
            for (K entryKey : records.keySet()) {
                DatabaseEntry keyEntry = createDatabaseEntry(entryKey);
                if (keyEntry != null) {
                    database.delete(transaction, keyEntry);
                }
            }
            transaction.commit();
        } catch (Exception e) {
            LOGGER.error(BDB_DELETE_ERROR_MSG, e);
            abortTransaction(transaction);
            result = false;
        }

        return result;
    }

    /**
     * Method to abort a given transaction.
     *
     * @param transaction transaction to be aborted.
     */
    protected void abortTransaction(Transaction transaction) {
        if (transaction != null) {
            try {
                transaction.abort();
            } catch (DatabaseException e) {
                LOGGER.error(BDB_TRX_ABORT_ERROR, e);
            }
        }
    }

    /**
     * Method to de-serialize a a given array of bytes to an object.
     *
     * @param data Array of bytes to be de-serialized.
     * @return De-serialized object.
     */
    protected <T> T deserialize(byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            return SerializationUtils.deserialize(data);
        } catch (Exception e) {
            LOGGER.error(BDB_VALUE_DESERIALIZATION_ERROR, e);
        }

        return null;
    }

    /**
     * Method to create database entry from an object.
     *
     * @param entry Entry to be used for creating the database entry.
     * @return Database entry.
     * @throws IOException If an error occurs while serializing.
     */
    protected <T extends Serializable> DatabaseEntry createDatabaseEntry(T entry) {
        DatabaseEntry databaseEntry = null;
        try {
            if (entry != null) {
                databaseEntry = new DatabaseEntry(SerializationUtils.serialize(entry));
            } else {
                databaseEntry = new DatabaseEntry();
            }
        } catch (Exception e) {
            LOGGER.error(BDB_ENTRY_CREATION_ERROR, e);
        }

        return databaseEntry;
    }

    /**
     * Method to check whether the cache object is outdated.
     *
     * @param dateTime Date time to check.
     * @return True if outdated and False if not.
     */
    protected boolean isCacheObjectOutDated(LocalDateTime dateTime) {
        return LocalDateTime.now().isAfter(dateTime);
    }

    /**
     * Method to generate cache expiry date time.
     *
     * @return Cache expiry date time.
     */
    protected LocalDateTime generateCacheExpiryDateTime(long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit) {
        if (cacheExpiryTimeUnit == null) {
            cacheExpiryTimeUnit = ChronoUnit.SECONDS;
        }

        return LocalDateTime.now().plus(cacheExpiryTime, cacheExpiryTimeUnit);
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
    protected abstract boolean processAndStoreData(Transaction transaction, K key, V value, boolean overwrite);

    /**
     * Method to process and store data  with cache expiry time.
     *
     * @param key                 the key to store
     * @param value               the value to store
     * @param overwrite           overwrite if true and does not overwrite if false
     * @param cacheExpiryTime     the cache expiry time
     * @param cacheExpiryTimeUnit the cache expiry time unit
     * @return true if success and false if not
     */
    protected abstract boolean processAndStoreDataWithCacheExpiryTime(K key, V value, boolean overwrite, long cacheExpiryTime, ChronoUnit cacheExpiryTimeUnit);

    /**
     * Method to process and get data.
     *
     * @param key the key to get value
     * @return the value
     */
    protected abstract V processAndGetData(K key);

    /**
     * Method to generate map of records from database entries.
     *
     * @param databaseEntryMap the database entry map
     * @return generated key value map
     */
    protected abstract Map<K, V> generateMapOfRecordsFromDatabaseEntries(Map<DatabaseEntry, DatabaseEntry> databaseEntryMap);

    /**
     * Method to re open database.
     */
    private void reopenDatabase() {
        try {
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setTransactional(true);
            database = dbEnvironment.openDatabase(null, dbName, dbConfig);
            setToOpenState();
        } catch (DatabaseException e) {
            LOGGER.error(BDB_RE_OPEN_ERROR, e);
        }
    }

    /**
     * Method to check whether the cache is open
     *
     * @throws PersistentCacheException if an error occurs while checking the state of the cache
     */
    private void checkCacheIsOpen() throws PersistentCacheException {
        if (!isOpen.get()) {
            throw new PersistentCacheException("Persistent cache is in closed state.");
        }
    }

    /**
     * Method to check whether the given key is null.
     *
     * @param key the key to check
     * @throws PersistentCacheException if an error occurs while checking for the key
     */
    private void checkKeyIsNull(K key) throws PersistentCacheException {
        if (key == null) {
            throw new PersistentCacheException("Key cannot be null.");
        }
    }

    /**
     * Method to set the cache to open state.
     */
    private void setToOpenState() {
        isOpen.set(true);
    }

    /**
     * Method to set the cache to close state.
     */
    private void setToCloseState() {
        isOpen.set(false);
    }

    /**
     * Method to get database entries map from berkeley db.
     *
     * @return database entries map from berkeley db
     */
    private Map<DatabaseEntry, DatabaseEntry> getDatabaseEntriesMapFromBDB() {
        Cursor cursor = null;
        Map<DatabaseEntry, DatabaseEntry> databaseEntryMap = new LinkedHashMap<>();

        try {
            cursor = database.openCursor(null, null);
            DatabaseEntry keyEntry = createDatabaseEntry(null);
            DatabaseEntry dataEntry = createDatabaseEntry(null);

            while (cursor.getNext(keyEntry, dataEntry, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                databaseEntryMap.put(keyEntry, dataEntry);
                keyEntry = createDatabaseEntry(null);
                dataEntry = createDatabaseEntry(null);
            }
        } catch (Exception e) {
            LOGGER.error(BDB_READ_ERROR_MSG, e);
        } finally {
            try {
                if (cursor != null) {
                    cursor.close();
                }

            } catch (DatabaseException e) {
                LOGGER.error(BDB_CURSOR_CLOSE_ERROR, e);
            }
        }

        return databaseEntryMap;
    }
}

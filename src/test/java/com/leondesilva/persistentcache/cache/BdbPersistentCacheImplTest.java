package com.leondesilva.persistentcache.cache;

import com.leondesilva.persistentcache.error.PersistentCacheException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BdbPersistentCacheImplTest {
    PersistentCache<String, TestCacheObject> persistentCache;
    private static final String dbName = "PersistentCacheImplTestDB";
    String dbPath;
    private static final long maxLogFileSize = 5000000;

    private static final String KEY1 = "k1";
    private static final String KEY2 = "k2";
    private static final String KEY3 = "k3";
    private static final String KEY4 = "k4";
    private static final String KEY5 = "k5";

    private TestCacheObject testCacheObject1 = new TestCacheObject("1");
    private TestCacheObject testCacheObject2 = new TestCacheObject("2");
    private TestCacheObject testCacheObject3 = new TestCacheObject("3");
    private TestCacheObject testCacheObject4 = new TestCacheObject("4");
    private TestCacheObject testCacheObject5 = new TestCacheObject("5");

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Before
    public void setup() throws IOException, PersistentCacheException {
        dbPath = "/Drive/TESTBDBFILE"; //temporaryFolder.newFolder().getAbsolutePath();
        persistentCache = PersistentCacheFactory.createCache(dbName, dbPath, maxLogFileSize);
    }

    @Test
    public void should_store_data_correctly_using_put_method() throws IOException, PersistentCacheException {
        Assert.assertTrue(persistentCache.put(KEY1, testCacheObject1));
        Assert.assertTrue(persistentCache.put(KEY2, testCacheObject2));
        Assert.assertTrue(persistentCache.put(KEY3, testCacheObject3));
        Assert.assertTrue(persistentCache.put(KEY4, testCacheObject4));
        Assert.assertTrue(persistentCache.put(KEY5, testCacheObject5));

        Assert.assertEquals(testCacheObject1, persistentCache.get(KEY1));
        Assert.assertEquals(testCacheObject2, persistentCache.get(KEY2));
        Assert.assertEquals(testCacheObject3, persistentCache.get(KEY3));
        Assert.assertEquals(testCacheObject4, persistentCache.get(KEY4));
        Assert.assertEquals(testCacheObject5, persistentCache.get(KEY5));
    }

    @Test
    public void should_update_existing_values_from_put_method() throws PersistentCacheException {
        Assert.assertTrue(persistentCache.put(KEY1, testCacheObject1));
        Assert.assertTrue(persistentCache.put(KEY1, testCacheObject2));
        Assert.assertEquals(testCacheObject2, persistentCache.get(KEY1));
    }

    @Test (expected = UnsupportedOperationException.class)
    public void should_throw_a_not_supported_exception_when_put_with_cache_expiry_method_is_called() throws PersistentCacheException {
        persistentCache.put(KEY1, testCacheObject1, 100000, ChronoUnit.MILLIS);
    }

    @Test
    public void should_store_data_correctly_using_put_if_absent_method() throws IOException, PersistentCacheException {
        Assert.assertTrue(persistentCache.putIfAbsent(KEY1, testCacheObject1));
        Assert.assertTrue(persistentCache.putIfAbsent(KEY2, testCacheObject2));
        Assert.assertTrue(persistentCache.putIfAbsent(KEY3, testCacheObject3));
        Assert.assertTrue(persistentCache.putIfAbsent(KEY4, testCacheObject4));
        Assert.assertTrue(persistentCache.putIfAbsent(KEY5, testCacheObject5));

        Assert.assertEquals(testCacheObject1, persistentCache.get(KEY1));
        Assert.assertEquals(testCacheObject2, persistentCache.get(KEY2));
        Assert.assertEquals(testCacheObject3, persistentCache.get(KEY3));
        Assert.assertEquals(testCacheObject4, persistentCache.get(KEY4));
        Assert.assertEquals(testCacheObject5, persistentCache.get(KEY5));
    }

    @Test
    public void should_not_update_existing_key_value_if_put_if_absent_method_is_called() throws PersistentCacheException {
        Assert.assertTrue(persistentCache.put(KEY1, testCacheObject1));
        // False should be returned if the key exists.
        Assert.assertFalse(persistentCache.putIfAbsent(KEY1, testCacheObject2));
        Assert.assertEquals(testCacheObject1, persistentCache.get(KEY1));
    }

    @Test (expected = UnsupportedOperationException.class)
    public void should_throw_a_not_supported_exception_when_put_if_absent_with_cache_expiry_method_is_called() throws PersistentCacheException {
        persistentCache.putIfAbsent(KEY1, testCacheObject1, 100000, ChronoUnit.MILLIS);
    }

    @Test
    public void should_put_all_the_key_value_entries_provided_as_a_map() throws PersistentCacheException {
        Map<String, TestCacheObject> map = new LinkedHashMap<>();
        map.put(KEY1, testCacheObject1);
        map.put(KEY2, testCacheObject2);
        map.put(KEY3, testCacheObject3);

        Assert.assertTrue(persistentCache.putAll(map));
        Assert.assertEquals(map, persistentCache.getAll());
    }

    @Test
    public void should_delete_record_after_retrieving() throws PersistentCacheException {
        persistentCache.put(KEY1, testCacheObject1);
        persistentCache.put(KEY2, testCacheObject2);
        Assert.assertEquals(testCacheObject1, persistentCache.get(KEY1, true));
        Assert.assertNull(persistentCache.get(KEY1));
        Assert.assertEquals(testCacheObject2, persistentCache.get(KEY2));
    }

    @Test
    public void should_return_true_if_the_key_exists_and_false_if_does_not_exist() throws PersistentCacheException {
        persistentCache.put(KEY4, testCacheObject4);
        Assert.assertTrue(persistentCache.containsKey(KEY4));
        Assert.assertFalse(persistentCache.containsKey(KEY5));
    }

    @Test
    public void should_delete_a_record_for_a_given_key() throws PersistentCacheException {
        persistentCache.put(KEY1, testCacheObject1);
        persistentCache.put(KEY2, testCacheObject2);

        Assert.assertTrue(persistentCache.delete(KEY1));
        Assert.assertNull(persistentCache.get(KEY1));
        Assert.assertEquals(testCacheObject2, persistentCache.get(KEY2));
    }

    @Test
    public void should_delete_given_list_of_keys() throws PersistentCacheException {
        Map<String, TestCacheObject> map = new LinkedHashMap<>();
        map.put(KEY1, testCacheObject1);
        map.put(KEY2, testCacheObject2);
        map.put(KEY3, testCacheObject3);
        persistentCache.putAll(map);

        List<String> keysList = new LinkedList<>();
        keysList.add(KEY2);
        keysList.add(KEY3);

        Assert.assertTrue(persistentCache.delete(keysList));
        Assert.assertEquals(testCacheObject1, persistentCache.get(KEY1));
        Assert.assertEquals(null, persistentCache.get(KEY2));
        Assert.assertEquals(null, persistentCache.get(KEY3));
    }

    @Test
    public void should_delete_all_records_correctly() throws PersistentCacheException {
        Map<String, TestCacheObject> map = new LinkedHashMap<>();
        map.put(KEY1, testCacheObject1);
        map.put(KEY2, testCacheObject2);
        map.put(KEY3, testCacheObject3);
        persistentCache.putAll(map);

        Assert.assertTrue(persistentCache.deleteAll());
        Assert.assertTrue(persistentCache.getAll().isEmpty());
    }

    @Test
    public void should_truncate_correctly() throws PersistentCacheException {
        Map<String, TestCacheObject> map = new LinkedHashMap<>();
        map.put(KEY1, testCacheObject1);
        map.put(KEY2, testCacheObject2);
        map.put(KEY3, testCacheObject3);
        persistentCache.putAll(map);

        Assert.assertTrue(persistentCache.truncate());
        Assert.assertTrue(persistentCache.getAll().isEmpty());
    }

    @Test (expected = PersistentCacheException.class)
    public void should_thrown_an_exception_when_put_is_called_in_close_state() throws PersistentCacheException {
        try {
            persistentCache.close();
        } catch (Exception e) {
            Assert.fail();
        }

        persistentCache.put(KEY1, testCacheObject1);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void should_throw_a_not_supported_exception_when_put_with_cache_expiry_method_is_called_at_closed_state() throws PersistentCacheException {
        try {
            persistentCache.close();
        } catch (Exception e) {
            Assert.fail();
        }

        persistentCache.put(KEY1, testCacheObject1, 100000, ChronoUnit.MILLIS);
    }

    @Test (expected = PersistentCacheException.class)
    public void should_thrown_an_exception_when_put_if_absent_method_is_called_in_close_state() throws PersistentCacheException {
        try {
            persistentCache.close();
        } catch (Exception e) {
            Assert.fail();
        }

        persistentCache.putIfAbsent(KEY1, testCacheObject1);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void should_throw_a_not_supported_exception_when_put_if_absent_with_cache_expiry_method_is_called_at_closed_state() throws PersistentCacheException {
        try {
            persistentCache.close();
        } catch (Exception e) {
            Assert.fail();
        }

        persistentCache.putIfAbsent(KEY1, testCacheObject1, 100000, ChronoUnit.MILLIS);
    }

    @Test (expected = PersistentCacheException.class)
    public void should_thrown_an_exception_when_put_all_method_is_called_in_close_state() throws PersistentCacheException {
        try {
            persistentCache.close();
        } catch (Exception e) {
            Assert.fail();
        }

        Map<String, TestCacheObject> map = new LinkedHashMap<>();
        map.put(KEY1, testCacheObject1);
        persistentCache.putAll(map);
    }

    @Test (expected = PersistentCacheException.class)
    public void should_thrown_an_exception_when_get_method_is_called_in_close_state() throws PersistentCacheException {
        try {
            persistentCache.close();
        } catch (Exception e) {
            Assert.fail();
        }

        persistentCache.get(KEY1);
    }

    @Test (expected = PersistentCacheException.class)
    public void should_thrown_an_exception_when_get__with_delete_method_is_called_in_close_state() throws PersistentCacheException {
        try {
            persistentCache.close();
        } catch (Exception e) {
            Assert.fail();
        }

        persistentCache.get(KEY1, true);
    }

    @Test (expected = PersistentCacheException.class)
    public void should_thrown_an_exception_when_get_all_method_is_called_in_close_state() throws PersistentCacheException {
        try {
            persistentCache.close();
        } catch (Exception e) {
            Assert.fail();
        }

        persistentCache.getAll();
    }

    @Test (expected = PersistentCacheException.class)
    public void should_thrown_an_exception_when_contains_key_method_is_called_in_close_state() throws PersistentCacheException {
        try {
            persistentCache.close();
        } catch (Exception e) {
            Assert.fail();
        }

        persistentCache.containsKey(KEY1);
    }

    @Test (expected = PersistentCacheException.class)
    public void should_thrown_an_exception_when_delete_method_is_called_in_close_state() throws PersistentCacheException {
        try {
            persistentCache.close();
        } catch (Exception e) {
            Assert.fail();
        }

        persistentCache.delete(KEY1);
    }

    @Test (expected = PersistentCacheException.class)
    public void should_thrown_an_exception_when_delete_all_method_is_called_in_close_state() throws PersistentCacheException {
        try {
            persistentCache.close();
        } catch (Exception e) {
            Assert.fail();
        }

        persistentCache.deleteAll();
    }

    @Test (expected = PersistentCacheException.class)
    public void should_thrown_an_exception_when_truncate_method_is_called_in_close_state() throws PersistentCacheException {
        try {
            persistentCache.close();
        } catch (Exception e) {
            Assert.fail();
        }

        persistentCache.truncate();
    }



}
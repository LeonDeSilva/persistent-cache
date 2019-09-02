package com.leondesilva.persistentcache.cache;

import com.leondesilva.persistentcache.cache.loaders.CacheLoader;
import com.leondesilva.persistentcache.error.PersistentCacheException;
import org.junit.*;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Ignore
public class BdbPersistentLoadingCacheImplTest {
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
        dbPath = temporaryFolder.newFolder().getAbsolutePath();
        persistentCache = PersistentCacheFactory.createLoadingCache(dbName, dbPath, maxLogFileSize, 3, ChronoUnit.SECONDS, new CacheLoader<String, TestCacheObject>() {
            private Map<String, Integer> instanceCounter = new HashMap<String, Integer>();

            @Override
            public TestCacheObject load(String key) {
                int i = 1;

                if (instanceCounter.containsKey(key)) {
                    i = instanceCounter.get(key) + 1;
                } else {
                    instanceCounter.put(key, i);
                }

               return new TestCacheObject(key + "##" + i);

                // TODO : TESTS FOR CACHE LOADER !!!!
            }
        });
    }

    @Test (expected = UnsupportedOperationException.class)
    public void should_throw_a_not_supported_exception_when_put_method_is_called() throws IOException, PersistentCacheException, InterruptedException {
        persistentCache.put(KEY1, testCacheObject1);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void should_throw_a_not_supported_exception_when_put_with_cache_expiry_method_is_called() throws PersistentCacheException {
        persistentCache.put(KEY1, testCacheObject1, 100000, ChronoUnit.MILLIS);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void should_throw_a_not_supported_exception_when_put_if_absent_method_is_called() throws IOException, PersistentCacheException, InterruptedException {
        persistentCache.putIfAbsent(KEY1, testCacheObject1);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void should_throw_a_not_supported_exception_when_put_if_absent_with_cache_expiry_method_is_called() throws PersistentCacheException {
        persistentCache.putIfAbsent(KEY1, testCacheObject1, 100000, ChronoUnit.MILLIS);
    }

    @Test (expected = UnsupportedOperationException.class)
    public void should_throw_a_not_supported_exception_when_put_all_method_is_called() throws PersistentCacheException {
        Map<String, TestCacheObject> map = new LinkedHashMap<>();
        map.put(KEY1, testCacheObject1);
        map.put(KEY2, testCacheObject2);
        map.put(KEY3, testCacheObject3);

        persistentCache.putAll(map);
    }

    @Test
    public void should_return_correct_object_loaded_from_cache_loader() throws PersistentCacheException {
        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 1), persistentCache.get(KEY1));
        Assert.assertEquals(new TestCacheObject(KEY2 + "##" + 1), persistentCache.get(KEY2));
        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 1), persistentCache.get(KEY1));
    }

    @Test
    public void should_return_new_object_loaded_from_cache_loader_after_cache_is_expired() throws PersistentCacheException, InterruptedException {
        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 1), persistentCache.get(KEY1));
        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 1), persistentCache.get(KEY1));
        Thread.sleep(3000);

        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 2), persistentCache.get(KEY1));
    }

    @Test
    public void should_delete_a_record_after_retrieving_and_next_get_should_be_called_through_cache_loader() throws PersistentCacheException {
        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 1), persistentCache.get(KEY1, true));
        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 2), persistentCache.get(KEY1));
    }

    @Test (expected = UnsupportedOperationException.class)
    public void should_throw_a_not_supported_exception_when_contains_key_method_is_called() throws PersistentCacheException {
        Assert.assertTrue(persistentCache.containsKey(KEY1));
    }

    @Test
    public void should_delete_a_record_for_a_given_key_and_load_from_cache_loader_in_next_get_method_call() throws PersistentCacheException {
        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 1), persistentCache.get(KEY1));
        Assert.assertTrue(persistentCache.delete(KEY1));
        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 2), persistentCache.get(KEY1));
    }

    @Test
    public void should_delete_given_list_of_keys_and_load_from_cache_loader_in_next_get_method_call() throws PersistentCacheException {
        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 1), persistentCache.get(KEY1));
        Assert.assertEquals(new TestCacheObject(KEY2 + "##" + 1), persistentCache.get(KEY2));
        Assert.assertEquals(new TestCacheObject(KEY3 + "##" + 1), persistentCache.get(KEY3));
        Assert.assertEquals(new TestCacheObject(KEY4 + "##" + 1), persistentCache.get(KEY4));
        Assert.assertEquals(new TestCacheObject(KEY5 + "##" + 1), persistentCache.get(KEY5));

        Assert.assertTrue(persistentCache.delete(KEY2));
        Assert.assertTrue(persistentCache.delete(KEY4));
        Assert.assertTrue(persistentCache.delete(KEY5));

        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 1), persistentCache.get(KEY1));
        Assert.assertEquals(new TestCacheObject(KEY2 + "##" + 2), persistentCache.get(KEY2));
        Assert.assertEquals(new TestCacheObject(KEY3 + "##" + 1), persistentCache.get(KEY3));
        Assert.assertEquals(new TestCacheObject(KEY4 + "##" + 2), persistentCache.get(KEY4));
        Assert.assertEquals(new TestCacheObject(KEY5 + "##" + 2), persistentCache.get(KEY5));
    }

    @Test
    public void should_delete_all_records_correctly_and_load_from_cache_loader() throws PersistentCacheException {
        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 1), persistentCache.get(KEY1));
        Assert.assertEquals(new TestCacheObject(KEY2 + "##" + 1), persistentCache.get(KEY2));
        Assert.assertEquals(new TestCacheObject(KEY3 + "##" + 1), persistentCache.get(KEY3));
        Assert.assertEquals(new TestCacheObject(KEY4 + "##" + 1), persistentCache.get(KEY4));
        Assert.assertEquals(new TestCacheObject(KEY5 + "##" + 1), persistentCache.get(KEY5));

        Assert.assertTrue(persistentCache.deleteAll());

        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 2), persistentCache.get(KEY1));
        Assert.assertEquals(new TestCacheObject(KEY2 + "##" + 2), persistentCache.get(KEY2));
        Assert.assertEquals(new TestCacheObject(KEY3 + "##" + 2), persistentCache.get(KEY3));
        Assert.assertEquals(new TestCacheObject(KEY4 + "##" + 2), persistentCache.get(KEY4));
        Assert.assertEquals(new TestCacheObject(KEY5 + "##" + 2), persistentCache.get(KEY5));
    }

    @Test
    public void should_truncate_correctly_and_load_from_cache_loader_in_next_get_method_call() throws PersistentCacheException {
        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 1), persistentCache.get(KEY1));
        Assert.assertEquals(new TestCacheObject(KEY2 + "##" + 1), persistentCache.get(KEY2));
        Assert.assertEquals(new TestCacheObject(KEY3 + "##" + 1), persistentCache.get(KEY3));
        Assert.assertEquals(new TestCacheObject(KEY4 + "##" + 1), persistentCache.get(KEY4));
        Assert.assertEquals(new TestCacheObject(KEY5 + "##" + 1), persistentCache.get(KEY5));

        Assert.assertTrue(persistentCache.truncate());

        Assert.assertEquals(new TestCacheObject(KEY1 + "##" + 2), persistentCache.get(KEY1));
        Assert.assertEquals(new TestCacheObject(KEY2 + "##" + 2), persistentCache.get(KEY2));
        Assert.assertEquals(new TestCacheObject(KEY3 + "##" + 2), persistentCache.get(KEY3));
        Assert.assertEquals(new TestCacheObject(KEY4 + "##" + 2), persistentCache.get(KEY4));
        Assert.assertEquals(new TestCacheObject(KEY5 + "##" + 2), persistentCache.get(KEY5));
    }

    @Test (expected = UnsupportedOperationException.class)
    public void should_thrown_a_not_supported_exception_when_put_is_called_in_close_state() throws PersistentCacheException {
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

    @Test (expected = UnsupportedOperationException.class)
    public void should_thrown_a_not_supported_exception_when_put_if_absent_method_is_called_in_close_state() throws PersistentCacheException {
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

    @Test (expected = UnsupportedOperationException.class)
    public void should_thrown_a_not_supported_exception_when_put_all_method_is_called_in_close_state() throws PersistentCacheException {
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

    @Test (expected = UnsupportedOperationException.class)
    public void should_thrown_a_not_supported_exception_when_contains_key_method_is_called_in_close_state() throws PersistentCacheException {
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
}
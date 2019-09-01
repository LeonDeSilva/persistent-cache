package com.leondesilva.persistentcache.cache;

import java.io.Serializable;

public class TestCacheObject implements Serializable {
    private String id;

    public TestCacheObject(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof TestCacheObject)) {
            return false;
        }

        TestCacheObject that = (TestCacheObject) o;

        if (!id.equals(that.id)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
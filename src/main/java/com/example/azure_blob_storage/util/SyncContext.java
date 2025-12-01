package com.example.azure_blob_storage.util;

import java.util.HashMap;
import java.util.Map;

public class SyncContext {

    private final Map<String, Object> properties = new HashMap<>();

    public void set(String name, Object value) {
        properties.put(name, value);
    }

    public String get(String name) {
        return String.class.cast(properties.get(name));
    }

    public <T> T get(String name, Class<T> requiredType) {
        return requiredType.cast(properties.get(name));
    }
}

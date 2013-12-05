package com.jetdrone.vertx.xson.java;

import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.impl.Base64;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonObject extends HashMap<String, Object> implements JsonElement<String>, Map<String, Object> {

    public JsonObject() {
        super();
    }

    public JsonObject(Map<String, Object> source) {
        super(source);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getAt(String key) {
        return (V) super.get(key);
    }

    @Override
    public <V> JsonObject putAt(String key, V value) {
        super.put(key, value);
        return this;
    }

    @Override
    public boolean isArray() {
        return false;
    }

    @Override
    public boolean isObject() {
        return true;
    }

    @Override
    public JsonObject asJsonObject() {
        return this;
    }

    @Override
    public JsonArray asJsonArray() {
        throw new ClassCastException();
    }

    // basic json types

    public boolean getBoolean(String key) {
        Boolean value = (Boolean) super.get(key);
        if (value == null) {
            return false;
        }

        return value;
    }

    public Number getNumber(String key) {
        Number value = (Number) super.get(key);
        if (value == null) {
            return null;
        }

        return value;
    }

    public String getString(String key) {
        String value = (String) super.get(key);
        if (value == null) {
            return null;
        }

        return value;
    }

    public JsonArray getArray(String key) {
        List value = (List) super.get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof JsonArray) {
            return (JsonArray) value;
        }

        JsonArray res = new JsonArray();
        res.addAll(value);
        return res;
    }

    @SuppressWarnings("unchecked")
    public JsonObject getObject(String key) {
        Map<String, Object> value = (Map<String, Object>) super.get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof JsonObject) {
            return (JsonObject) value;
        }

        return new JsonObject(value);
    }

    // extension

    public byte[] getBinary(String key) {
        Object value = super.get(key);
        if (value != null) {
            if (value instanceof String) {
                // expected Base64 string
                return Base64.decode((String) value);
            }
            if (value instanceof byte[]) {
                // this value was never encoded (just passed around in the heap)
                return (byte[]) value;
            }
            throw new DecodeException("invalid format");
        }

        return null;
    }

    public Date getDate(String key) {
        Object value = super.get(key);
        if (value != null) {
            if (value instanceof String) {
                // expected ISO8601 UTC string
                try {
                    return com.jetdrone.vertx.xson.core.JSON.DATE_FORMAT.parse((String) value);
                } catch (ParseException e) {
                    throw new DecodeException(e.getMessage());
                }
            }
            if (value instanceof Date) {
                // this value was never encoded (just passed around in the heap)
                return (Date) value;
            }
            throw new DecodeException("invalid format");
        }

        return null;
    }
}

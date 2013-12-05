package com.jetdrone.vertx.xson.java;

import com.jetdrone.vertx.xson.core.*;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.impl.Base64;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class JsonArray extends ArrayList<Object> implements JsonElement<Integer>, List<Object> {

    private static final long serialVersionUID = 1l;

    public JsonArray() {
        super();
    }

    public JsonArray(List<?> list) {
        super(list);
    }

    public JsonArray(Object... elements) {
        super();
        if (elements != null) {
            for (Object t : elements) {
                add(t);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getAt(Integer index) {
        return (V) super.get(index);
    }

    @Override
    public <V> JsonArray putAt(Integer key, V value) {
        super.add(key, value);
        return this;
    }

    @Override
    public boolean isArray() {
        return true;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public JsonObject asJsonObject() {
        throw new ClassCastException();
    }

    @Override
    public JsonArray asJsonArray() {
        return this;
    }

    // basic json types

    public boolean getBoolean(int index) {
        Boolean value = (Boolean) super.get(index);
        if (value == null) {
            return false;
        }

        return value;
    }

    public Number getNumber(int index) {
        Number value = (Number) super.get(index);
        if (value == null) {
            return null;
        }

        return value;
    }

    public String getString(int index) {
        String value = (String) super.get(index);
        if (value == null) {
            return null;
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    public JsonArray getArray(int index) {
        List<?> value = (List<?>) super.get(index);
        if (value == null) {
            return null;
        }

        if (value instanceof JsonArray) {
            return (JsonArray) value;
        }

        return new JsonArray(value);
    }

    @SuppressWarnings("unchecked")
    public JsonObject getObject(int index) {
        Map<String, ?> value = (Map<String, ?>) super.get(index);
        if (value == null) {
            return null;
        }

        if (value instanceof JsonObject) {
            return (JsonObject) value;
        }

        return new JsonObject(value);
    }

    // extension

    public byte[] getBinary(int index) {
        Object value = super.get(index);
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

    public Date getDate(int index) {
        Object value = super.get(index);
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

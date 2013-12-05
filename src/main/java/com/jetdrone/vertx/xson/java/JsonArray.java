package com.jetdrone.vertx.xson.java;

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

    @Override
    public boolean getBoolean(Integer index) {
        Boolean value = (Boolean) super.get(index);
        if (value == null) {
            return false;
        }

        return value;
    }

    @Override
    public Number getNumber(Integer index) {
        Number value = (Number) super.get(index);
        if (value == null) {
            return null;
        }

        return value;
    }

    @Override
    public String getString(Integer index) {
        String value = (String) super.get(index);
        if (value == null) {
            return null;
        }

        return value;
    }

    @SuppressWarnings("unchecked")
    @Override
    public JsonArray getArray(Integer index) {
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
    @Override
    public JsonObject getObject(Integer index) {
        Map<String, ?> value = (Map<String, ?>) super.get(index);
        if (value == null) {
            return null;
        }

        if (value instanceof JsonObject) {
            return (JsonObject) value;
        }

        return new JsonObject(value);
    }

    @Override
    public byte[] getBinary(Integer index) {
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

    @Override
    public Date getDate(Integer index) {
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

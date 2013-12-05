package com.jetdrone.vertx.xson.java;

import org.vertx.java.core.buffer.Buffer;

import java.util.List;
import java.util.Map;

public final class JSON extends com.jetdrone.vertx.xson.core.JSON {

    private JSON() {}

    @SuppressWarnings("unchecked")
    public static <R> R decode(Buffer source) {
        return decode(source.getBytes());
    }

    public static JsonObject decodeObject(Buffer source) {
        if (source == null) {
            return null;
        }

        return new JsonObject(decodeObject(source.getBytes()));
    }

    public static JsonArray decodeArray(Buffer source) {
        if (source == null) {
            return null;
        }

        return new JsonArray(decodeArray(source.getBytes()));
    }

    @SuppressWarnings("unchecked")
    public static JsonElement<?> decodeElement(Buffer source) {
        Object json = decode(source);

        if (json == null) {
            return null;
        }

        if (json instanceof Map) {
            return new JsonObject((Map<String, Object>) json);
        }

        if (json instanceof List) {
            return new JsonArray((List<Object>) json);
        }

        throw new ClassCastException();
    }
}

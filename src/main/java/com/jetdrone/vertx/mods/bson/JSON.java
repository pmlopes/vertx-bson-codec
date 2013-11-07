package com.jetdrone.vertx.mods.bson;

import org.vertx.java.core.buffer.Buffer;

import java.util.*;

public class JSON {

    private static final byte[] NULL = new byte[]{'n', 'u', 'l', 'l'};

    public static Buffer encode(List list) {
        return encodeList(new Buffer(), list);
    }

    public static Buffer encode(Map map) {
        return encodeMap(new Buffer(), map);
    }

    public static <T> T decode(Buffer buffer) {
        JsonParser parser = new JsonParser(buffer);
        return (T) parser.parse();
    }

    private static Buffer encodeList(Buffer buffer, List list) {
        buffer.appendByte((byte) '[');

        for (int i = 0, len = list.size() - 1; i <= len; i++) {
            Object value = list.get(i);
            encode(buffer, value);
            if (i != len) {
                buffer.appendByte((byte) ',');
            }
        }

        buffer.appendByte((byte) ']');
        return buffer;
    }

    private static Buffer encodeMap(Buffer buffer, Map map) {
        buffer.appendByte((byte) '{');

        final Set<Map.Entry> entries = map.entrySet();
        int size = entries.size();

        for (Map.Entry entry : entries) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            encodeString(buffer, key);
            buffer.appendByte((byte) ':');
            encode(buffer, value);
            if (--size != 0) {
                buffer.appendByte((byte) ',');
            }
        }

        buffer.appendByte((byte) '}');
        return buffer;
    }

    private static void encodeString(Buffer buffer, String str) {
        buffer.appendByte((byte) '"');
        for (int i = 0, len = str.length(); i < len; i++) {
            char c = str.charAt(i);

            if ((1024 <= c && c <= 1279) || (1280 <= c && c <= 1327) || (11744 <= c && c <= 11775) || (42560 <= c && c <= 42655)) {
                buffer.appendByte((byte) '\\');
                buffer.appendByte((byte) 'u');
                buffer.appendString(Integer.toHexString(c));
            } else {
                if (c == '"' || c == '\\' || c == '/' || c == '\b' || c == '\f' || c == '\n' || c == '\r' || c == '\t') {
                    buffer.appendByte((byte) '\\');
                }
                buffer.appendByte((byte) c);
            }
        }
        buffer.appendByte((byte) '"');
    }

    private static void encode(Buffer buffer, Object value) {
        if (value == null) {
            buffer.appendBytes(NULL);
        } else {
            if (value instanceof Boolean) {
                buffer.appendString(value.toString());
            } else if (value instanceof Number) {
                buffer.appendString(value.toString());
            } else if (value instanceof String) {
                encodeString(buffer, (String) value);
            } else if (value instanceof Map) {
                encodeMap(buffer, (Map) value);
            } else if (value instanceof List) {
                encodeList(buffer, (List) value);
            }
        }
    }
}

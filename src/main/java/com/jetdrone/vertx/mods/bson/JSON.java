package com.jetdrone.vertx.mods.bson;

import org.vertx.java.core.buffer.Buffer;

import java.util.*;

public class JSON {

    private static final byte[] NULL = new byte[]{'n', 'u', 'l', 'l'};

    public static Buffer encodeList(List list) {
        return encodeList(new Buffer(), list);
    }

    public static Buffer encodeMap(Map map) {
        return encodeMap(new Buffer(), map);
    }

    public static <T> T decode(Buffer buffer) {
        return (T) decodeValue(buffer, new int[] {0});
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

    private static List decodeList(Buffer buffer, int[] pos) {
        List array = new ArrayList();
        return array;
    }

    private static Map decodeMap(Buffer buffer, int[] pos) {
        Map object = new HashMap();
        return object;
    }

    private static String decodeString(Buffer buffer, int[] pos) {
        // start
        pos[0]++;
        // placeholder
        StringBuilder sb = new StringBuilder();
        // temp
        byte b;

        while ((b = buffer.getByte(pos[0])) != '"') {
            if (b == '\\') {
                // read escape
                b = buffer.getByte(++pos[0]);
                switch (b) {
                    case '"': sb.append('"'); break;
                    case '/': sb.append('/'); break;
                    case '\\': sb.append('\\'); break;
                    case 'b': sb.append('\b'); break;
                    case 'f': sb.append('\f'); break;
                    case 'n': sb.append('\n'); break;
                    case 'r': sb.append('\r'); break;
                    case 't': sb.append('\t'); break;
                    case 'u':
                        // TODO: read 4 hex digits
                        break;
                    default:
                        throw new RuntimeException("Unknown escape sequence: " + (char) b);
                }
            } else {
                sb.append((char) b);
            }
            pos[0]++;
        }

        return sb.toString();
    }

    private static Object decodeValue(Buffer buffer, int[] pos) {
        byte b = buffer.getByte(pos[0]);
        // skip while spaces
        while (b == ' ' || b == '\t' || b == '\n' || b == '\b' || b == '\f' || b == '\r' || b == '\0') {
            b = buffer.getByte(++pos[0]);
        }
        // identify the type
        if (b == 'n') {
            // should be null
            if (buffer.getByte(++pos[0]) == 'u' && buffer.getByte(++pos[0]) == 'l' && buffer.getByte(++pos[0]) == 'l') {
                // we were correct
                return null;
            } else {
                throw new RuntimeException("Invalid JSON: " + buffer.toString());
            }
        }
        if (b == 't') {
            // should be true
            if (buffer.getByte(++pos[0]) == 'r' && buffer.getByte(++pos[0]) == 'u' && buffer.getByte(++pos[0]) == 'e') {
                // we were correct
                return true;
            } else {
                throw new RuntimeException("Invalid JSON: " + buffer.toString());
            }
        }
        if (b == 'f') {
            // should be false
            if (buffer.getByte(++pos[0]) == 'a' && buffer.getByte(++pos[0]) == 'l' && buffer.getByte(++pos[0]) == 's' && buffer.getByte(++pos[0]) == 'e') {
                // we were correct
                return false;
            } else {
                throw new RuntimeException("Invalid JSON: " + buffer.toString());
            }
        }
        // +/-/digit -> number
        // " -> string
        if (b == '"') {
            // should be string
            return decodeString(buffer, pos);
        }
        if (b == '[') {
            return decodeList(buffer, pos);
        }
        if (b == '{') {
            return decodeMap(buffer, pos);
        }

        throw new RuntimeException("Invalid JSON: " + buffer.toString());
    }
}

package com.jetdrone.vertx.xson.core;

import com.jetdrone.vertx.xson.core.impl.LE;
import com.jetdrone.vertx.xson.java.bson.Key;
import com.jetdrone.vertx.xson.java.bson.MD5;
import com.jetdrone.vertx.xson.java.bson.ObjectId;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.EncodeException;

import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;

public abstract class BSON {

    private static final byte FLOAT = (byte) 0x01;
    private static final byte STRING = (byte) 0x02;
    private static final byte EMBEDDED_DOCUMENT = (byte) 0x03;
    private static final byte ARRAY = (byte) 0x04;
    private static final byte BINARY = (byte) 0x05;
    private static final byte BINARY_BINARY = (byte) 0x00;
    private static final byte BINARY_FUNCTION = (byte) 0x01;
    @Deprecated
    private static final byte BINARY_BINARY_OLD = (byte) 0x02;
    @Deprecated
    private static final byte BINARY_UUID_OLD = (byte) 0x03;
    private static final byte BINARY_UUID = (byte) 0x04;
    private static final byte BINARY_MD5 = (byte) 0x05;
    private static final byte BINARY_USERDEFINED = (byte) 0x80;
    @Deprecated
    private static final byte UNDEFINED = (byte) 0x06;
    private static final byte OBJECT_ID = (byte) 0x07;
    private static final byte BOOLEAN = (byte) 0x08;
    private static final byte UTC_DATETIME = (byte) 0x09;
    private static final byte NULL = (byte) 0x0A;
    private static final byte REGEX = (byte) 0x0B;
    @Deprecated
    private static final byte DBPOINTER = (byte) 0x0C;
    private static final byte JSCODE = (byte) 0x0D;
    @Deprecated
    private static final byte SYMBOL = (byte) 0x0E;
    private static final byte JSCODE_WS = (byte) 0x0F;
    private static final byte INT32 = (byte) 0x10;
    private static final byte TIMESTAMP = (byte) 0x11;
    private static final byte INT64 = (byte) 0x12;
    private static final byte MINKEY = (byte) 0xFF;
    private static final byte MAXKEY = (byte) 0x7F;

    public static void addSerializer(final Object serializer) {
        // Serialize Custom Types
        throw new RuntimeException("Not Implemented!");
    }

    public static Buffer encode(Map<String, ?> jsObject) {
        Buffer buffer = new Buffer();
        // allocate space for the document length
        LE.appendInt(buffer, 0);

        for (Map.Entry<String, ?> entry : jsObject.entrySet()) {
            encode(buffer, entry.getKey(), entry.getValue());
        }

        LE.setInt(buffer, 0, buffer.length() + 1);
        LE.appendByte(buffer, (byte) 0x00);
        return buffer;
    }

    // While JSON allows both Object or Array as top level object, BSON only allows Object
    private static Buffer encode(List<?> list) {
        Buffer buffer = new Buffer();
        // allocate space for the document length
        LE.appendInt(buffer, 0);

        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);
            encode(buffer, String.valueOf(i), value);
        }

        LE.setInt(buffer, 0, buffer.length() + 1);
        LE.appendByte(buffer, (byte) 0x00);
        return buffer;
    }

    public static Map<String, ?> decode(Buffer source) {
        if (source == null) {
            return null;
        }
        return decodeMap(source, 0);
    }

    @SuppressWarnings("unchecked")
    private static void encode(Buffer buffer, String key, Object value) {
        if (value == null) {
            LE.appendByte(buffer, NULL);
            LE.appendCString(buffer, key);
        } else if (value instanceof Double) {
            LE.appendByte(buffer, FLOAT);
            LE.appendCString(buffer, key);
            LE.appendDouble(buffer, (Double) value);
        } else if (value instanceof String) {
            LE.appendByte(buffer, STRING);
            LE.appendCString(buffer, key);
            LE.appendString(buffer, (String) value);
        } else if (value instanceof Map) {
            LE.appendByte(buffer, EMBEDDED_DOCUMENT);
            LE.appendCString(buffer, key);
            buffer.appendBuffer(encode((Map<String, ?>) value));
        } else if (value instanceof List) {
            LE.appendByte(buffer, ARRAY);
            LE.appendCString(buffer, key);
            buffer.appendBuffer(encode((List<?>) value));
        } else if (value instanceof UUID) {
            LE.appendByte(buffer, BINARY);
            LE.appendCString(buffer, key);
            // append length
            LE.appendInt(buffer, 16);
            LE.appendByte(buffer, BINARY_UUID);
            // append data
            UUID uuid = (UUID) value;
            buffer.appendLong(uuid.getMostSignificantBits());
            buffer.appendLong(uuid.getLeastSignificantBits());
        } else if (value instanceof byte[]) {
            LE.appendByte(buffer, BINARY);
            LE.appendCString(buffer, key);
            // append length
            byte[] data = (byte[]) value;
            LE.appendInt(buffer, data.length);
            LE.appendByte(buffer, BINARY_BINARY);
            // append data
            LE.appendBytes(buffer, data);
        } else if (value instanceof Buffer) {
            LE.appendByte(buffer, BINARY);
            LE.appendCString(buffer, key);
            // append length
            LE.appendInt(buffer, ((Buffer) value).length());
            LE.appendByte(buffer, BINARY_USERDEFINED);
            // append data
            buffer.appendBuffer((Buffer) value);
        } else if (value instanceof MD5) {
            LE.appendByte(buffer, BINARY);
            LE.appendCString(buffer, key);
            // append length
            byte[] data = ((MD5) value).getHash();
            LE.appendInt(buffer, data.length);
            LE.appendByte(buffer, BINARY_MD5);
            // append data
            LE.appendBytes(buffer, data);
        } else if (value instanceof ObjectId) {
            LE.appendByte(buffer, OBJECT_ID);
            LE.appendCString(buffer, key);
            LE.appendBytes(buffer, ((ObjectId) value).getBytes());
        } else if (value instanceof Boolean) {
            LE.appendByte(buffer, BOOLEAN);
            LE.appendCString(buffer, key);
            LE.appendBoolean(buffer, (Boolean) value);
        } else if (value instanceof Date) {
            if (value instanceof Timestamp) {
                LE.appendByte(buffer, TIMESTAMP);
                LE.appendCString(buffer, key);
                LE.appendLong(buffer, ((Date) value).getTime());
            } else {
                LE.appendByte(buffer, UTC_DATETIME);
                LE.appendCString(buffer, key);
                LE.appendLong(buffer, ((Date) value).getTime());
            }
        } else if (value instanceof Pattern) {
            LE.appendByte(buffer, REGEX);
            LE.appendCString(buffer, key);
            Pattern pattern = (Pattern) value;
            LE.appendCString(buffer, pattern.pattern());
            int iFlags = pattern.flags();
            StringBuilder flags = new StringBuilder();
            if ((iFlags & Pattern.CASE_INSENSITIVE) == Pattern.CASE_INSENSITIVE) {
                flags.append('i');
            }
            if ((iFlags & Pattern.MULTILINE) == Pattern.MULTILINE) {
                flags.append('m');
            }
            if ((iFlags & Pattern.DOTALL) == Pattern.DOTALL) {
                flags.append('s');
            }
            if ((iFlags & Pattern.UNICODE_CASE) == Pattern.UNICODE_CASE) {
                flags.append('u');
            }
            if ((iFlags & Pattern.COMMENTS) == Pattern.COMMENTS) {
                flags.append('x');
            }
            if ((iFlags & Pattern.UNICODE_CHARACTER_CLASS) == Pattern.UNICODE_CHARACTER_CLASS) {
                flags.append('l');
            }
            LE.appendCString(buffer, flags.toString());
        } else if (value instanceof Integer) {
            LE.appendByte(buffer, INT32);
            LE.appendCString(buffer, key);
            LE.appendInt(buffer, (Integer) value);
        } else if (value instanceof Long) {
            LE.appendByte(buffer, INT64);
            LE.appendCString(buffer, key);
            LE.appendLong(buffer, (Long) value);
        } else if (value instanceof Key) {
            if (value == Key.MIN) {
                LE.appendByte(buffer, MINKEY);
                LE.appendCString(buffer, key);
            } else if (value == Key.MAX) {
                LE.appendByte(buffer, MAXKEY);
                LE.appendCString(buffer, key);
            } else {
                throw new EncodeException("Don't know how to encodeObject: " + value);
            }
        } else {
            // TODO: JSON.js does not throw exception but ignores the value, should we do the same?
            throw new EncodeException("Don't know how to encode: " + value.getClass().getName());
        }
    }

    private static Map<String, ?> decodeMap(Buffer buffer, int pos) {

        // skip the last 0x00
        int length = pos + LE.getInt(buffer, pos) - 1;
        pos += 4;

        Map<String, Object> document = new HashMap<>();

        while (pos < length) {
            // get type
            byte type = LE.getByte(buffer, pos);
            pos++;
            String key = LE.getCString(buffer, pos);
            pos += key.length() + 1;

            switch (type) {
                case FLOAT:
                    document.put(key, LE.getDouble(buffer, pos));
                    pos += 8;
                    break;
                case STRING:
                    int utfLength = LE.getInt(buffer, pos);
                    pos += 4;
                    document.put(key, LE.getString(buffer, pos, utfLength - 1));
                    pos += utfLength;
                    break;
                case EMBEDDED_DOCUMENT:
                    int docLen = LE.getInt(buffer, pos);
                    document.put(key, decodeMap(buffer, pos));
                    pos += docLen;
                    break;
                case ARRAY:
                    int arrLen = LE.getInt(buffer, pos);
                    document.put(key, decodeList(buffer, pos));
                    pos += arrLen;
                    break;
                case BINARY:
                    int binLen = LE.getInt(buffer, pos);
                    pos += 4;
                    byte bintype = LE.getByte(buffer, pos);
                    pos++;
                    switch (bintype) {
                        case BINARY_BINARY:
                            document.put(key, LE.getBytes(buffer, pos, binLen));
                            pos += binLen;
                            break;
                        case BINARY_FUNCTION:
                            throw new DecodeException("Not Implemented");
                        case BINARY_BINARY_OLD:
                            int oldBinLen = LE.getInt(buffer, pos);
                            pos += 4;
                            document.put(key, LE.getBytes(buffer, pos, oldBinLen));
                            pos += binLen;
                            break;
                        case BINARY_UUID_OLD:
                            throw new DecodeException("Not Implemented");
                        case BINARY_UUID:
                            long mostSignificantBits = buffer.getLong(pos);
                            pos += 8;
                            long leastSignificantBits = buffer.getLong(pos);
                            pos += 8;
                            document.put(key, new UUID(mostSignificantBits, leastSignificantBits));
                            break;
                        case BINARY_MD5:
                            final byte[] md5 = LE.getBytes(buffer, pos, binLen);
                            document.put(key, new MD5() {
                                @Override
                                public byte[] getHash() {
                                    return md5;
                                }
                            });
                            pos += binLen;
                            break;
                        case BINARY_USERDEFINED:
                            document.put(key, buffer.getBuffer(pos, pos + binLen));
                            pos += binLen;
                            break;
                    }
                    break;
                case UNDEFINED:
                    // undefined has no meaning in Java, so treat it as a NO-OP
                    break;
                case OBJECT_ID:
                    document.put(key, new ObjectId(LE.getBytes(buffer, pos, 12)));
                    pos += 12;
                    break;
                case BOOLEAN:
                    document.put(key, LE.getBoolean(buffer, pos));
                    pos++;
                    break;
                case UTC_DATETIME:
                    document.put(key, new Date(LE.getLong(buffer, pos)));
                    pos += 8;
                    break;
                case NULL:
                    document.put(key, null);
                    break;
                case REGEX:
                    String regex = LE.getCString(buffer, pos);
                    pos += regex.length() + 1;
                    String options = LE.getCString(buffer, pos);
                    pos += options.length() + 1;

                    int flags = 0;
                    for (int i = 0; i < options.length(); i++) {
                        if (options.charAt(i) == 'i') {
                            flags |= Pattern.CASE_INSENSITIVE;
                            continue;
                        }
                        if (options.charAt(i) == 'm') {
                            flags |= Pattern.MULTILINE;
                            continue;
                        }
                        if (options.charAt(i) == 's') {
                            flags |= Pattern.DOTALL;
                            continue;
                        }
                        if (options.charAt(i) == 'u') {
                            flags |= Pattern.UNICODE_CASE;
                            continue;
                        }
                        if (options.charAt(i) == 'x') {
                            flags |= Pattern.COMMENTS;
                            continue;
                        }
                        if (options.charAt(i) == 'l') {
                            flags |= Pattern.UNICODE_CHARACTER_CLASS;
                        }
                    }
                    document.put(key, Pattern.compile(regex, flags));
                    break;
                case DBPOINTER:
                case JSCODE:
                case SYMBOL:
                case JSCODE_WS:
                    throw new DecodeException("Not Implemented");
                case INT32:
                    document.put(key, LE.getInt(buffer, pos));
                    pos += 4;
                    break;
                case TIMESTAMP:
                    document.put(key, new Timestamp(LE.getLong(buffer, pos)));
                    pos += 8;
                    break;
                case INT64:
                    document.put(key, LE.getLong(buffer, pos));
                    pos += 8;
                    break;
                case MINKEY:
                    document.put(key, Key.MIN);
                    break;
                case MAXKEY:
                    document.put(key, Key.MAX);
                    break;
            }
        }

        return document;
    }

    private static List<?> decodeList(Buffer buffer, int pos) {
        // skip the last 0x00
        int length = pos + LE.getInt(buffer, pos) - 1;
        pos += 4;

        List<Object> list = new LinkedList<>();

        while (pos < length) {
            // get type
            byte type = LE.getByte(buffer, pos);
            pos++;
            String key = LE.getCString(buffer, pos);
            pos += key.length() + 1;

            switch (type) {
                case FLOAT:
                    list.add(Integer.parseInt(key), LE.getDouble(buffer, pos));
                    pos += 8;
                    break;
                case STRING:
                    int utfLength = LE.getInt(buffer, pos);
                    pos += 4;
                    list.add(Integer.parseInt(key), LE.getString(buffer, pos, utfLength - 1));
                    pos += utfLength;
                    break;
                case EMBEDDED_DOCUMENT:
                    int docLen = LE.getInt(buffer, pos);
                    list.add(Integer.parseInt(key), decodeMap(buffer, pos));
                    pos += docLen;
                    break;
                case ARRAY:
                    int arrLen = LE.getInt(buffer, pos);
                    list.add(Integer.parseInt(key), decodeList(buffer, pos));
                    pos += arrLen;
                    break;
                case BINARY:
                    int binLen = LE.getInt(buffer, pos);
                    pos += 4;
                    byte bintype = LE.getByte(buffer, pos);
                    pos++;
                    switch (bintype) {
                        case BINARY_BINARY:
                            list.add(Integer.parseInt(key), LE.getBytes(buffer, pos, binLen));
                            pos += binLen;
                            break;
                        case BINARY_FUNCTION:
                            throw new DecodeException("Not Implemented");
                        case BINARY_BINARY_OLD:
                            int oldBinLen = LE.getInt(buffer, pos);
                            pos += 4;
                            list.add(Integer.parseInt(key), LE.getBytes(buffer, pos, oldBinLen));
                            pos += binLen;
                            break;
                        case BINARY_UUID_OLD:
                            throw new DecodeException("Not Implemented");
                        case BINARY_UUID:
                            long mostSingnificantBits = buffer.getLong(pos);
                            pos += 8;
                            long leastSingnificantBits = buffer.getLong(pos);
                            pos += 8;
                            list.add(Integer.parseInt(key), new UUID(mostSingnificantBits, leastSingnificantBits));
                            break;
                        case BINARY_MD5:
                            final byte[] md5 = LE.getBytes(buffer, pos, binLen);
                            list.add(Integer.parseInt(key), new MD5() {
                                @Override
                                public byte[] getHash() {
                                    return md5;
                                }
                            });
                            pos += binLen;
                            break;
                        case BINARY_USERDEFINED:
                            list.add(Integer.parseInt(key), buffer.getBuffer(pos, pos + binLen));
                            pos += binLen;
                            break;
                    }
                    break;
                case UNDEFINED:
                    // undefined has no meaning in Java, so treat it as a NO-OP
                    break;
                case OBJECT_ID:
                    list.add(Integer.parseInt(key), new ObjectId(LE.getBytes(buffer, pos, 12)));
                    pos += 12;
                    break;
                case BOOLEAN:
                    list.add(Integer.parseInt(key), LE.getBoolean(buffer, pos));
                    pos++;
                    break;
                case UTC_DATETIME:
                    list.add(Integer.parseInt(key), new Date(LE.getLong(buffer, pos)));
                    pos += 8;
                    break;
                case NULL:
                    list.add(Integer.parseInt(key), null);
                    break;
                case REGEX:
                    String regex = LE.getCString(buffer, pos);
                    pos += regex.length() + 1;
                    String options = LE.getCString(buffer, pos);
                    pos += options.length() + 1;

                    int flags = 0;
                    for (int i = 0; i < options.length(); i++) {
                        if (options.charAt(i) == 'i') {
                            flags |= Pattern.CASE_INSENSITIVE;
                            continue;
                        }
                        if (options.charAt(i) == 'm') {
                            flags |= Pattern.MULTILINE;
                            continue;
                        }
                        if (options.charAt(i) == 's') {
                            flags |= Pattern.DOTALL;
                            continue;
                        }
                        if (options.charAt(i) == 'u') {
                            flags |= Pattern.UNICODE_CASE;
                            continue;
                        }
                        if (options.charAt(i) == 'x') {
                            flags |= Pattern.COMMENTS;
                            continue;
                        }
                        if (options.charAt(i) == 'l') {
                            flags |= Pattern.UNICODE_CHARACTER_CLASS;
                        }
                    }
                    list.add(Integer.parseInt(key), Pattern.compile(regex, flags));
                    break;
                case DBPOINTER:
                case JSCODE:
                case SYMBOL:
                case JSCODE_WS:
                    throw new DecodeException("Not Implemented");
                case INT32:
                    list.add(Integer.parseInt(key), LE.getInt(buffer, pos));
                    pos += 4;
                    break;
                case TIMESTAMP:
                    list.add(Integer.parseInt(key), new Timestamp(LE.getLong(buffer, pos)));
                    pos += 8;
                    break;
                case INT64:
                    list.add(Integer.parseInt(key), LE.getLong(buffer, pos));
                    pos += 8;
                    break;
                case MINKEY:
                    list.add(Integer.parseInt(key), Key.MIN);
                    break;
                case MAXKEY:
                    list.add(Integer.parseInt(key), Key.MAX);
                    break;
            }
        }

        return list;
    }
}
package com.jetdrone.vertx.mods.bson;

import org.vertx.java.core.buffer.Buffer;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;

public final class BSON {

    private static class Encoder {
        final String name;
        final Field field;

        Encoder(Field field) {
            this.name = field.getName();
            this.field = field;
        }
    }

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

    private static final Map<Class<? extends BSONObject>, List<Encoder>> COMPILERS = new IdentityHashMap<>();

    public static void compile(Class<? extends BSONObject> clazz) {
        List<Encoder> encoders = new ArrayList<>();
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(BSONElement.class)) {
                // TODO: verify the field type for supported types field.getType();
                encoders.add(new Encoder(field));
            }
        }
        COMPILERS.put(clazz, encoders);
    }

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
            buffer.appendBuffer(encode((Map) value));
        } else if (value instanceof List) {
            LE.appendByte(buffer, ARRAY);
            LE.appendCString(buffer, key);
            buffer.appendBuffer(encode((List) value));
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
        } else if (value instanceof Binary) {
            LE.appendByte(buffer, BINARY);
            LE.appendCString(buffer, key);
            // append length
            byte[] data = ((Binary) value).getBytes();
            LE.appendInt(buffer, data.length);
            LE.appendByte(buffer, BINARY_USERDEFINED);
            // append data
            LE.appendBytes(buffer, data);
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
            // TODO: convert flags to BSON flags x,l
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
                throw new RuntimeException("Don't know how to encode: " + value);
            }
        } else if (value instanceof BSONObject) {
            LE.appendByte(buffer, EMBEDDED_DOCUMENT);
            LE.appendCString(buffer, key);
            buffer.appendBuffer(encode((BSONObject) value));
        } else {
            throw new RuntimeException("Don't know how to encode: " + value);
        }
    }

    public static Buffer encode(Map map) {
        Buffer buffer = new Buffer();
        // allocate space for the document length
        LE.appendInt(buffer, 0);

        for (Object entry : map.entrySet()) {
            Map.Entry entrySet = (Map.Entry) entry;
            Object key = entrySet.getKey();
            if (!(key instanceof String)) {
                throw new RuntimeException("BSON only allows CString as key");
            }
            Object value = entrySet.getValue();
            encode(buffer, (String) key, value);
        }

        LE.setInt(buffer, 0, buffer.length() + 1);
        LE.appendByte(buffer, (byte) 0x00);
        return buffer;
    }

    public static Buffer encode(BSONObject bson) {
        // find the right compiler
        List<Encoder> encoders = COMPILERS.get(bson.getClass());

        if (encoders == null) {
            compile(bson.getClass());
            encoders = COMPILERS.get(bson.getClass());
        }

        Buffer buffer = new Buffer();
        // allocate space for the document length
        LE.appendInt(buffer, 0);

        for (Encoder encoder : encoders) {
            try {
                encode(buffer, encoder.name, encoder.field.get(bson));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        LE.appendByte(buffer, (byte) 0x00);
        LE.setInt(buffer, 0, buffer.length());
        return buffer;
    }

    private static Buffer encode(List list) {
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

    public static Map<String, Object> decode(Buffer buffer) {
        return decodeDocument(buffer, 0);
    }

    private static Map<String, Object> decodeDocument(Buffer buffer, int pos) {

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
                    document.put(key, decodeDocument(buffer, pos));
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
                            throw new RuntimeException("Not Implemented");
                        case BINARY_BINARY_OLD:
                            int oldBinLen = LE.getInt(buffer, pos);
                            pos += 4;
                            document.put(key, LE.getBytes(buffer, pos, oldBinLen));
                            pos += binLen;
                            break;
                        case BINARY_UUID_OLD:
                            throw new RuntimeException("Not Implemented");
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
                            final byte[] userdef = LE.getBytes(buffer, pos, binLen);
                            document.put(key, new Binary() {
                                @Override
                                public byte[] getBytes() {
                                    return userdef;
                                }
                            });
                            pos += binLen;
                            break;
                    }
                    break;
                case UNDEFINED:
                    throw new RuntimeException("Not Implemented");
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
                        // TODO: convert flags to BSON flags x,l
                    }
                    document.put(key, Pattern.compile(regex, flags));
                    break;
                case DBPOINTER:
                case JSCODE:
                case SYMBOL:
                case JSCODE_WS:
                    throw new RuntimeException("Not Implemented");
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

    private static List<Object> decodeList(Buffer buffer, int pos) {
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
                    list.add(Integer.parseInt(key), decodeDocument(buffer, pos));
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
                        case BINARY_BINARY_OLD:
                        case BINARY_UUID_OLD:
                            throw new RuntimeException("Not Implemented");
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
                            final byte[] userdef = LE.getBytes(buffer, pos, binLen);
                            list.add(Integer.parseInt(key), new Binary() {
                                @Override
                                public byte[] getBytes() {
                                    return userdef;
                                }
                            });
                            pos += binLen;
                            break;
                    }
                    break;
                case UNDEFINED:
                    throw new RuntimeException("Not Implemented");
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
                        // TODO: convert flags to BSON flags x,l
                    }
                    list.add(Integer.parseInt(key), Pattern.compile(regex, flags));
                    break;
                case DBPOINTER:
                case JSCODE:
                case SYMBOL:
                case JSCODE_WS:
                    throw new RuntimeException("Not Implemented");
                case INT32:
                    list.add(Integer.parseInt(key), LE.getInt(buffer, pos));
                    pos += 4;
                    break;
                case TIMESTAMP:
                    throw new RuntimeException("Not Implemented");
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
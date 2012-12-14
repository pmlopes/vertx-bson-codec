package bson.vertx.eventbus;

import bson.vertx.Binary;
import bson.vertx.Key;
import bson.vertx.ObjectId;
import org.vertx.java.core.buffer.Buffer;

import java.util.*;
import java.util.regex.Pattern;

import static bson.vertx.eventbus.LE.*;

final class BSONCodec {

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

    private static void encodeType(Buffer buffer, byte type, String key) {
        appendByte(buffer, type);
        appendCString(buffer, key);
    }

    private static void encode(Buffer buffer, String key, Object value) {
        if (value == null) {
            encodeType(buffer, NULL, key);
        } else if (value instanceof Double) {
            encodeType(buffer, FLOAT, key);
            appendDouble(buffer, (Double) value);
        } else if (value instanceof String) {
            encodeType(buffer, STRING, key);
            appendString(buffer, (String) value);
        } else if (value instanceof Map) {
            encodeType(buffer, EMBEDDED_DOCUMENT, key);
            buffer.appendBuffer(encode((Map) value));
        } else if (value instanceof List) {
            encodeType(buffer, ARRAY, key);
            buffer.appendBuffer(encode((List) value));
        } else if (value instanceof UUID) {
            encodeType(buffer, BINARY, key);
            // append length
            appendInt(buffer, 16);
            appendByte(buffer, BINARY_UUID);
            // append data
            UUID uuid = (UUID) value;
            buffer.appendLong(uuid.getMostSignificantBits());
            buffer.appendLong(uuid.getLeastSignificantBits());
        } else if (value instanceof byte[]) {
            encodeType(buffer, BINARY, key);
            // append length
            byte[] data = (byte[]) value;
            appendInt(buffer, data.length);
            appendByte(buffer, BINARY_BINARY);
            // append data
            appendBytes(buffer, data);
        } else if (value instanceof Binary) {
            encodeType(buffer, BINARY, key);
            // append length
            byte[] data = ((Binary) value).getBytes();
            appendInt(buffer, data.length);
            appendByte(buffer, BINARY_USERDEFINED);
            // append data
            appendBytes(buffer, data);
        }
//            if (value instanceof ) {
//                encodeType(buffer, UNDEFINED, key);
//            }
        else if (value instanceof ObjectId) {
            encodeType(buffer, OBJECT_ID, key);
            appendBytes(buffer, ((ObjectId) value).getBytes());
        } else if (value instanceof Boolean) {
            encodeType(buffer, BOOLEAN, key);
            appendBoolean(buffer, (Boolean) value);
        } else if (value instanceof Date) {
            encodeType(buffer, UTC_DATETIME, key);
            appendLong(buffer, ((Date) value).getTime());
        } else if (value instanceof Pattern) {
            encodeType(buffer, REGEX, key);
            Pattern pattern = (Pattern) value;
            appendCString(buffer, pattern.pattern());
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
            appendCString(buffer, flags.toString());
        }
//            if (value instanceof JSCode) {
//                encodeType(buffer, JSCODE, key);
//                continue;
//            }
//            if (value instanceof JSCodeWS) {
//                encodeType(buffer, JSCODE_WS, key);
//                continue;
//            }
        else if (value instanceof Integer) {
            encodeType(buffer, INT32, key);
            appendInt(buffer, (Integer) value);
        }
//            if (value instanceof Timestamp) {
//                encodeType(buffer, TIMESTAMP, key);
//                continue;
//            }
        else if (value instanceof Long) {
            encodeType(buffer, INT64, key);
            appendLong(buffer, (Long) value);
        } else if (value instanceof Key) {
            if (value == Key.MIN) {
                encodeType(buffer, MINKEY, key);
            } else if (value == Key.MAX) {
                encodeType(buffer, MAXKEY, key);
            } else {
                throw new RuntimeException("Dont know how to encode: " + value);
            }
        } else {
            throw new RuntimeException("Dont know how to encode: " + value);
        }
    }

    public static Buffer encode(Map map) {
        Buffer buffer = new Buffer();
        // allocate space for the document length
        appendInt(buffer, 0);

        for (Object entry : map.entrySet()) {
            Map.Entry entrySet = (Map.Entry) entry;
            Object key = entrySet.getKey();
            if (!(key instanceof String)) {
                throw new RuntimeException("BSON only allows CString as key");
            }
            Object value = entrySet.getValue();
            encode(buffer, (String) key, value);
        }

        setInt(buffer, 0, buffer.length() + 1);
        appendByte(buffer, (byte) 0x00);
        return buffer;
    }

    public static Buffer encode(List list) {
        Buffer buffer = new Buffer();
        // allocate space for the document length
        appendInt(buffer, 0);

        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);
            encode(buffer, String.valueOf(i), value);
        }

        setInt(buffer, 0, buffer.length() + 1);
        appendByte(buffer, (byte) 0x00);
        return buffer;
    }

    public static Map<String, Object> decode(Buffer buffer) {
        return decodeDocument(buffer, 0);
    }

    private static Map<String, Object> decodeDocument(Buffer buffer, int pos) {

        // skip the last 0x00
        int length = pos + getInt(buffer, pos) - 1;
        pos += 4;

        Map<String, Object> document = new HashMap<>();

        while (pos < length) {
            // get type
            byte type = getByte(buffer, pos);
            pos++;
            String key = getCString(buffer, pos);
            pos += key.length() + 1;

            switch (type) {
                case FLOAT:
                    document.put(key, getDouble(buffer, pos));
                    pos += 8;
                    break;
                case STRING:
                    int utfLength = getInt(buffer, pos);
                    pos += 4;
                    document.put(key, getString(buffer, pos, utfLength - 1));
                    pos += utfLength;
                    break;
                case EMBEDDED_DOCUMENT:
                    int docLen = getInt(buffer, pos);
                    document.put(key, decodeDocument(buffer, pos));
                    pos += docLen;
                    break;
                case ARRAY:
                    int arrLen = getInt(buffer, pos);
                    document.put(key, decodeList(buffer, pos));
                    pos += arrLen;
                    break;
                case BINARY:
                    int binLen = getInt(buffer, pos);
                    pos += 4;
                    byte bintype = getByte(buffer, pos);
                    pos++;
                    switch (bintype) {
                        case BINARY_BINARY:
                            document.put(key, getBytes(buffer, pos, binLen));
                            pos += binLen;
                            break;
                        case BINARY_FUNCTION:
                            throw new RuntimeException("Not Implemented");
                        case BINARY_BINARY_OLD:
                            int oldBinLen = getInt(buffer, pos);
                            pos += 4;
                            document.put(key, getBytes(buffer, pos, oldBinLen));
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
                            throw new RuntimeException("Not Implemented");
                        case BINARY_USERDEFINED:
                            final byte[] userdef = getBytes(buffer, pos, binLen);
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
                    document.put(key, new ObjectId(getBytes(buffer, pos, 12)));
                    pos += 12;
                    break;
                case BOOLEAN:
                    document.put(key, getBoolean(buffer, pos));
                    pos++;
                    break;
                case UTC_DATETIME:
                    document.put(key, new Date(getLong(buffer, pos)));
                    pos += 8;
                    break;
                case NULL:
                    document.put(key, null);
                    break;
                case REGEX:
                    String regex = getCString(buffer, pos);
                    pos += regex.length() + 1;
                    String options = getCString(buffer, pos);
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
                    document.put(key, getInt(buffer, pos));
                    pos += 4;
                    break;
                case TIMESTAMP:
                    throw new RuntimeException("Not Implemented");
                case INT64:
                    document.put(key, getLong(buffer, pos));
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
        int length = pos + getInt(buffer, pos) - 1;
        pos += 4;

        List<Object> list = new LinkedList<>();

        while (pos < length) {
            // get type
            byte type = getByte(buffer, pos);
            pos++;
            String key = getCString(buffer, pos);
            pos += key.length() + 1;

            switch (type) {
                case FLOAT:
                    list.add(Integer.parseInt(key), getDouble(buffer, pos));
                    pos += 8;
                    break;
                case STRING:
                    int utfLength = getInt(buffer, pos);
                    pos += 4;
                    list.add(Integer.parseInt(key), getString(buffer, pos, utfLength - 1));
                    pos += utfLength;
                    break;
                case EMBEDDED_DOCUMENT:
                    int docLen = getInt(buffer, pos);
                    list.add(Integer.parseInt(key), decodeDocument(buffer, pos));
                    pos += docLen;
                    break;
                case ARRAY:
                    int arrLen = getInt(buffer, pos);
                    list.add(Integer.parseInt(key), decodeList(buffer, pos));
                    pos += arrLen;
                    break;
                case BINARY:
                    int binLen = getInt(buffer, pos);
                    pos += 4;
                    byte bintype = getByte(buffer, pos);
                    pos++;
                    switch (bintype) {
                        case BINARY_BINARY:
                            list.add(Integer.parseInt(key), getBytes(buffer, pos, binLen));
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
                        case BINARY_USERDEFINED:
                            throw new RuntimeException("Not Implemented");
                    }
                    break;
                case UNDEFINED:
                    throw new RuntimeException("Not Implemented");
                case OBJECT_ID:
                    list.add(Integer.parseInt(key), new ObjectId(getBytes(buffer, pos, 12)));
                    pos += 12;
                    break;
                case BOOLEAN:
                    list.add(Integer.parseInt(key), getBoolean(buffer, pos));
                    pos++;
                    break;
                case UTC_DATETIME:
                    list.add(Integer.parseInt(key), new Date(getLong(buffer, pos)));
                    pos += 8;
                    break;
                case NULL:
                    list.add(Integer.parseInt(key), null);
                    break;
                case REGEX:
                    String regex = getCString(buffer, pos);
                    pos += regex.length() + 1;
                    String options = getCString(buffer, pos);
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
                    list.add(Integer.parseInt(key), getInt(buffer, pos));
                    pos += 4;
                    break;
                case TIMESTAMP:
                    throw new RuntimeException("Not Implemented");
                case INT64:
                    list.add(Integer.parseInt(key), getLong(buffer, pos));
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
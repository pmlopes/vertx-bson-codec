package com.jetdrone.bson.vertx.eventbus;

import com.jetdrone.bson.BSONElement;
import com.jetdrone.bson.BSONObject;
import com.jetdrone.bson.vertx.Binary;
import com.jetdrone.bson.vertx.Key;
import com.jetdrone.bson.vertx.MD5;
import com.jetdrone.bson.vertx.ObjectId;
import org.vertx.java.core.buffer.Buffer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;

import static com.jetdrone.bson.vertx.eventbus.LE.*;

public class BSONCompiler {

    private static final byte FLOAT = (byte) 0x01;
    private static final byte STRING = (byte) 0x02;
    private static final byte EMBEDDED_DOCUMENT = (byte) 0x03;
    private static final byte ARRAY = (byte) 0x04;
    private static final byte BINARY = (byte) 0x05;
    private static final byte BINARY_BINARY = (byte) 0x00;
    private static final byte BINARY_UUID = (byte) 0x04;
    private static final byte BINARY_MD5 = (byte) 0x05;
    private static final byte BINARY_USERDEFINED = (byte) 0x80;
    private static final byte OBJECT_ID = (byte) 0x07;
    private static final byte BOOLEAN = (byte) 0x08;
    private static final byte UTC_DATETIME = (byte) 0x09;
    private static final byte NULL = (byte) 0x0A;
    private static final byte REGEX = (byte) 0x0B;
    private static final byte INT32 = (byte) 0x10;
    private static final byte TIMESTAMP = (byte) 0x11;
    private static final byte INT64 = (byte) 0x12;
    private static final byte MINKEY = (byte) 0xFF;
    private static final byte MAXKEY = (byte) 0x7F;

    private static void encodeFloat(Buffer buffer, String key, Double value) {
        appendByte(buffer, FLOAT);
        appendCString(buffer, key);
        appendDouble(buffer, value);
    }

    private static void encodeString(Buffer buffer, String key, String value) {
        appendByte(buffer, STRING);
        appendCString(buffer, key);
        appendString(buffer, value);
    }

    private static void encodeEmbeddedDocument(Buffer buffer, String key, BSONObject document) throws InvocationTargetException, IllegalAccessException {
        appendByte(buffer, EMBEDDED_DOCUMENT);
        appendCString(buffer, key);
        buffer.appendBuffer(serialize(document));
    }

    private static void encodeArray(Buffer buffer, String key, List array) {
        appendByte(buffer, ARRAY);
        appendCString(buffer, key);
    }

    private static void encodeBinaryBinary(Buffer buffer, String key, byte[] binary) {
        appendByte(buffer, BINARY);
        appendCString(buffer, key);
        // append length
        appendInt(buffer, binary.length);
        appendByte(buffer, BINARY_BINARY);
        // append data
        appendBytes(buffer, binary);
    }

    private static void encodeBinaryUUID(Buffer buffer, String key, UUID uuid) {
        appendByte(buffer, BINARY);
        appendCString(buffer, key);
        // append length
        appendInt(buffer, 16);
        appendByte(buffer, BINARY_UUID);
        // append data
        buffer.appendLong(uuid.getMostSignificantBits());
        buffer.appendLong(uuid.getLeastSignificantBits());
    }

    private static void encodeBinaryMD5(StringBuffer buffer, String key, MD5 md5) {
        throw new RuntimeException("Not implemented yet");
    }

    private static void encodeBinaryUserDefined(StringBuffer buffer, String key, Binary binary) {
        throw new RuntimeException("Not implemented yet");
    }

    private static void encodeObjectId(Buffer buffer, String key, ObjectId objectId) {
        appendByte(buffer, OBJECT_ID);
        appendCString(buffer, key);
        appendBytes(buffer, objectId.getBytes());
    }

    private static void encodeBoolean(Buffer buffer, String key, Boolean bool) {
        appendByte(buffer, BOOLEAN);
        appendCString(buffer, key);
        appendBoolean(buffer, bool);
    }

    private static void encodeUTCDatetime(Buffer buffer, String key, Date date) {
        appendByte(buffer, UTC_DATETIME);
        appendCString(buffer, key);
        appendLong(buffer, date.getTime());
    }

    private static void encodeNull(Buffer buffer, String key) {
        appendByte(buffer, NULL);
        appendCString(buffer, key);
    }

    private static void encodeRegex(Buffer buffer, String key, Pattern regex) {
        appendByte(buffer, REGEX);
        appendCString(buffer, key);
        appendCString(buffer, regex.pattern());
        int iFlags = regex.flags();
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

    private static void encodeInt32(Buffer buffer, String key, Integer int32) {
        appendByte(buffer, INT32);
        appendCString(buffer, key);
        appendInt(buffer, int32);
    }

    private static void encodeTimestamp(Buffer buffer, String key, Timestamp timestamp) {
        throw new RuntimeException("Not implemented yet");
    }

    private static void encodeInt64(Buffer buffer, String key, Long int64) {
        appendByte(buffer, INT64);
        appendCString(buffer, key);
        appendLong(buffer, int64);
    }

    private static void encodeMinKey(Buffer buffer, String key) {
        throw new RuntimeException("Not implemented yet");
    }

    private static void encodeMaxKey(Buffer buffer, String key) {
        throw new RuntimeException("Not implemented yet");
    }

    private static final Map<Class, Method> ENCODERS = new HashMap<>();

    static {
        try {
            ENCODERS.put(Double.class, BSONCompiler.class.getDeclaredMethod("encodeFloat", new Class[]{Buffer.class, String.class, Double.class}));
            ENCODERS.put(String.class, BSONCompiler.class.getDeclaredMethod("encodeString", new Class[]{Buffer.class, String.class, String.class}));
            ENCODERS.put(BSONObject.class, BSONCompiler.class.getDeclaredMethod("encodeEmbeddedDocument", new Class[]{Buffer.class, String.class, BSONObject.class}));
            ENCODERS.put(List.class, BSONCompiler.class.getDeclaredMethod("encodeArray", new Class[]{Buffer.class, String.class, List.class}));
            ENCODERS.put(byte[].class, BSONCompiler.class.getDeclaredMethod("encodeBinaryBinary", new Class[]{Buffer.class, String.class, byte[].class}));
            ENCODERS.put(UUID.class, BSONCompiler.class.getDeclaredMethod("encodeBinaryUUID", new Class[]{Buffer.class, String.class, UUID.class}));
            ENCODERS.put(MD5.class, BSONCompiler.class.getDeclaredMethod("encodeBinaryMD5", StringBuffer.class, String.class, MD5.class));
            ENCODERS.put(Binary.class, BSONCompiler.class.getDeclaredMethod("encodeBinaryUserDefined", StringBuffer.class, String.class, Binary.class));
            ENCODERS.put(ObjectId.class, BSONCompiler.class.getDeclaredMethod("encodeObjectId", Buffer.class, String.class, ObjectId.class));
            ENCODERS.put(Boolean.class, BSONCompiler.class.getDeclaredMethod("encodeBoolean", Buffer.class, String.class, Boolean.class));
            ENCODERS.put(Date.class, BSONCompiler.class.getDeclaredMethod("encodeUTCDatetime", Buffer.class, String.class, Date.class));
            ENCODERS.put(null, BSONCompiler.class.getDeclaredMethod("encodeNull", Buffer.class, String.class));
            ENCODERS.put(Pattern.class, BSONCompiler.class.getDeclaredMethod("encodeRegex", Buffer.class, String.class, Pattern.class));
            ENCODERS.put(Integer.class, BSONCompiler.class.getDeclaredMethod("encodeInt32", Buffer.class, String.class, Integer.class));
            ENCODERS.put(Timestamp.class, BSONCompiler.class.getDeclaredMethod("encodeTimestamp", Buffer.class, String.class, Timestamp.class));
            ENCODERS.put(Long.class, BSONCompiler.class.getDeclaredMethod("encodeInt64", Buffer.class, String.class, Long.class));
            ENCODERS.put(Key.class, BSONCompiler.class.getDeclaredMethod("encodeMinKey", Buffer.class, String.class));
            ENCODERS.put(Key.class, BSONCompiler.class.getDeclaredMethod("encodeMaxKey", Buffer.class, String.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    static class Encoder {
        final String name;
        final Field field;
        final Method encoder;

        Encoder(Field field, Method encoder) {
            this.name = field.getName();
            this.field = field;
            this.encoder = encoder;
        }
    }

    private static final Map<Class<? extends BSONObject>, List<Encoder>> COMPILERS = new HashMap<>();

    public static void compile(Class<? extends BSONObject> clazz) {
        List<Encoder> encoders = new ArrayList<>();
        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(BSONElement.class)) {
                Method m = ENCODERS.get(field.getType());
                if (m != null) {
                    encoders.add(new Encoder(field, m));
                } else {
                    // verify if this class implement BSONObject interface
                    Class[] fieldInterfaces = field.getType().getInterfaces();
                    if (fieldInterfaces != null) {
                        for (Class fieldInterface : fieldInterfaces) {
                            if (fieldInterface == BSONObject.class) {
                                m = ENCODERS.get(fieldInterface);
                                encoders.add(new Encoder(field, m));
                                break;
                            }
                        }
                        // TODO: error handling
                    } else {
                        // TODO: error handling
                    }
                }
            }
        }

        COMPILERS.put(clazz, encoders);
    }

    public static Buffer serialize(BSONObject bson) throws IllegalAccessException, InvocationTargetException {
        // find the right compiler
        List<Encoder> encoders = COMPILERS.get(bson.getClass());

        if (encoders == null) {
            throw new RuntimeException("Class " + bson.getClass().getName() + " is not registered in the compiler unit");
        }

        Buffer buffer = new Buffer();
        // allocate space for the document length
        appendInt(buffer, 0);

        for (Encoder encoder : encoders) {
            encoder.encoder.invoke(null, buffer, encoder.name, encoder.field.get(bson));
        }

        setInt(buffer, 0, buffer.length() + 1);
        appendByte(buffer, (byte) 0x00);
        return buffer;
    }

    private static Buffer serialize(List list) {
        Buffer buffer = new Buffer();
        // allocate space for the document length
        appendInt(buffer, 0);

        for (int i = 0; i < list.size(); i++) {
            Object value = list.get(i);
//            encode(buffer, String.valueOf(i), value);
        }

        setInt(buffer, 0, buffer.length() + 1);
        appendByte(buffer, (byte) 0x00);
        return buffer;
    }
}
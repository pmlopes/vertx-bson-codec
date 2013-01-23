package com.jetdrone.bson.vertx.eventbus;

import com.jetdrone.bson.BSONElement;
import com.jetdrone.bson.BSONObject;
import com.jetdrone.bson.vertx.ObjectId;
import org.vertx.java.core.buffer.Buffer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.jetdrone.bson.vertx.eventbus.LE.*;

public class BSONCompiler {

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

    public BSONCompiler(Class<? extends BSONObject> clazz) throws NoSuchMethodException {
        Map<Class, Method> encoderMap = new HashMap<>();

        encoderMap.put(Double.class, this.getClass().getDeclaredMethod("encodeFloat", new Class[]{Buffer.class, String.class, Double.class}) );
        encoderMap.put(String.class, this.getClass().getDeclaredMethod("encodeString", new Class[]{Buffer.class, String.class, String.class}) );
        encoderMap.put(BSONObject.class, this.getClass().getDeclaredMethod("encodeEmbeddedDocument", new Class[]{Buffer.class, String.class, BSONObject.class}) );
        encoderMap.put(List.class, this.getClass().getDeclaredMethod("encodeArray", new Class[]{Buffer.class, String.class, List.class}) );
        encoderMap.put(byte[].class, this.getClass().getDeclaredMethod("encodeBinaryBinary", new Class[]{Buffer.class, String.class, byte[].class}) );
        encoderMap.put(UUID.class, this.getClass().getDeclaredMethod("encodeBinaryUUID", new Class[]{Buffer.class, String.class, UUID.class}) );

        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(BSONElement.class)) {
                Method m = encoderMap.get(field.getType());
                if (m == null) {
                    throw new RuntimeException("Don't know how to encode: " + field.getType().getName());
                }
                encoders.add(new Encoder(field, m));
            }
        }
    }

    // encoders

    private void encodeFloat(Buffer buffer, String key, Double value) {
        appendByte(buffer, FLOAT);
        appendCString(buffer, key);
        appendDouble(buffer, value);
    }

    private void encodeString(Buffer buffer, String key, String value) {
        appendByte(buffer, STRING);
        appendCString(buffer, key);
        appendString(buffer, value);
    }

    private void encodeEmbeddedDocument(Buffer buffer, String key, BSONObject document) {
        throw new RuntimeException("Not implemented yet");
    }

    private void encodeArray(Buffer buffer, String key, List document) {
        throw new RuntimeException("Not implemented yet");
    }

    private void encodeBinaryBinary(Buffer buffer, String key, byte[] binary) {
        appendByte(buffer, BINARY);
        appendCString(buffer, key);
        // append length
        appendInt(buffer, binary.length);
        appendByte(buffer, BINARY_BINARY);
        // append data
        appendBytes(buffer, binary);
    }

    private void encodeBinaryUUID(Buffer buffer, String key, UUID uuid) {
        appendByte(buffer, BINARY);
        appendCString(buffer, key);
        // append length
        appendInt(buffer, 16);
        appendByte(buffer, BINARY_UUID);
        // append data
        buffer.appendLong(uuid.getMostSignificantBits());
        buffer.appendLong(uuid.getLeastSignificantBits());
    }

    private void encodeObjectId(Buffer buffer, String key, ObjectId objectId) {

    }

    private List<Encoder> encoders = new ArrayList<>();

    public Buffer serialize(BSONObject bson) throws IllegalAccessException, InvocationTargetException {
        Buffer buffer = new Buffer();
        // allocate space for the document length
        appendInt(buffer, 0);

        for (Encoder encoder : encoders) {
            encoder.encoder.invoke(this, buffer, encoder.name, encoder.field.get(bson));
        }

        setInt(buffer, 0, buffer.length() + 1);
        appendByte(buffer, (byte) 0x00);
        return buffer;
    }
}

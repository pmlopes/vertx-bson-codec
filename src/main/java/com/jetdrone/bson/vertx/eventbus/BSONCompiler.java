package com.jetdrone.bson.vertx.eventbus;

import com.jetdrone.bson.BSONElement;
import com.jetdrone.bson.BSONObject;
import org.vertx.java.core.buffer.Buffer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private void encodeString(Buffer buffer, String key, String value) {
        appendByte(buffer, STRING);
        appendCString(buffer, key);
        appendString(buffer, value);
    }

    private List<Encoder> encoders = new ArrayList<>();

    public void compile(Class<? extends BSONObject> clazz) throws NoSuchMethodException {

        Map<Class, Method> encoderMap = new HashMap<>();

        encoderMap.put(String.class, BSONCompiler.class.getDeclaredMethod("encodeString", new Class[] {Buffer.class, String.class, String.class}) );

        for (Field field : clazz.getFields()) {
            if (field.isAnnotationPresent(BSONElement.class)) {
                encoders.add(new Encoder(field, encoderMap.get(field.getType())));
            }
        }
    }

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

    public static void main(String[] args) throws Exception {
        BSONCompiler compiler = new BSONCompiler();

        compiler.compile(SimpleBSON.class);

        SimpleBSON bson = new SimpleBSON();
        bson.name = "SimpleBSON";

        Buffer buffer = compiler.serialize(bson);

//        byte[] data = buffer.getBytes();
//        for (byte b : data) {
//            System.out.println(b + " " + (char) b);
//        }
    }
}

class SimpleBSON implements BSONObject {
    @BSONElement
    public String name;
}
package com.jetdrone.vertx.mods.bson;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.json.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import static org.junit.Assert.*;

public class JsonParseTest {

    public static Buffer readResourceToBuffer(Class<?> clazz, String resource) {
        try {
            Buffer buffer = new Buffer(0);

            try (InputStream in = clazz.getResourceAsStream(resource)) {
                int read;
                byte[] data = new byte[4096];
                while ((read = in.read(data, 0, data.length)) != -1) {
                    if (read == data.length) {
                        buffer.appendBytes(data);
                    } else {
                        byte[] slice = new byte[read];
                        System.arraycopy(data, 0, slice, 0, slice.length);
                        buffer.appendBytes(slice);
                    }
                }
            }

            return buffer;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @Test
    public void naiveMicroBenchmarkDecode() {
        Buffer[] files = new Buffer[5];
        String[] strings = new String[5];

        for (int i = 0; i < files.length; i++) {
            files[i] = readResourceToBuffer(this.getClass(), "/file" + (i+1) + ".json");
            strings[i] = files[i].toString();
        }

        int repetitions = 100000;

        long t0 = System.nanoTime();

        for (int i = 0; i < repetitions; i++) {
            for (int j = 0; j < files.length; j++) {
                Object o = JSON.decode(files[j]);
            }
        }

        long t1 = System.nanoTime();

        for (int i = 0; i < repetitions; i++) {
            for (int j = 0; j < strings.length; j++) {
                Object o = new JsonObject(strings[j]);
            }
        }

        long t2 = System.nanoTime();

        double xson = (t1 - t0);
        double json = (t2 - t1);

        assertTrue(xson < json);

        double speedupPerOperation = (xson / (repetitions * files.length)) / (json / (repetitions * files.length));

        System.out.println("decode: " + speedupPerOperation);
    }

    @Test
    public void naiveMicroBenchmarkEncode() {

        Map[] json = new Map[5];
        JsonObject[] jsono = new JsonObject[5];

        for (int i = 0; i < json.length; i++) {
            jsono[i] = new JsonObject(readResourceToBuffer(this.getClass(), "/file" + (i+1) + ".json").toString());
            json[i] = jsono[i].toMap();
        }

        int repetitions = 100000;

        long t0 = System.nanoTime();

        for (int i = 0; i < repetitions; i++) {
            for (int j = 0; j < json.length; j++) {
                JSON.encode(json[j]);
            }
        }

        long t1 = System.nanoTime();

        for (int i = 0; i < repetitions; i++) {
            for (int j = 0; j < json.length; j++) {
                jsono[j].encode();
            }
        }

        long t2 = System.nanoTime();

        double _xson = (t1 - t0);
        double _json = (t2 - t1);

        assertTrue(_xson < _json);

        double speedupPerOperation = (_xson / (repetitions * json.length)) / (_json / (repetitions * json.length));

        System.out.println("encode: " + speedupPerOperation);
    }
}

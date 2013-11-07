package com.jetdrone.vertx.mods.bson;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class JSONTest {

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
    public void testEncodeArray() {

        List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        numbers.add(2);
        numbers.add(3);
        numbers.add(4);

        String encoded = JSON.encode(numbers).toString();
        assertEquals("[1,2,3,4]", encoded);
    }

    @Test
    public void testDecode() {
        System.out.println(JSON.decode(new Buffer().appendString("  \"hello crazy\\\" world!\"")));
    }

    @Test
    public void testJson1() {
        Buffer file1 = readResourceToBuffer(this.getClass(), "/file1.json");

        System.out.println(JSON.decode(file1));
    }
}

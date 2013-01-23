package com.jetdrone.bson.vertx.eventbus;

import com.jetdrone.bson.BSONElement;
import com.jetdrone.bson.BSONObject;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public class EncoderTest {

    static class TestObject implements BSONObject {
        @BSONElement
        public String name;
        @BSONElement
        public Double value;
        @BSONElement
        public byte[] bin;
    }

    @Test
    public void speedTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // prepare
        BSONCompiler compiler = new BSONCompiler(TestObject.class);

        // test normal way
        long t0 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            Map<String, Object> json = new HashMap<>();
            json.put("name", "jsonMap");
            json.put("value", 1.0);
            json.put("bin", new byte[] {0, 1, 2, 3, 4, 5});

            BSONCodec.encode(json);
        }
        long t1 = System.nanoTime();
        // test reflection
        for (int i = 0; i < 10000; i++) {
            TestObject json = new TestObject();
            json.name = "jsonMap";
            json.value = 1.0;
            json.bin = new byte[] {0, 1, 2, 3, 4, 5};
            compiler.serialize(json);
        }
        long t2 = System.nanoTime();
        // reporting
        System.out.println("Map serialization: " + (t1 - t0));
        System.out.println("Interface+Annotation serialization: " + (t2 - t1));
        System.out.println("Speedup: " + (t1 - t0) / (t2 - t1));
    }
}

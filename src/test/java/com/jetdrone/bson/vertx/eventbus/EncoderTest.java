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
    }

    @Test
    public void speedTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // prepare
        BSONCompiler compiler = new BSONCompiler();
        compiler.compile(TestObject.class);

        // test normal way
        long t0 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            Map<String, String> json = new HashMap<>();
            json.put("name", "jsonMap");

            BSONCodec.encode(json);
        }
        long t1 = System.nanoTime();
        // test refection
        for (int i = 0; i < 10000; i++) {
            TestObject json = new TestObject();
            json.name = "jsonMap";

            compiler.serialize(json);
        }
        long t2 = System.nanoTime();
        // reporting
        System.out.println("Map serialization: " + (t1 - t0));
        System.out.println("Interface+Annotation serialization: " + (t2 - t1));
        System.out.println("Speedup: " + (t1 - t0) / (t2 - t1));
    }
}

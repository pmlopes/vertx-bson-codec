package com.jetdrone.bson.vertx.eventbus;

import com.jetdrone.bson.BSONElement;
import com.jetdrone.bson.BSONObject;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EncoderTest {

    static class TestObject implements BSONObject {
        @BSONElement
        public String name;
        @BSONElement
        public Double value;
        @BSONElement
        public byte[] bin;
        @BSONElement
        public TestSubObject subjson;
    }

    static class TestSubObject implements BSONObject {
        @BSONElement
        public String name;
    }

    @Test
    public void speedTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // prepare
        BSONCompiler.compile(TestObject.class);
        BSONCompiler.compile(TestSubObject.class);

        // test normal way
        long t0 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            Map<String, Object> json = new HashMap<>();
            json.put("name", "jsonMap");
            json.put("value", 1.0);
            json.put("bin", new byte[] {0, 1, 2, 3, 4, 5});
            Map<String, Object> subjson = new HashMap<>();
            subjson.put("name", "subJsonMap");
            json.put("subjson", subjson);
//            List array = new ArrayList();
//            array.add("A");
//            array.add(1);
//            array.add(true);
//            json.put("array", array);
            BSONCodec.encode(json);
        }
        long t1 = System.nanoTime();
        // test reflection
        for (int i = 0; i < 10000; i++) {
            TestObject json = new TestObject();
            json.name = "jsonMap";
            json.value = 1.0;
            json.bin = new byte[] {0, 1, 2, 3, 4, 5};
            json.subjson = new TestSubObject();
            json.subjson.name = "subJsonMap";
//            List array = new ArrayList();
//            array.add("A");
//            array.add(1);
//            array.add(true);
//            json.array = array;

            BSONCompiler.serialize(json);
        }
        long t2 = System.nanoTime();
        // reporting
        System.out.println("Map serialization: " + (t1 - t0));
        System.out.println("Interface+Annotation serialization: " + (t2 - t1));
        System.out.println("Speedup: " + ((double) (t1 - t0)) / ((double) (t2 - t1)));
    }
}

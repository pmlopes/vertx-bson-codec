package com.jetdrone.bson.vertx.eventbus;

import com.jetdrone.bson.BSONElement;
import com.jetdrone.bson.BSONObject;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
        @BSONElement
        public List array;
    }

    static class TestSubObject implements BSONObject {
        @BSONElement
        public String name;
    }

    @Test
    public void speedTest() {
        // prepare
        BSONCodec.compile(TestObject.class);
        BSONCodec.compile(TestSubObject.class);

        // test normal way
        long t0 = System.nanoTime();
        for (int i = 0; i < 10000; i++) {
            Map<String, Object> json = new HashMap<>();
            json.put("name", "jsonMap");
            json.put("value", 1.0);
            json.put("bin", new byte[] {0, 1, 2, 3, 4, 5});
            Map<String, Object> subjson = new IdentityHashMap<>();
            subjson.put("name", "subJsonMap");
            json.put("subjson", subjson);
            List array = new ArrayList();
            array.add("A");
            array.add(1);
            array.add(true);
            json.put("array", array);

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
            List array = new ArrayList();
            array.add("A");
            array.add(1);
            array.add(true);
            json.array = array;

            BSONCodec.encode(json);
        }
        long t2 = System.nanoTime();

//        // reporting
//        System.out.println("Map serialization: " + (t1 - t0));
//        System.out.println("Interface+Annotation serialization: " + (t2 - t1));
//        System.out.println("Speedup: " + ((double) (t1 - t0)) / ((double) (t2 - t1)));

        assertTrue(((double) (t1 - t0)) / ((double) (t2 - t1)) > 1.0);
    }

    @Test
    public void validityTest() {
        // prepare
        BSONCodec.compile(TestObject.class);
        BSONCodec.compile(TestSubObject.class);

        // test normal way
        Map<String, Object> json = new HashMap<>();
        json.put("name", "jsonMap");
        json.put("value", 1.0);
        json.put("bin", new byte[] {0, 1, 2, 3, 4, 5});
        Map<String, Object> subjson = new HashMap<>();
        subjson.put("name", "subJsonMap");
        json.put("subjson", subjson);
        List array = new ArrayList();
        array.add("A");
        array.add(1);
        array.add(true);
        json.put("array", array);

        Buffer fromMap = BSONCodec.encode(json);

        TestObject obj = new TestObject();
        obj.name = "jsonMap";
        obj.value = 1.0;
        obj.bin = new byte[] {0, 1, 2, 3, 4, 5};
        obj.subjson = new TestSubObject();
        obj.subjson.name = "subJsonMap";
        List list = new ArrayList();
        list.add("A");
        list.add(1);
        list.add(true);
        obj.array = list;

        Buffer fromObj = BSONCodec.encode(obj);
        assertEquals(fromMap.length(), fromObj.length());

        // reverse the encoding back to maps
        Map backFromMap = BSONCodec.decode(fromMap);
        Map backFromObj = BSONCodec.decode(fromObj);

        // verify the binary equality (not identity)
        assertArrayEquals((byte[]) backFromMap.get("bin"), (byte[]) backFromObj.get("bin"));

        // remove the bin from the maps
        backFromMap.remove("bin");
        backFromObj.remove("bin");
        // compare the remaining natives
        assertEquals(backFromMap, backFromObj);
    }
}

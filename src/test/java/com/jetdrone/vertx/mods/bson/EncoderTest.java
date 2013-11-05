package com.jetdrone.vertx.mods.bson;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.util.*;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EncoderTest {

    static class TestObject {
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

    static class TestSubObject {
        @BSONElement
        public String name;
    }

    @Test
    public void speedTest() {
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
            List<Object> array = new ArrayList<>();
            array.add("A");
            array.add(1);
            array.add(true);
            json.put("array", array);

            BSON.encodeMap(json);
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
            List<Object> array = new ArrayList<>();
            array.add("A");
            array.add(1);
            array.add(true);
            json.array = array;

            BSON.encodeObject(json);
        }
        long t2 = System.nanoTime();

        // reporting
        System.out.println("Map serialization: " + (t1 - t0));
        System.out.println("Annotation serialization: " + (t2 - t1));
        System.out.println("Speedup: " + ((double) (t1 - t0)) / ((double) (t2 - t1)));

        assertTrue(((double) (t1 - t0)) / ((double) (t2 - t1)) > 1.0);
    }

    @Test
    public void validityTest() {
        // test normal way
        Map<String, Object> json = new HashMap<>();
        json.put("name", "jsonMap");
        json.put("value", 1.0);
        json.put("bin", new byte[] {0, 1, 2, 3, 4, 5});
        Map<String, Object> subjson = new HashMap<>();
        subjson.put("name", "subJsonMap");
        json.put("subjson", subjson);
        List<Object> array = new ArrayList<>();
        array.add("A");
        array.add(1);
        array.add(true);
        json.put("array", array);

        Buffer fromMap = BSON.encodeMap(json);

        TestObject obj = new TestObject();
        obj.name = "jsonMap";
        obj.value = 1.0;
        obj.bin = new byte[] {0, 1, 2, 3, 4, 5};
        obj.subjson = new TestSubObject();
        obj.subjson.name = "subJsonMap";
        List<Object> list = new ArrayList<>();
        list.add("A");
        list.add(1);
        list.add(true);
        obj.array = list;

        Buffer fromObj = BSON.encodeObject(obj);
        assertEquals(fromMap.length(), fromObj.length());

        // reverse the encoding back to maps
        Map backFromMap = BSON.decode(fromMap);
        Map backFromObj = BSON.decode(fromObj);

        // verify the binary equality (not identity)
        assertArrayEquals((byte[]) backFromMap.get("bin"), (byte[]) backFromObj.get("bin"));

        // remove the bin from the maps
        backFromMap.remove("bin");
        backFromObj.remove("bin");
        // compare the remaining natives
        assertEquals(backFromMap, backFromObj);

        json.remove("bin");
        Buffer fromMap2 = BSON.encodeMap(json);

        TestObject backFromObj2 = BSON.decodeObject(TestObject.class, fromMap2);
    }
}

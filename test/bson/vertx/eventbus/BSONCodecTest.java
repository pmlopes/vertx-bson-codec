package bson.vertx.eventbus;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

public class BSONCodecTest {

    @BeforeClass
    public static void testSetup() {
        // Preparation of the unit tests
    }

    @AfterClass
    public static void testCleanup() {
        // Teardown for data used by the unit tests
    }

    @Test
    public void testEncodeMap() {
        Map<String, Object> test = new HashMap<>();
        test.put("hello", "world");
        test.put("PI", Math.PI);
        test.put("null", null);
        test.put("createDate", new Date(0));
        List<Object> list = new ArrayList<>();
        list.add("awesome");
        list.add(5.05);
        list.add(1986);
        list.add(true);
        list.add(null);
        list.add(new Date());
        test.put("BSON", list);

        BSONCodec.encode(test);
    }
} 

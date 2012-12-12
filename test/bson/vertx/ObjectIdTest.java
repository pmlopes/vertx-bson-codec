package bson.vertx;

import org.junit.Test;

public class ObjectIdTest {

    @Test
    public void testObjectCreate() throws Exception {
        new ObjectId();
    }

    @Test
    public void testObjectCreateFromHex() throws Exception {
        new ObjectId("4d88e15b60f486e428412dc9");
//        1300816219,
//        []byte{0x60, 0xf4, 0x86},
//        0xe428,
//        427156,

        new ObjectId("00000000aabbccddee000001");
//        0,
//        []byte{0xaa, 0xbb, 0xcc},
//        0xddee,
//        1,
    }
}

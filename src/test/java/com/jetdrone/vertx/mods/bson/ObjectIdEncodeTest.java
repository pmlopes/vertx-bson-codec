package com.jetdrone.vertx.mods.bson;

import com.jetdrone.vertx.mods.bson.ObjectId;
import com.jetdrone.vertx.mods.bson.BSON;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ObjectIdEncodeTest {

    @Test
    public void testObjectId() {
        Map<String, ObjectId> json = new HashMap<>();
        json.put("_", new ObjectId("4d88e15b60f486e428412dc9"));

        byte[] bson = BSON.encode(json).getBytes();

        byte[] expected = new byte[]{
                // length
                0x14, 0x00, 0x00, 0x00,
                // data
                0x07, '_', 0x00, 0x4d, (byte) 0x88, (byte) 0xe1, 0x5b, 0x60, (byte) 0xf4, (byte) 0x86, (byte) 0xe4, 0x28, 0x41, 0x2d, (byte) 0xc9,
                // end
                0x00
        };

        assertArrayEquals(expected, bson);

        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }
}

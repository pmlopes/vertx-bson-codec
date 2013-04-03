package com.jetdrone.vertx.mods.bson;

import com.jetdrone.vertx.mods.bson.BSON;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * This class contains tests inspired from the gobson project.
 */
public class MongoTimestampTest {

    @Test
    public void testTimestamp() {
        Map<String, Timestamp> json = new HashMap<>();
        json.put("_", new Timestamp(258));

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[] {
                // length
                0x10, 0x00, 0x00, 0x00,
                0x11, '_', 0x00, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }
}
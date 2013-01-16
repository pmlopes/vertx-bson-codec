package com.jetdrone.bson.vertx.eventbus;

import com.jetdrone.bson.vertx.Binary;
import com.jetdrone.bson.vertx.MD5;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class BinaryTest {

    @Test
    public void testUUID() {
        Map<String, UUID> json = new HashMap<>();
        json.put("_", UUID.fromString("797ff043-11eb-11e1-80d6-510998755d10"));

        byte[] bson = BSONCodec.encode(json).getBytes();

        byte[] expected = new byte[]{
                // length
                0x1d, 0x00, 0x00, 0x00,
                // data
                0x05, '_', 0x00, 0x10, 0x00, 0x00, 0x00, 0x04, 0x79, 0x7f, (byte) 0xf0, 0x43, 0x11, (byte) 0xeb, 0x11, (byte) 0xe1, (byte) 0x80, (byte) 0xd6, 0x51, 0x09, (byte) 0x98, 0x75, 0x5d, 0x10,
                // end
                0x00
        };

        assertArrayEquals(expected, bson);

        // reverse
        Map document = BSONCodec.decode(new Buffer(expected));
        assertEquals(json, document);
    }

    @Test
    public void testUserDefinedBinary() {
        Map<String, Binary> json = new HashMap<>();
        json.put("_", new Binary() {
            public byte[] getBytes() {
                return "udef".getBytes();
            }
        });

        byte[] bson = BSONCodec.encode(json).getBytes();

        byte[] expected = new byte[]{
                // length
                0x11, 0x00, 0x00, 0x00,
                // data
                0x05, '_', 0x00, 0x04, 0x00, 0x00, 0x00, (byte) 0x80, 'u', 'd', 'e', 'f',
                // end
                0x00
        };

        assertArrayEquals(expected, bson);

        // reverse
        Map document = BSONCodec.decode(new Buffer(expected));
        assertArrayEquals(new byte[]{'u', 'd', 'e', 'f'}, ((Binary) document.get("_")).getBytes());
    }

    @Test
    public void testMD5Binary() {
        Map<String, MD5> json = new HashMap<>();
        json.put("_", new MD5() {
            public byte[] getHash() {
                return "udef".getBytes();
            }
        });

        byte[] bson = BSONCodec.encode(json).getBytes();

        byte[] expected = new byte[]{
                // length
                0x11, 0x00, 0x00, 0x00,
                // data
                0x05, '_', 0x00, 0x04, 0x00, 0x00, 0x00, 0x05, 'u', 'd', 'e', 'f',
                // end
                0x00
        };

        assertArrayEquals(expected, bson);

        // reverse
        Map document = BSONCodec.decode(new Buffer(expected));
        assertArrayEquals(new byte[]{'u', 'd', 'e', 'f'}, ((MD5) document.get("_")).getHash());
    }
}

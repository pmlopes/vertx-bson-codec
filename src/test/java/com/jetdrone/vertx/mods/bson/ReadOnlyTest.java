package com.jetdrone.vertx.mods.bson;

import com.jetdrone.vertx.xson.java.BSON;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.util.Map;

import static org.junit.Assert.assertArrayEquals;

public class ReadOnlyTest {

    @Test
    public void testOldBinary() {
        byte[] bson = new byte[]{
                // length
                0x14, 0x00, 0x00, 0x00,
                // data
                0x05, '_', 0x00, 0x07, 0x00, 0x00, 0x00, 0x02, 0x03, 0x00, 0x00, 0x00, 'o', 'l', 'd',
                // end
                0x00
        };

        Map document = BSON.decode(new Buffer(bson));
        assertArrayEquals(new byte[]{'o', 'l', 'd'}, (byte[]) document.get("_"));
    }
}

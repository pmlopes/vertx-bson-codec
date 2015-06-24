package com.jetdrone.vertx.codec.bson;

import io.vertx.core.buffer.Buffer;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class ObjectIdEncodeTest {

  private static final BSONMessageCodec BSON = new BSONMessageCodec();

  @Test
  public void testObjectId() {
    BSONDocument json = new BSONDocument();
    json.put("_", new ObjectId("4d88e15b60f486e428412dc9"));

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x14, 0x00, 0x00, 0x00,
        // data
        0x07, '_', 0x00, 0x4d, (byte) 0x88, (byte) 0xe1, 0x5b, 0x60, (byte) 0xf4, (byte) 0x86, (byte) 0xe4, 0x28, 0x41, 0x2d, (byte) 0xc9,
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());

    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }
}

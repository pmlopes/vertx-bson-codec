package com.jetdrone.vertx.codec.bson;

import io.vertx.core.buffer.Buffer;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * This class contains tests inspired from the gobson project.
 */
public class MongoTimestampTest {

  private static final BSONMessageCodec BSON = new BSONMessageCodec();

  @Test
  public void testTimestamp() {
    BSONDocument json = new BSONDocument();
    json.put("_", new Timestamp(258));

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x10, 0x00, 0x00, 0x00,
        0x11, '_', 0x00, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }
}
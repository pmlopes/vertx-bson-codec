package com.jetdrone.vertx.codec.bson;

import io.vertx.core.buffer.Buffer;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertArrayEquals;

public class ReadOnlyTest {

  private static final BSONMessageCodec BSON = new BSONMessageCodec();

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

    Map document = BSON.decodeFromWire(0, Buffer.buffer(bson));
    assertArrayEquals(new byte[]{'o', 'l', 'd'}, (byte[]) document.get("_"));
  }
}

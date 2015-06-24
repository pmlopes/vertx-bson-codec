package com.jetdrone.vertx.codec.bson;

import io.vertx.core.buffer.Buffer;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * This class contains tests inspired from the gobson project.
 */
public class GoBSONTest {

  private static final BSONMessageCodec BSON = new BSONMessageCodec();

  private static final byte[] EMPTY_BSON = new byte[]{
      5,
      0,
      0,
      0,
      0
  };

  @Test
  public void testEmptyMap() {
    BSONDocument empty = new BSONDocument();

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, empty);
    byte[] bson = buffer.getBytes();

    assertArrayEquals(EMPTY_BSON, bson);

    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(EMPTY_BSON));
    assertEquals(empty, document);
  }

  @Test
  public void testFloat() {
    BSONDocument json = new BSONDocument();
    json.put("_", 5.05);

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x10, 0x0, 0x0, 0x0,
        0x01, '_', 0x00, '3', '3', '3', '3', '3', '3', 0x14, '@',
        // end
        0x00};

    assertArrayEquals(expected, buffer.getBytes());

    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }

  @Test
  public void testString() {
    BSONDocument json = new BSONDocument();
    json.put("_", "yo");

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);
    byte[] bson = buffer.getBytes();

    byte[] expected = new byte[]{
        // length
        0x0f, 0x00, 0x00, 0x00,
        0x02, '_', 0x00, 0x03, 0x00, 0x00, 0x00, 'y', 'o', 0x00,
        // end
        0x00};

    assertArrayEquals(expected, bson);
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }

  @Test
  public void testSubDocumentBoolean() {
    BSONDocument json = new BSONDocument();
    BSONDocument subjson = new BSONDocument();
    subjson.put("a", true);
    json.put("_", subjson);

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);
    byte[] bson = buffer.getBytes();

    byte[] expected = new byte[]{
        // length
        0x11, 0x00, 0x00, 0x00,
        0x03, '_', 0x00, 0x09, 0x00, 0x00, 0x00, 0x08, 'a', 0x00, 0x01, 0x00,
        // end
        0x00
    };

    assertArrayEquals(expected, bson);
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }

  @Test
  public void testTrue() {
    BSONDocument json = new BSONDocument();
    json.put("_", true);

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x09, 0x00, 0x00, 0x00,
        0x08, '_', 0x00, 0x01,
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }

  @Test
  public void testFalse() {
    BSONDocument json = new BSONDocument();
    json.put("_", false);

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x09, 0x00, 0x00, 0x00,
        0x08, '_', 0x00, 0x00,
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }

  @Test
  public void testNull() {
    BSONDocument json = new BSONDocument();
    json.put("_", null);

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x08, 0x00, 0x00, 0x00,
        0x0a, '_', 0x00,
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }

  @Test
  public void testRegEx() {
    BSONDocument json = new BSONDocument();
    json.put("_", Pattern.compile("ab"));

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x0c, 0x00, 0x00, 0x00,
        0x0b, '_', 0x00, 'a', 'b', 0x00, 0x00,
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    // compare if the keys are the same
    assertEquals(json.keySet(), document.keySet());
    // now compare the regex
    Pattern jsonp = (Pattern) json.get("_");
    Pattern docp = (Pattern) document.get("_");

    assertEquals(jsonp.pattern(), docp.pattern());
    assertEquals(jsonp.flags(), docp.flags());
  }

  @Test
  public void testDate() {
    BSONDocument json = new BSONDocument();
    json.put("_", new Date(258));

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x10, 0x00, 0x00, 0x00,
        0x09, '_', 0x00, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }

  @Test
  public void testBinary() {
    BSONDocument json = new BSONDocument();
    json.put("_", new byte[]{'y', 'o'});

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x0f, 0x00, 0x00, 0x00,
        0x05, '_', 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 'y', 'o',
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));

    // compare if the keys are the same
    assertEquals(json.keySet(), document.keySet());
    // now compare the regex
    byte[] jsonBytes = (byte[]) json.get("_");
    byte[] docBytes = (byte[]) document.get("_");

    assertArrayEquals(jsonBytes, docBytes);
  }

  @Test
  public void testInt32() {
    BSONDocument json = new BSONDocument();
    json.put("_", 258);

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x0c, 0x00, 0x00, 0x00,
        0x10, '_', 0x00, 0x02, 0x01, 0x00, 0x00,
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }

  @Test
  public void testInt64() {
    BSONDocument json = new BSONDocument();
    json.put("_", 258l);

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x10, 0x00, 0x00, 0x00,
        0x12, '_', 0x00, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }

  @Test
  public void testInt64_2() {
    BSONDocument json = new BSONDocument();
    json.put("_", 258l << 32);

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x10, 0x00, 0x00, 0x00,
        0x12, '_', 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x01, 0x00, 0x00,
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }

  @Test
  public void testMinKey() {
    BSONDocument json = new BSONDocument();
    json.put("_", Key.MIN);

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x08, 0x00, 0x00, 0x00,
        (byte) 0xff, '_', 0x00,
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }

  @Test
  public void testMaxKey() {
    BSONDocument json = new BSONDocument();
    json.put("_", Key.MAX);

    Buffer buffer = Buffer.buffer();
    BSON.encodeToWire(buffer, json);

    byte[] expected = new byte[]{
        // length
        0x08, 0x00, 0x00, 0x00,
        0x7f, '_', 0x00,
        // end
        0x00
    };

    assertArrayEquals(expected, buffer.getBytes());
    // reverse
    Map document = BSON.decodeFromWire(0, Buffer.buffer(expected));
    assertEquals(json, document);
  }
}
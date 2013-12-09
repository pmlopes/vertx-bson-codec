package com.jetdrone.vertx.mods.bson;

import com.jetdrone.vertx.xson.java.BSON;
import com.jetdrone.vertx.xson.java.bson.Key;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

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

    private static final byte[] EMPTY_BSON = new byte[]{
            5,
            0,
            0,
            0,
            0
    };

    @Test
    public void testEmptyMap() {
        Map<String, ?> empty = new HashMap<>();
        Buffer buffer = BSON.encode(empty);

        assertArrayEquals(EMPTY_BSON, buffer.getBytes());

        // reverse
        Map document = BSON.decode(new Buffer(EMPTY_BSON));
        assertEquals(empty, document);
    }

    @Test
    public void testFloat() {
        Map<String, Double> json = new HashMap<>();
        json.put("_", 5.05);

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x10, 0x0, 0x0, 0x0,
                0x01, '_', 0x00, '3', '3', '3', '3', '3', '3', 0x14, '@',
                // end
                0x00};

        assertArrayEquals(expected, buffer.getBytes());

        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }

    @Test
    public void testString() {
        Map<String, String> json = new HashMap<>();
        json.put("_", "yo");

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x0f, 0x00, 0x00, 0x00,
                0x02, '_', 0x00, 0x03, 0x00, 0x00, 0x00, 'y', 'o', 0x00,
                // end
                0x00};

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }

    @Test
    public void testSubDocumentBoolean() {
        Map<String, Map> json = new HashMap<>();
        Map<String, Boolean> subjson = new HashMap<>();
        subjson.put("a", true);
        json.put("_", subjson);

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x11, 0x00, 0x00, 0x00,
                0x03, '_', 0x00, 0x09, 0x00, 0x00, 0x00, 0x08, 'a', 0x00, 0x01, 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }

    @Test
    public void testTrue() {
        Map<String, Boolean> json = new HashMap<>();
        json.put("_", true);

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x09, 0x00, 0x00, 0x00,
                0x08, '_', 0x00, 0x01,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }

    @Test
    public void testFalse() {
        Map<String, Boolean> json = new HashMap<>();
        json.put("_", false);

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x09, 0x00, 0x00, 0x00,
                0x08, '_', 0x00, 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }

    @Test
    public void testNull() {
        Map<String, Object> json = new HashMap<>();
        json.put("_", null);

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x08, 0x00, 0x00, 0x00,
                0x0a, '_', 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }

    @Test
    public void testRegEx() {
        Map<String, Pattern> json = new HashMap<>();
        json.put("_", Pattern.compile("ab"));

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x0c, 0x00, 0x00, 0x00,
                0x0b, '_', 0x00, 'a', 'b', 0x00, 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        // compare if the keys are the same
        assertEquals(json.keySet(), document.keySet());
        // now compare the regex
        Pattern jsonp = json.get("_");
        Pattern docp = (Pattern) document.get("_");

        assertEquals(jsonp.pattern(), docp.pattern());
        assertEquals(jsonp.flags(), docp.flags());
    }

    @Test
    public void testDate() {
        Map<String, Date> json = new HashMap<>();
        json.put("_", new Date(258));

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x10, 0x00, 0x00, 0x00,
                0x09, '_', 0x00, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }

    @Test
    public void testBinary() {
        Map<String, byte[]> json = new HashMap<>();
        json.put("_", new byte[]{'y', 'o'});

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x0f, 0x00, 0x00, 0x00,
                0x05, '_', 0x00, 0x02, 0x00, 0x00, 0x00, 0x00, 'y', 'o',
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));

        // compare if the keys are the same
        assertEquals(json.keySet(), document.keySet());
        // now compare the regex
        byte[] jsonBytes = json.get("_");
        byte[] docBytes = (byte[]) document.get("_");

        assertArrayEquals(jsonBytes, docBytes);
    }

    @Test
    public void testInt32() {
        Map<String, Integer> json = new HashMap<>();
        json.put("_", 258);

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x0c, 0x00, 0x00, 0x00,
                0x10, '_', 0x00, 0x02, 0x01, 0x00, 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }

    @Test
    public void testInt64() {
        Map<String, Long> json = new HashMap<>();
        json.put("_", 258l);

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x10, 0x00, 0x00, 0x00,
                0x12, '_', 0x00, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }

    @Test
    public void testInt64_2() {
        Map<String, Long> json = new HashMap<>();
        json.put("_", 258l << 32);

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x10, 0x00, 0x00, 0x00,
                0x12, '_', 0x00, 0x00, 0x00, 0x00, 0x00, 0x02, 0x01, 0x00, 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }

    @Test
    public void testMinKey() {
        Map<String, Key> json = new HashMap<>();
        json.put("_", Key.MIN);

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x08, 0x00, 0x00, 0x00,
                (byte) 0xff, '_', 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }

    @Test
    public void testMaxKey() {
        Map<String, Key> json = new HashMap<>();
        json.put("_", Key.MAX);

        Buffer buffer = BSON.encode(json);
        byte[] expected = new byte[]{
                // length
                0x08, 0x00, 0x00, 0x00,
                0x7f, '_', 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
        // reverse
        Map document = BSON.decode(new Buffer(expected));
        assertEquals(json, document);
    }
}
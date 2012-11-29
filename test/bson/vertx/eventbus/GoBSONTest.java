package bson.vertx.eventbus;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.assertArrayEquals;

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
        Map empty = new HashMap();
        Buffer buffer = BSONCodec.encode(empty);

        assertArrayEquals(EMPTY_BSON, buffer.getBytes());
    }

    @Test
    public void testFloat() {
        Map<String, Double> json = new HashMap<>();
        json.put("_", 5.05);

        Buffer buffer = BSONCodec.encode(json);
        byte[] expected = new byte[]{
                // length
                0x10, 0x0, 0x0, 0x0,
                0x01, '_', 0x00, '3', '3', '3', '3', '3', '3', 0x14, '@',
                // end
                0x00};

        assertArrayEquals(expected, buffer.getBytes());
    }

    @Test
    public void testString() {
        Map<String, String> json = new HashMap<>();
        json.put("_", "yo");

        Buffer buffer = BSONCodec.encode(json);
        byte[] expected = new byte[]{
                // length
                0x0f, 0x00, 0x00, 0x00,
                0x02, '_', 0x00, 0x03, 0x00, 0x00, 0x00, 'y', 'o', 0x00,
                // end
                0x00};

        assertArrayEquals(expected, buffer.getBytes());
    }

    @Test
    public void testSubDocumentBoolean() {
        Map<String, Map> json = new HashMap<>();
        Map<String, Boolean> subjson = new HashMap<>();
        subjson.put("a", true);
        json.put("_", subjson);

        Buffer buffer = BSONCodec.encode(json);
        byte[] expected = new byte[]{
                // length
                0x11, 0x00, 0x00, 0x00,
                0x03, '_', 0x00, 0x09, 0x00, 0x00, 0x00, 0x08, 'a', 0x00, 0x01, 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());

    }

    @Test
    public void testTrue() {
        Map<String, Boolean> json = new HashMap<>();
        json.put("_", true);

        Buffer buffer = BSONCodec.encode(json);
        byte[] expected = new byte[]{
                // length
                0x09, 0x00, 0x00, 0x00,
                0x08, '_', 0x00, 0x01,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
    }

    @Test
    public void testFalse() {
        Map<String, Boolean> json = new HashMap<>();
        json.put("_", false);

        Buffer buffer = BSONCodec.encode(json);
        byte[] expected = new byte[]{
                // length
                0x09, 0x00, 0x00, 0x00,
                0x08, '_', 0x00, 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
    }

    @Test
    public void testNull() {
        Map<String, Object> json = new HashMap<>();
        json.put("_", null);

        Buffer buffer = BSONCodec.encode(json);
        byte[] expected = new byte[]{
                // length
                0x08, 0x00, 0x00, 0x00,
                0x0a, '_', 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
    }

    @Test
    public void testRegEx() {
        Map<String, Pattern> json = new HashMap<>();
        json.put("_", Pattern.compile("ab"));

        Buffer buffer = BSONCodec.encode(json);
        byte[] expected = new byte[]{
                // length
                0x0c, 0x00, 0x00, 0x00,
                0x0b, '_', 0x00, 'a', 'b', 0x00, 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
    }

    @Test
    public void testDate() {
        Map<String, Date> json = new HashMap<>();
        json.put("_", new Date(258));

        Buffer buffer = BSONCodec.encode(json);
        byte[] expected = new byte[]{
                // length
                0x10, 0x00, 0x00, 0x00,
                0x09, '_', 0x00, 0x02, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                // end
                0x00
        };

        assertArrayEquals(expected, buffer.getBytes());
    }

//    {bson.M{"_": []interface{}{true, false}},
//        "\x04_\x00\r\x00\x00\x00\x080\x00\x01\x081\x00\x00\x00"},
//    {bson.M{"_": []byte("yo")},
//        "\x05_\x00\x02\x00\x00\x00\x00yo"},
//    {bson.M{"_": bson.Binary{0x02, []byte("old")}},
//        "\x05_\x00\x07\x00\x00\x00\x02\x03\x00\x00\x00old"},
//    {bson.M{"_": bson.Binary{0x80, []byte("udef")}},
//        "\x05_\x00\x04\x00\x00\x00\x80udef"},
//    {bson.M{"_": bson.Undefined}, // Obsolete, but still seen in the wild.
//        "\x06_\x00"},
//    {bson.M{"_": bson.ObjectId("0123456789ab")},
//        "\x07_\x000123456789ab"},
//    {bson.M{"_": bson.JS{"code", nil}},
//        "\x0D_\x00\x05\x00\x00\x00code\x00"},
//    {bson.M{"_": bson.Symbol("sym")},
//        "\x0E_\x00\x04\x00\x00\x00sym\x00"},
//    {bson.M{"_": bson.JS{"code", bson.M{"": nil}}},
//        "\x0F_\x00\x14\x00\x00\x00\x05\x00\x00\x00code\x00" +
//                "\x07\x00\x00\x00\x0A\x00\x00"},
//    {bson.M{"_": 258},
//        "\x10_\x00\x02\x01\x00\x00"},
//    {bson.M{"_": bson.MongoTimestamp(258)},
//        "\x11_\x00\x02\x01\x00\x00\x00\x00\x00\x00"},
//    {bson.M{"_": int64(258)},
//        "\x12_\x00\x02\x01\x00\x00\x00\x00\x00\x00"},
//    {bson.M{"_": int64(258 << 32)},
//        "\x12_\x00\x00\x00\x00\x00\x02\x01\x00\x00"},
//    {bson.M{"_": bson.MaxKey},
//        "\x7F_\x00"},
//    {bson.M{"_": bson.MinKey},
//        "\xFF_\x00"},}
}
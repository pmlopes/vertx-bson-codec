package com.jetdrone.vertx.mods.bson;

import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * This class contains tests based on jsonspec.org website
 */
public class BSONSPECTest {

    @Test
    public void testEncodeBSONSPEC1() {
        Map<String, Object> json = new HashMap<>();
        json.put("hello", "world");

        // this is the hello world example from http://bsonspec.org
        byte[] expected = new byte[]{
                (byte) 0x16,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x02,
                'h',
                'e',
                'l',
                'l',
                'o',
                (byte) 0x00,
                (byte) 0x06,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                'w',
                'o',
                'r',
                'l',
                'd',
                (byte) 0x00,
                (byte) 0x00
        };

        byte[] bson = BSON.encodeMap(json).getBytes();

        assertArrayEquals(expected, bson);
    }

    @Test
    public void testDecodeBSONSPEC1() {
        // this is the hello world example from http://bsonspec.org
        byte[] bson = new byte[]{
                (byte) 0x16,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x02,
                'h',
                'e',
                'l',
                'l',
                'o',
                (byte) 0x00,
                (byte) 0x06,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                'w',
                'o',
                'r',
                'l',
                'd',
                (byte) 0x00,
                (byte) 0x00
        };

        Map json = BSON.decode(new Buffer(bson));

        Map<String, String> expected = new HashMap<>();
        expected.put("hello", "world");

        assertEquals(expected, json);
    }

    @Test
    public void testEncodeBSONSPEC2() {
        // this is {"BSON": ["awesome", 5.05, 1986]} from http://bsonspec.org
        Map<String, Object> json = new HashMap<>();
        List<Object> list = new ArrayList<>();
        list.add("awesome");
        list.add(5.05);
        list.add(1986);
        json.put("BSON", list);

        byte[] expected = new byte[]{
                (byte) 0x31,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x04,
                'B',
                'S',
                'O',
                'N',
                (byte) 0x00,
                (byte) 0x26,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x02,
                '0',
                (byte) 0x00,
                (byte) 0x08,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                'a',
                'w',
                'e',
                's',
                'o',
                'm',
                'e',
                (byte) 0x00,
                (byte) 0x01,
                '1',
                (byte) 0x00,
                (byte) 0x33,
                (byte) 0x33,
                (byte) 0x33,
                (byte) 0x33,
                (byte) 0x33,
                (byte) 0x33,
                (byte) 0x14,
                (byte) 0x40,
                (byte) 0x10,
                '2',
                (byte) 0x00,
                (byte) 0xc2,
                (byte) 0x07,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00
        };

        byte[] bson = BSON.encodeMap(json).getBytes();

        assertArrayEquals(expected, bson);
    }

    @Test
    public void testDecodeBSONSPEC2() {
        byte[] bson = new byte[]{
                (byte) 0x31,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x04,
                'B',
                'S',
                'O',
                'N',
                (byte) 0x00,
                (byte) 0x26,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x02,
                '0',
                (byte) 0x00,
                (byte) 0x08,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                'a',
                'w',
                'e',
                's',
                'o',
                'm',
                'e',
                (byte) 0x00,
                (byte) 0x01,
                '1',
                (byte) 0x00,
                (byte) 0x33,
                (byte) 0x33,
                (byte) 0x33,
                (byte) 0x33,
                (byte) 0x33,
                (byte) 0x33,
                (byte) 0x14,
                (byte) 0x40,
                (byte) 0x10,
                '2',
                (byte) 0x00,
                (byte) 0xc2,
                (byte) 0x07,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00,
                (byte) 0x00
        };

        Map<String, Object> expected = new HashMap<>();
        List<Object> list = new ArrayList<>();
        list.add("awesome");
        list.add(5.05);
        list.add(1986);
        expected.put("BSON", list);

        Map json = BSON.decode(new Buffer(bson));

        assertEquals(expected, json);
    }
}

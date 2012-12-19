package com.jetdrone.bson.vertx;

import org.junit.Test;

import static org.junit.Assert.*;

public class ObjectIdTest {

    @Test
    public void testObjectCreate() throws Exception {
        new ObjectId();
    }

    @Test
    public void testObjectCreateFromHex() throws Exception {
        ObjectId oid = new ObjectId("4d88e15b60f486e428412dc9");

        assertEquals(1300816219, oid.getTimestamp());
        assertArrayEquals(new byte[]{0x60, (byte) 0xf4, (byte) 0x86}, oid.getMachine());
        assertEquals(0xe428, oid.getPid());
        assertEquals(4271561, oid.getIncrement());

        oid = new ObjectId("00000000aabbccddee000001");

        assertEquals(0, oid.getTimestamp());
        assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc}, oid.getMachine());
        assertEquals(0xddee, oid.getPid());
        assertEquals(1, oid.getIncrement());
    }

    @Test
    public void testObjectCreateFromBin() throws Exception {
        ObjectId oid = new ObjectId(new byte[]{
                // timestamp
                0x4d, (byte) 0x88, (byte) 0xe1, 0x5b,
                // machine
                0x60, (byte) 0xf4, (byte) 0x86,
                // pid
                (byte) 0xe4, 0x28,
                // counter
                0x41, 0x2d, (byte) 0xc9});

        assertEquals(1300816219, oid.getTimestamp());
        assertArrayEquals(new byte[]{0x60, (byte) 0xf4, (byte) 0x86}, oid.getMachine());
        assertEquals(0xe428, oid.getPid());
        assertEquals(4271561, oid.getIncrement());

        oid = new ObjectId(new byte[]{
                // timestamp
                0x00, 0x00, 0x00, 0x00,
                // machine
                (byte) 0xaa, (byte) 0xbb, (byte) 0xcc,
                // pid
                (byte) 0xdd, (byte) 0xee,
                // counter
                0x00, 0x00, 0x01});

        assertEquals(0, oid.getTimestamp());
        assertArrayEquals(new byte[]{(byte) 0xaa, (byte) 0xbb, (byte) 0xcc}, oid.getMachine());
        assertEquals(0xddee, oid.getPid());
        assertEquals(1, oid.getIncrement());
    }

    @Test
    public void testObjectIdToString() throws Exception {
        ObjectId oid = new ObjectId("4d88e15b60f486e428412dc9");
        assertEquals("4d88e15b60f486e428412dc9", oid.toString());

        oid = new ObjectId("00000000aabbccddee000001");
        assertEquals("00000000aabbccddee000001", oid.toString());
    }

    @Test
    public void testObjectIdEquality() throws Exception {
        ObjectId oid = new ObjectId("4d88e15b60f486e428412dc9");
        ObjectId oid2 = new ObjectId("4d88e15b60f486e428412dc9");

        assertTrue(oid.equals(oid2));
        assertFalse(oid == oid2);
    }
}

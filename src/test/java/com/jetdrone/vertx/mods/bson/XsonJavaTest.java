package com.jetdrone.vertx.mods.bson;

import com.jetdrone.vertx.xson.java.JSON;
import com.jetdrone.vertx.xson.java.JsonArray;
import com.jetdrone.vertx.xson.java.JsonObject;
import org.junit.Test;
import org.vertx.java.core.buffer.Buffer;

import java.util.Date;

import static org.junit.Assert.*;

public class XsonJavaTest {

    @Test
    public void testSimple() {
        JsonObject obj = new JsonObject();
        obj.putAt("str-key", "value");
        obj.put("date-key", new Date(1386234960));

        JsonArray array = new JsonArray(1, 2, 3, 4);
        obj.put("array-key", array);

        assertEquals("{\"str-key\":\"value\",\"date-key\":\"1970-01-17T01:03:54.960Z\",\"array-key\":[1,2,3,4]}", JSON.encode(obj));

        JsonObject obj2 = JSON.decodeObject(new Buffer("{\"str-key\":\"value\",\"date-key\":\"2013-12-03T12:27:54.624Z\",\"array-key\":[1,2,3,4]}"));

        assertEquals(array, obj2.getArray("array-key"));
    }

    @Test
    public void testChaining() {
        JsonObject obj = new JsonObject();
        obj.putAt("str-key", "value").put("date-key", new Date(1386234960));

        JsonArray array = new JsonArray(1, 2, 3, 4);
        obj.put("array-key", array);

        assertEquals("{\"str-key\":\"value\",\"date-key\":\"1970-01-17T01:03:54.960Z\",\"array-key\":[1,2,3,4]}", JSON.encode(obj));

        JsonObject obj2 = JSON.decodeObject(new Buffer("{\"str-key\":\"value\",\"date-key\":\"2013-12-03T12:27:54.624Z\",\"array-key\":[1,2,3,4]}"));

        assertEquals(array, obj2.getArray("array-key"));
    }
}

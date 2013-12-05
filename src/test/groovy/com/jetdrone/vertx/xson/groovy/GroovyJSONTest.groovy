package com.jetdrone.vertx.xson.groovy

import com.jetdrone.vertx.xson.java.JsonArray
import com.jetdrone.vertx.xson.java.JsonObject
import org.junit.Test
import org.vertx.groovy.core.buffer.Buffer

class GroovyJSONTest {

    @Test
    public void testSimpleGroovy() {
        JsonObject obj = new JsonObject()

        obj['str-key'] = 'value'
        obj['date-key'] = new Date(1386234960)

        obj['array-key'] = new JsonArray(1, 2, 3, 4)

        System.out.println(JSON.encode(obj))

        JsonObject obj2 = JSON.decodeObject(new Buffer("{\"str-key\":\"value\",\"date-key\":\"2013-12-03T12:27:54.624Z\",\"array-key\":[1,2,3,4]}"))

        System.out.println(obj2.getArray("array-key"))
    }
}

package com.jetdrone.vertx.xson.java;

import org.vertx.java.core.buffer.Buffer;

import java.util.Map;

public final class BSON extends com.jetdrone.vertx.xson.core.BSON {

    private BSON() {}

    public static JsonObject decodeObject(Buffer source) {
        Map<String, Object> bson = decode(source);

        if (bson == null) {
            return null;
        }

        return new JsonObject(bson);
    }

}

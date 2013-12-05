package com.jetdrone.vertx.xson.java;

import java.util.Date;

public interface JsonElement<K> {

    <V> V getAt(K keyOrIndex);

    <V> JsonElement<K> putAt(K keyOrIndex, V value);

    boolean isArray();

    boolean isObject();

    JsonObject asJsonObject();

    JsonArray asJsonArray();

    // basic json types

    boolean getBoolean(K keyOrIndex);

    Number getNumber(K keyOrIndex);

    String getString(K keyOrIndex);

    JsonArray getArray(K keyOrIndex);

    JsonObject getObject(K keyOrIndex);

    // extension

    byte[] getBinary(K keyOrIndex);

    Date getDate(K keyOrIndex);
}
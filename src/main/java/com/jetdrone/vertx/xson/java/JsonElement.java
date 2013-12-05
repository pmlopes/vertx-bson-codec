package com.jetdrone.vertx.xson.java;

public interface JsonElement<K> {

    <V> V getAt(K keyOrIndex);

    <V> JsonElement<K> putAt(K keyOrIndex, V value);

    boolean isArray();

    boolean isObject();

    JsonObject asJsonObject();

    JsonArray asJsonArray();
}
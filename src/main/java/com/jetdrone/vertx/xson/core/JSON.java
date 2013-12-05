package com.jetdrone.vertx.xson.core;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;

import com.jetdrone.vertx.xson.core.impl.ThreadLocalUTCDateFormat;

import org.vertx.java.core.json.DecodeException;
import org.vertx.java.core.json.EncodeException;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

public abstract class JSON {

    // date formatter
    public static final ThreadLocalUTCDateFormat DATE_FORMAT = new ThreadLocalUTCDateFormat();

    // create ObjectMapper instance
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final ObjectMapper ppMapper = new ObjectMapper();
    // extensions
    private static final SimpleModule EcmaCompat;

    static {
        // Do not crash if more data than the expected is received (should not happen since we have maps and list)
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ppMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // Non-standard JSON but we allow C style comments in our JSON (Vert.x default to true, so keep it for compatibility)
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        ppMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        // custom serializers
        EcmaCompat = new SimpleModule("ECMA+Custom Compat Layer");
        // serialize Dates as per ECMAScript SPEC
        EcmaCompat.addSerializer(new JsonSerializer<Date>() {
            @Override
            public Class<Date> handledType() {
                return Date.class;
            }

            @Override
            public void serialize(Date value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
                if (value == null) {
                    jgen.writeNull();
                } else {
                    jgen.writeString(DATE_FORMAT.format(value));
                }
            }
        });

        mapper.registerModule(EcmaCompat);
        ppMapper.registerModule(EcmaCompat);
    }

    public static void addSerializer(final JsonSerializer serializer) {
        // Serialize Custom Types
        EcmaCompat.addSerializer(serializer);
    }

    public static String encode(Map jsObject) {
        return encode(jsObject, false);
    }

    public static String encode(List jsArray) {
        return encode(jsArray, false);
    }

    public static String encodePretty(Map jsObject) {
        return encode(jsObject, true);
    }

    public static String encodePretty(List jsArray) {
        return encode(jsArray, true);
    }

    private static String encode(Object item, boolean prettyPrint) {
        try {
            if (prettyPrint) {
                return ppMapper.writeValueAsString(item);
            }
            return mapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            throw new EncodeException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static <R> R decode(byte[] source) {
        if (source == null) {
            return null;
        }

        try {
            // Untyped List/Map
            return (R) mapper.readValue(source, Object.class);
        } catch (IOException e) {
            throw new DecodeException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> decodeObject(byte[] source) {
        if (source == null) {
            return null;
        }

        try {
            return mapper.readValue(source, Map.class);
        } catch (IOException e) {
            throw new DecodeException(e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Object> decodeArray(byte[] source) {
        if (source == null) {
            return null;
        }

        try {
            return mapper.readValue(source, List.class);
        } catch (IOException e) {
            throw new DecodeException(e.getMessage());
        }
    }
}

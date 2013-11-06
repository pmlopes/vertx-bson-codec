package com.jetdrone.vertx.mods.bson;

import org.junit.Test;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.testtools.TestVerticle;

import java.util.HashMap;
import java.util.Map;

import static org.vertx.testtools.VertxAssert.*;

public class ModuleIntegrationTest extends TestVerticle {

    @Test
    public void testSimpleTimes2HandlerOverBSON() {

        EventBus eb = getVertx().eventBus();

        eb.registerHandler("bson.times2.handler", new Handler<Message<Buffer>>() {
            @Override
            public void handle(Message<Buffer> message) {
                Map obj = BSON.decode(message.body());
                assertEquals(5,  obj.get("value"));

                Map<String, Integer> replyMsg = new HashMap<>();
                replyMsg.put("value", ((Integer) obj.get("value")) * 2);

                message.reply(BSON.encodeMap(replyMsg));
            }
        });

        Map<String, Integer> msg = new HashMap<>();
        msg.put("value", 5);

        eb.send("bson.times2.handler", BSON.encodeMap(msg), new Handler<Message<Buffer>>() {
            @Override
            public void handle(Message<Buffer> message) {
                Map answer = BSON.decode(message.body());
                assertEquals(10, answer.get("value"));
                testComplete();
            }
        });
    }

    @Test
    public void testSimpleTimes2LocalHandlerOverBSON() {

        EventBus eb = getVertx().eventBus();

        eb.registerLocalHandler("bson.local.times2.handler", new Handler<Message<Buffer>>() {
            @Override
            public void handle(Message<Buffer> message) {
                Map obj = BSON.decode(message.body());
                assertEquals(5, obj.get("value"));

                Map<String, Integer> replyMsg = new HashMap<>();
                replyMsg.put("value", ((Integer) obj.get("value")) * 2);

                message.reply(BSON.encodeMap(replyMsg));
            }
        });

        Map<String, Integer> msg = new HashMap<>();
        msg.put("value", 5);

        eb.send("bson.local.times2.handler", BSON.encodeMap(msg), new Handler<Message<Buffer>>() {
            @Override
            public void handle(Message<Buffer> message) {
                Map answer = BSON.decode(message.body());
                assertEquals(10, answer.get("value"));
                testComplete();
            }
        });
    }

    @Test
    public void testSimpleTimes2HandlerOverJSON() {

        EventBus eventBus = getVertx().eventBus();

        eventBus.registerHandler("json.times2.handler", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> jsonMessage) {
                assertEquals(5,  (int) jsonMessage.body().getInteger("value"));

                JsonObject replyMessage = new JsonObject();
                replyMessage.putNumber("value", jsonMessage.body().getInteger("value") * 2);

                jsonMessage.reply(replyMessage);
            }
        });

        JsonObject msg = new JsonObject();
        msg.putNumber("value", 5);

        eventBus.send("json.times2.handler", msg, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                assertEquals(10, message.body().getNumber("value"));
                testComplete();
            }
        });
    }
}

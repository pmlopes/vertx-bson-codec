package com.jetdrone.vertx.bson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.test.VertxConfiguration;

import com.jetdrone.bson.vertx.eventbus.BSONEventBus;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.test.VertxTestBase;
import org.vertx.java.test.junit.VertxJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

@RunWith(VertxJUnit4ClassRunner.class)
@VertxConfiguration
public class ModuleIntegrationTest extends VertxTestBase {
    @Test
    public void testSimpleTimes2HandlerOverBSON() {
        BSONEventBus bsonEventBus = new BSONEventBus(getVertx());

        bsonEventBus.registerHandler("bson.times2.handler", new Handler<Message<Map>>() {
            @Override
            public void handle(Message<Map> mapMessage) {
                assertEquals(5,  mapMessage.body.get("value"));

                Map<String, Integer> replyMsg = new HashMap<>();
                replyMsg.put("value", ((Integer) mapMessage.body.get("value")) * 2);

                mapMessage.reply(replyMsg);
            }
        });

        Map<String, Integer> msg = new HashMap<>();
        msg.put("value", 5);

        bsonEventBus.send("bson.times2.handler", msg, new Handler<Message<Map>>() {
            @Override
            public void handle(Message<Map> mapMessage) {
                assertEquals(10, mapMessage.body.get("value"));
            }
        });
    }

    @Test
    public void testSimpleTimes2HandlerOverJSON() {
        EventBus eventBus = getVertx().eventBus();

        eventBus.registerHandler("bson.times2.handler", new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> jsonMessage) {
                assertEquals(5,  (int) jsonMessage.body.getInteger("value"));

                JsonObject replyMessage = new JsonObject();
                replyMessage.putNumber("value", jsonMessage.body.getInteger("value") * 2);

                jsonMessage.reply(replyMessage);
            }
        });

        JsonObject msg = new JsonObject();
        msg.putNumber("value", 5);

        eventBus.send("bson.times2.handler", msg, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> mapMessage) {
                assertEquals(10,  (int) mapMessage.body.getInteger("value"));
            }
        });
    }
}

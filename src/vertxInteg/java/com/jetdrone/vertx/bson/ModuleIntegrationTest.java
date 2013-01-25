package com.jetdrone.vertx.bson;

import com.jetdrone.bson.BSONElement;
import com.jetdrone.bson.BSONObject;
import org.junit.Before;
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
import org.vertx.java.test.utils.QueueReplyHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(VertxJUnit4ClassRunner.class)
@VertxConfiguration
public class ModuleIntegrationTest extends VertxTestBase {

    private long timeout = 2;

    @Before
    public void setup() {
        this.timeout = Long.parseLong(System.getProperty("vertx.test.timeout", "2"));
    }

    @Test
    public void testSimpleTimes2HandlerOverBSON() {

        final LinkedBlockingQueue<Map> queue = new LinkedBlockingQueue<>();

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

        bsonEventBus.send("bson.times2.handler", msg, new QueueReplyHandler<Map>(queue, timeout));

        try {
            Map answer = queue.poll(timeout, TimeUnit.SECONDS);
            assertEquals(10, answer.get("value"));
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSimpleTimes2LocalHandlerOverBSON() {

        final LinkedBlockingQueue<Map> queue = new LinkedBlockingQueue<>();

        BSONEventBus bsonEventBus = new BSONEventBus(getVertx());

        bsonEventBus.registerLocalHandler("bson.local.times2.handler", new Handler<Message<Map>>() {
            @Override
            public void handle(Message<Map> mapMessage) {
                assertEquals(5, mapMessage.body.get("value"));

                Map<String, Integer> replyMsg = new HashMap<>();
                replyMsg.put("value", ((Integer) mapMessage.body.get("value")) * 2);

                mapMessage.reply(replyMsg);
            }
        });

        Map<String, Integer> msg = new HashMap<>();
        msg.put("value", 5);

        bsonEventBus.send("bson.local.times2.handler", msg, new QueueReplyHandler<Map>(queue, timeout));

        try {
            Map answer = queue.poll(timeout, TimeUnit.SECONDS);
            assertEquals(10, answer.get("value"));
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSimpleTimes2HandlerOverJSON() {

        final LinkedBlockingQueue<JsonObject> queue = new LinkedBlockingQueue<>();

        EventBus eventBus = getVertx().eventBus();

        eventBus.registerHandler("json.times2.handler", new Handler<Message<JsonObject>>() {
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

        eventBus.send("json.times2.handler", msg, new QueueReplyHandler<JsonObject>(queue, timeout));

        try {
            JsonObject answer = queue.poll(timeout, TimeUnit.SECONDS);
            assertEquals(10, (int) answer.getInteger("value"));
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }

    public static class MyTest implements BSONObject {
        @BSONElement
        public int value = 5;
    }

    @Test
    public void testSimpleBSONObjectHandler() {

        final LinkedBlockingQueue<Map> queue = new LinkedBlockingQueue<>();

        BSONEventBus bsonEventBus = new BSONEventBus(getVertx());

        bsonEventBus.registerHandler("bson.BSONObject.handler", new Handler<Message<Map>>() {
            @Override
            public void handle(Message<Map> mapMessage) {
                Map<String, Object> data = new HashMap<>();
                data.put("value", mapMessage.body.get("value"));
                queue.offer(data);
            }
        });

        MyTest myTest = new MyTest();
        // by default the initial value is 5 (see above)

        bsonEventBus.send("bson.BSONObject.handler", myTest);

        try {
            Map answer = queue.poll(timeout, TimeUnit.SECONDS);
            assertEquals(5, answer.get("value"));
        } catch (InterruptedException e) {
            fail(e.getMessage());
        }
    }
}

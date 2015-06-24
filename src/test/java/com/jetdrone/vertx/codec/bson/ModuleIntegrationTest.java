package com.jetdrone.vertx.codec.bson;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.test.core.VertxTestBase;
import org.junit.Test;

public class ModuleIntegrationTest extends VertxTestBase {

  @Test
  public void testSimpleTimes2ConsumerOverBSON() {

    EventBus eb = vertx.eventBus();

    eb.registerDefaultCodec(BSONDocument.class, new BSONMessageCodec());

    Handler<Message<BSONDocument>> times2 = (msg) -> {
      assertEquals(5, msg.body().get("value"));

      BSONDocument replyMsg = new BSONDocument();
      replyMsg.put("value", ((Integer) msg.body().get("value")) * 2);

      msg.reply(replyMsg);
    };

    eb.consumer("bson.times2.handler", times2);

    BSONDocument msg = new BSONDocument();
    msg.put("value", 5);


    Handler<AsyncResult<Message<BSONDocument>>> cb = (reply) -> {
      if (reply.failed()) {
        fail();
      } else {
        assertEquals(10, reply.result().body().get("value"));
        testComplete();
      }
    };

    eb.send("bson.times2.handler", msg, cb);
    await();
  }

  @Test
  public void testSimpleTimes2LocalHandlerOverBSON() {

    EventBus eb = vertx.eventBus();

    eb.registerDefaultCodec(BSONDocument.class, new BSONMessageCodec());

    eb.localConsumer("bson.local.times2.handler", new Handler<Message<BSONDocument>>() {
      @Override
      public void handle(Message<BSONDocument> message) {
        assertEquals(5, message.body().get("value"));

        BSONDocument replyMsg = new BSONDocument();
        replyMsg.put("value", ((Integer) message.body().get("value")) * 2);

        message.reply(replyMsg);
      }
    });

    BSONDocument msg = new BSONDocument();
    msg.put("value", 5);

    Handler<AsyncResult<Message<BSONDocument>>> cb = (reply) -> {
      if (reply.failed()) {
        fail();
      } else {
        assertEquals(10, reply.result().body().get("value"));
        testComplete();
      }
    };

    eb.send("bson.local.times2.handler", msg, cb);
    await();
  }
}

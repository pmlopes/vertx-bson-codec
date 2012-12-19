import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.deploy.Verticle;

import com.jetdrone.bson.vertx.eventbus.BSONEventBus;

import java.util.HashMap;
import java.util.Map;

public class App extends Verticle {
    public void start() {

        BSONEventBus bsonEventBus = new BSONEventBus(vertx);

        bsonEventBus.registerHandler("bson.times2.handler", new Handler<Message<Map>>() {
            @Override
            public void handle(Message<Map> mapMessage) {
                System.out.println("Original value is " + mapMessage.body.get("value"));

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
                System.out.println("Received value is " + mapMessage.body.get("value"));
            }
        });
    }
}
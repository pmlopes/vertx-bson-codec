import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.deploy.Verticle;

import com.jetdrone.vertx.mods.bson.BSON;

import java.util.HashMap;
import java.util.Map;

public class App extends Verticle {
    public void start() {

        EventBus eb = vertx.eventBus();

        eb.registerHandler("bson.times2.handler", new Handler<Message<Buffer>>() {
            @Override
            public void handle(Message<Buffer> message) {
                Map obj = BSON.decode(message.body);
                System.out.println("Original value is " + obj.get("value"));

                Map<String, Integer> replyMsg = new HashMap<>();
                replyMsg.put("value", ((Integer) mapMessage.body.get("value")) * 2);

                mapMessage.reply(BSON.encode(replyMsg));
            }
        });

        Map<String, Integer> msg = new HashMap<>();
        msg.put("value", 5);

        eb.send("bson.times2.handler", BSON.encode(msg), new Handler<Message<Buffer>>() {
            @Override
            public void handle(Message<Buffer> message) {
                Map obj = BSON.decode(message.body);
                System.out.println("Received value is " + obj.get("value"));
            }
        });
    }
}
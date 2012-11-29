package bson.vertx.eventbus;

import groovy.lang.Closure;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BSONEventBus {

    private static Handler<Message<Buffer>> wrapHandler(final Handler<Message<Map>> replyHandler) {
        if (replyHandler == null) {
            return null;
        }

        return new Handler<Message<Buffer>>() {
            @Override
            public void handle(final Message<Buffer> message) {
                // convert to
                Message<Map> bsonMessage = new Message<Map>() {
                    @Override
                    public void reply(Map document, Handler<Message<Map>> messageHandler) {
                        message.reply(BSONCodec.encode(document), wrapHandler(messageHandler));
                    }
                };

                bsonMessage.body = BSONCodec.decode(message.body);
                replyHandler.handle(bsonMessage);
            }
        };
    }

    private static Handler<Message<Buffer>> wrapClosure(final Closure<Void> closure) {
        if (closure == null) {
            return null;
        }

        return new Handler<Message<Buffer>>() {
            @Override
            public void handle(final Message<Buffer> message) {
                // convert to
                Message<Map> bsonMessage = new Message<Map>() {
                    @Override
                    public void reply(Map document, Handler<Message<Map>> messageHandler) {
                        message.reply(BSONCodec.encode(document), wrapHandler(messageHandler));
                    }
                };

                bsonMessage.body = BSONCodec.decode(message.body);
                closure.call(bsonMessage);
            }
        };
    }

    private final EventBus eventBus;
    private final Map<Object, Handler<Message<Buffer>>> handlerMap;

    // Generic
    public BSONEventBus(Object object) {
        EventBus eventBus = null;

        if (object instanceof org.vertx.java.core.Vertx) {
            eventBus = ((org.vertx.java.core.Vertx) object).eventBus();
        }

        if (object instanceof org.vertx.groovy.core.Vertx) {
            eventBus = (((org.vertx.groovy.core.Vertx) object).toJavaVertx()).eventBus();
        }

        this.eventBus = eventBus;
        this.handlerMap = new ConcurrentHashMap<>();
    }

    public BSONEventBus(org.vertx.java.core.Vertx vertx) {
        this.eventBus = vertx.eventBus();
        this.handlerMap = new ConcurrentHashMap<>();
    }

    // Groovy
    public BSONEventBus(org.vertx.groovy.core.Vertx vertx) {
        this(vertx.toJavaVertx());
    }

    public void registerLocalHandler(String address, Handler<Message<Map>> handler) {
        Handler<Message<Buffer>> wrapped = wrapHandler(handler);
        handlerMap.put(handler, wrapped);
        eventBus.registerLocalHandler(address, wrapped);
    }

    // Groovy
    public void registerLocalHandler(String address, Closure<Void> handler) {
        Handler<Message<Buffer>> wrapped = wrapClosure(handler);
        handlerMap.put(handler, wrapped);
        eventBus.registerLocalHandler(address, wrapped);
    }

    public void registerHandler(String address, Handler<Message<Map>> handler) {
        Handler<Message<Buffer>> wrapped = wrapHandler(handler);
        handlerMap.put(handler, wrapped);
        eventBus.registerHandler(address, wrapped);
    }

    // Groovy
    public void registerHandler(String address, Closure<Void> handler) {
        Handler<Message<Buffer>> wrapped = wrapClosure(handler);
        handlerMap.put(handler, wrapped);
        eventBus.registerHandler(address, wrapped);
    }

    public void unregisterHandler(String address, Handler<Message<Map>> handler) {
        Handler<Message<Buffer>> wrapped = handlerMap.remove(handler);
        if (wrapped != null) {
            eventBus.unregisterHandler(address, wrapped);
        }
    }

    // Groovy
    public void unregisterHandler(String address, Closure<Void> handler) {
        Handler<Message<Buffer>> wrapped = handlerMap.remove(handler);
        if (wrapped != null) {
            eventBus.unregisterHandler(address, wrapped);
        }
    }

    public void registerHandler(String address, Handler<Message<Map>> handler, AsyncResultHandler<Void> resultHandler) {
        Handler<Message<Buffer>> wrapped = wrapHandler(handler);
        handlerMap.put(handler, wrapped);
        eventBus.registerHandler(address, wrapped, resultHandler);
    }

    // Groovy
    public void registerHandler(String address, Closure<Void> handler, Closure<Void> resultHandler) {
        Handler<Message<Buffer>> wrapped = wrapClosure(handler);
        handlerMap.put(handler, wrapped);
//        eventBus.registerHandler(address, wrapped, resultHandler);
    }

    public void unregisterHandler(String address, Handler<Message<Map>> handler, AsyncResultHandler<Void> resultHandler) {
        Handler<Message<Buffer>> wrapped = handlerMap.remove(handler);
        if (wrapped != null) {
            eventBus.unregisterHandler(address, wrapped, resultHandler);
        }
    }

    // Groovy
    public void unregisterHandler(String address, Closure<Void> handler, Closure<Void> resultHandler) {
        Handler<Message<Buffer>> wrapped = handlerMap.remove(handler);
        if (wrapped != null) {
//            eventBus.unregisterHandler(address, wrapped, resultHandler);
        }
    }

    public void send(String address, Map message, Handler<Message<Map>> replyHandler) {
        Buffer _message = BSONCodec.encode(message);
        eventBus.send(address, _message, wrapHandler(replyHandler));
    }

    // Groovy
    public void send(String address, Map message, Closure<Void> replyHandler) {
        eventBus.send(address, BSONCodec.encode(message), wrapClosure(replyHandler));
    }

    public void send(String address, Map message) {
        eventBus.send(address, BSONCodec.encode(message));
    }

    public void publish(String address, Map message) {
        eventBus.publish(address, BSONCodec.encode(message));
    }
}

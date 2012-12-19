import com.jetdrone.bson.vertx.eventbus.BSONEventBus

def bsonEventBus = new BSONEventBus(vertx)

bsonEventBus.registerHandler('bson.times2.handler') { message ->
    println "Original value is ${message.body?.value}"

    message.reply([value: message.body?.value * 2])
}

def msg = [value: 5]

bsonEventBus.send('bson.times2.handler', msg) { message ->
    println "Received value is ${message.body?.value}"
}
import com.jetdrone.vertx.mods.bson.BSON

def eb = vertx.eventBus()

eb.registerHandler('bson.times2.handler') { message ->
    def obj = BSON.decode(message.body)
    println "Original value is ${obj?.value}"

    message.reply(BSON.encode([value: message.body?.value * 2]))
}

def msg = BSON.encode([value: 5])

bsonEventBus.send('bson.times2.handler', msg) { message ->
    def obj = BSON.decode(message.body)
    println "Received value is ${obj?.value}"
}
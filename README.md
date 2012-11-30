bson.vertx.eventbus
===================
[![Build Status](https://travis-ci.org/pmlopes/bson.vertx.eventbus.png)](https://travis-ci.org/pmlopes/bson.vertx.eventbus)

BSON EventBus for Vert.x

The module has no external dependencies, I've decided to implement the bson codec myself to optimize the usage of vert.x
buffers, and for that reason it is quite basic for the moment.

Right now it encodes/decodes:

* java.util.Map
* java.util.List
* java.lang.String
* java.lang.Integer
* java.lang.Long
* java.lang.Double
* java.util.Date
* java.util.regex.Pattern
* byte[] (binary)
* java.lang.Boolean
* null

Planned but not implemented yet:
* java.util.UUID
* and the remaining types if i find a proper mapping to native jdk types.

I also put together 2 samples (one in java and another in groovy)

* https://github.com/pmlopes/bson.vertx.eventbus/tree/master/example/mods/bson.example-groovy-v1.0
* https://github.com/pmlopes/bson.vertx.eventbus/tree/master/example/mods/bson.example-java-v1.0
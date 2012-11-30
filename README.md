bson.vertx.eventbus
===================
[![Build Status](https://travis-ci.org/pmlopes/bson.vertx.eventbus.png)](https://travis-ci.org/pmlopes/bson.vertx.eventbus)

BSON EventBus for Vert.x

Why another event bus?
----------------------
The standard EventBus from Vert.x allows Verticles to communicate with each other using JSON. JSON is a generic and fairly
simple encoding scheme, however it limits the data types to be:

* Number (normally a Double)
* String
* Boolean
* Array (Ordered List)
* Object (Map)
* null

Although for most cases these data types are enough, in some more complex cases a Verticle might need to exchange more
rich type data such as Dates, Integers, UUIDs, Regular Expressions, Binary data. This is where the BSON EventBus comes in.

Implementation details
----------------------
The module has no external dependencies, I've decided to implement the bson codec myself to optimize the usage of vert.x
buffers, and for that reason it is quite basic for the moment.

Supported Data Types
--------------------
At this moment, BSON Event Bus is capable of handling the following types:

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
* java.util.UUID

Not implemented yet
-------------------
The following types have not been implemented yet, however they are defined in the bson spec:

* Binary Function
* Binary OLD (deprecated)
* Binary UUID OLD (deprecated)
* Binary MD5 (is there a candidate Class in the JDK that i can map?)
* Binary User Defined
* Undefined (deprecated)
* ObjectId
* DBPointer (deprecated)
* JS Code
* JS Code with context
* Symbol
* Mongo Timestamp
* MinKey (what are these types?)
* MaxKey (what are these types?)

Quickstart
----------
There are 2 examples in the code:

* https://github.com/pmlopes/bson.vertx.eventbus/tree/master/example/mods/bson.example-groovy-v1.0
* https://github.com/pmlopes/bson.vertx.eventbus/tree/master/example/mods/bson.example-java-v1.0

In a quick overview all you need is:

1. Create a new instance of the BSONEventBus by wrapping the default EventBus
2. Send and receive java.util.Map as your Objects that are internally converted to BSON

The transformation to and from BSON is totally hidden from you. If you used vert.x with JSON encoding BSON will look
exactly the same. However instead of having specific classes (like it happens for JSON) BSON works with Maps directly.
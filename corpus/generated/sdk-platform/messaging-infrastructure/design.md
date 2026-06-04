# Design

The following sections describe the main components of the messaging API:

* [Topic](#topic)

* [Message Bus](#message-bus)

* [Connection](#connection)

### Topic

The [Topic](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/extensions/src/com/intellij/util/messages/Topic.java) class serves as an endpoint at the messaging infrastructure.
Clients are allowed to subscribe to a specific topic within a bus and send messages to that topic within that particular bus.
To clarify the corresponding message bus, a `Topic` field declaration should be annotated with `@Topic.AppLevel` and/or `@Topic.ProjectLevel`.

```PLANTUML
@startuml

skinparam monochrome true
skinparam DefaultFontName JetBrains Sans
skinparam DefaultFontSize 14
skinparam classAttributeIconSize 0
hide empty fields
hide empty methods

left to right direction

class "com.intellij.util.messages.Topic" as Topic {
  +getDisplayName()
  +getBroadcastDirection()
}

class ListenerClass {
  +method1()
  {method} ...
  +methodN()
}

Topic o--> "1 " ListenerClass

@enduml
```

#### Topic Properties

Display name
: Human-readable name used for logging/monitoring purposes.

Broadcast direction
: See [Broadcasting](#broadcasting) for more details. The default value is `TO_CHILDREN`.

Listener class
: A business interface for a particular topic.
Subscribers register an implementation of this interface at the messaging infrastructure.
Publishers later retrieve objects that conform to the interface (IS-A) and call any methods defined on those implementations.
The messaging infrastructure takes care of dispatching the message to all subscribers of the topic by calling the same method with the same arguments on the registered implementation callbacks.

### Message Bus

[MessageBus](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/extensions/src/com/intellij/util/messages/MessageBus.kt) is the core of the messaging system.
It is used in the following scenarios:

```PLANTUML
@startuml

skinparam monochrome true
skinparam DefaultFontName JetBrains Sans
skinparam DefaultFontSize 14

:Subscriber:
(Create connection) as (C)
note top of (C): Necessary for subscribing
Subscriber --> C

:Publisher:
(Publish)
Publisher --> Publish

@enduml
```

### Connection

Connection is represented by [MessageBusConnection](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/extensions/src/com/intellij/util/messages/MessageBusConnection.kt) class and manages all subscriptions for a particular client within a particular bus.

```PLANTUML
@startuml

skinparam monochrome true
skinparam DefaultFontName JetBrains Sans
skinparam DefaultFontSize 14
hide empty members
hide circle

class MessageBus
class MessageBusConnection
class "Default Handler" as DH
class "(Topic-Handler)" as TH

MessageBus "1" o-- "*" MessageBusConnection
MessageBusConnection o-- "0..1" DH
MessageBusConnection *-- "*" TH

@enduml
```

Connection stores topic-handler mappings - callbacks to invoke when message for the target topic is received (not more than one handler per topic within the same connection is allowed).

It's possible to specify default handler and subscribe to the target topic without explicitly provided callback.
Connection will use that default handler when storing a topic-handler mapping.

It's possible to explicitly release acquired resources (see `disconnect()`).
Also, it can be plugged to standard semi-automatic disposing ([Disposable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/Disposable.java)).

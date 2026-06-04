---
id: sdk.messaging-infrastructure.design.connection
title: Messaging Infrastructure: Connection
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, connection]
---
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


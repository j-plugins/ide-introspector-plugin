---
id: sdk.messaging-infrastructure.nested-messages
title: Messaging Infrastructure: Nested Messages
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, nested, messages]
---
Part of `sdk.messaging-infrastructure`.

Nested message is a message sent (directly or indirectly) during another message processing.
The IntelliJ Platform's messaging infrastructure guarantees that all messages sent to particular topic will be delivered at the sending order.

Consider the following configuration:

```PLANTUML
@startuml

skinparam DefaultFontName JetBrains Sans
skinparam DefaultFontSize 14
hide empty members
hide circle

top to bottom direction

class "bus" as B

class "connection1" as C1
class "connection2" as C2

class "topic-handler1" as TH1
class "topic-handler2" as TH2


B *-- C1
B *-- C2

C1 *-- TH1
C2 *-- TH2
@enduml
```

When a message is sent to the target topic, the following happens:

* message1 is sent

* handler1 receives message1 and sends message2 to the same topic

* handler2 receives message1

* handler2 receives message2

* handler1 receives message2

> Source: IntelliJ Platform SDK docs — Messaging Infrastructure: Nested Messages (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

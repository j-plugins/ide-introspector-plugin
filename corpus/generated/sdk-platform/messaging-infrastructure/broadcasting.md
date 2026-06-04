---
id: sdk.messaging-infrastructure.broadcasting
title: Messaging Infrastructure: Broadcasting
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, broadcasting]
---
Part of `sdk.messaging-infrastructure`.

Message buses can be organised into hierarchies.
Moreover, the IntelliJ Platform has them already:

```PLANTUML
@startuml

skinparam monochrome true
skinparam DefaultFontName JetBrains Sans
skinparam DefaultFontSize 14
hide empty members
hide circle

left to right direction

' Define the objects in the diagram
class "application bus" as AB
class "project bus" as PB
class "module bus" as MB

' Define the class relationships
AB o-- "*" PB
PB o-- "*" MB
@enduml
```

That allows to notify subscribers registered in one message bus on messages sent to another message bus.

Example setup:

```PLANTUML
@startuml

skinparam monochrome true
skinparam DefaultFontName JetBrains Sans
skinparam DefaultFontSize 14
hide empty members
hide circle
top to bottom direction

class "application bus" as AB
class "project bus" as PB
class "connection1" as C1

class "connection2" as C2
class "connection3" as C3
class "topic1-handler1" as T1H1

class "topic1-handler2" as T1H2
class "topic1-handler3" as T1H3

AB o-- PB
AB *-- C1

PB *-- C2
PB *-- C3
C1 *-- T1H1

C2 *-- T1H2
C3 *-- T1H3

@enduml
```

The example setup presents a simple hierarchy (the application bus is a parent of the project bus) with three subscribers for the same topic.

If topic1 defines broadcast direction as `TO_CHILDREN`, we get the following:

1. A message is sent to topic1 via application bus.

2. handler1 is notified about the message.

3. The message is delivered to the subscribers of the same topic within project bus (handler2 and handler3).

The main benefit of broadcasting is managing subscribers that are bound to child buses but interested in parent bus-level events.
In the example above, we may want to have project-specific functionality that reacts to the application-level events.
All we need to do is to subscribe to the target topic within the project bus.
No hard reference to the project-level subscriber will be stored at application-level then, i.e., we just avoided memory leak on project re-opening.

Broadcast configuration is defined per-topic.
The following options are available:

* `TO_CHILDREN` (default)

* `TO_DIRECT_CHILDREN`

* `NONE`

* `TO_PARENT`

See [Topic.BroadcastDirection](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/extensions/src/com/intellij/util/messages/Topic.java) for detailed description of each option.

> Source: IntelliJ Platform SDK docs — Messaging Infrastructure: Broadcasting (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

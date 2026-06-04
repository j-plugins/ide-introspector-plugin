# Messaging Infrastructure

IntelliJ Platform's messaging infrastructure is an implementation of [Publisher Subscriber Pattern](https://w.wiki/5xaV) that provides additional features like broadcasting on hierarchy and special nested events processing (a nested event is an event directly or indirectly fired from the callback of another event).

Tip:

All available listeners/topics are listed on [IntelliJ Platform Extension Point and Listener List](https://plugins.jetbrains.com/docs/intellij/intellij-platform-extension-point-list.html) under Listeners sections.

## Design (sdk.messaging-infrastructure.design)
## Messaging API Usage (sdk.messaging-infrastructure.messaging-api-usage)
## Broadcasting (sdk.messaging-infrastructure.broadcasting)
## Nested Messages

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

## Tips and Tricks (sdk.messaging-infrastructure.tips-and-tricks)

> Source: IntelliJ Platform SDK docs — Messaging Infrastructure (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

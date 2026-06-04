---
id: sdk.messaging-infrastructure.design.topic
title: Messaging Infrastructure: Topic
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, topic]
---
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

#### Topic Properties (messaging-infrastructure/design/topic/topic-properties.md)

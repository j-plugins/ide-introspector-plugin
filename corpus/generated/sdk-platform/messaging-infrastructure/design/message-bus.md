---
id: sdk.messaging-infrastructure.design.message-bus
title: Messaging Infrastructure: Message Bus
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, message, bus]
---
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


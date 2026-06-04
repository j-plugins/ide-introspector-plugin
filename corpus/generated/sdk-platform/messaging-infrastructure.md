---
id: sdk.messaging-infrastructure
title: Messaging Infrastructure
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, messaging, infrastructure]
---
IntelliJ Platform's messaging infrastructure is an implementation of [Publisher Subscriber Pattern](https://w.wiki/5xaV) that provides additional features like broadcasting on hierarchy and special nested events processing (a nested event is an event directly or indirectly fired from the callback of another event).

Tip:

All available listeners/topics are listed on [IntelliJ Platform Extension Point and Listener List](https://plugins.jetbrains.com/docs/intellij/intellij-platform-extension-point-list.html) under Listeners sections.

## Design (messaging-infrastructure/design.md)
### Topic (messaging-infrastructure/design/topic.md)
#### Topic Properties (messaging-infrastructure/design/topic/topic-properties.md)
### Message Bus (messaging-infrastructure/design/message-bus.md)
### Connection (messaging-infrastructure/design/connection.md)
## Messaging API Usage (messaging-infrastructure/messaging-api-usage.md)
### Defining a Business Interface and a Topic (messaging-infrastructure/messaging-api-usage/defining-a-business-interface-and-a-topic.md)
### Subscribing to a Topic (messaging-infrastructure/messaging-api-usage/subscribing-to-a-topic.md)
### Publishing Messages (messaging-infrastructure/messaging-api-usage/publishing-messages.md)
## Broadcasting (messaging-infrastructure/broadcasting.md)
## Nested Messages (messaging-infrastructure/nested-messages.md)
## Tips and Tricks (messaging-infrastructure/tips-and-tricks.md)
### Relief Listeners Management (messaging-infrastructure/tips-and-tricks/relief-listeners-management.md)
### Avoid Shared Data Modification from Subscribers (messaging-infrastructure/tips-and-tricks/avoid-shared-data-modification-from-subscribers.md)

> Source: IntelliJ Platform SDK docs — Messaging Infrastructure (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

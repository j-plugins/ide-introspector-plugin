---
id: sdk.messaging-infrastructure.design.topic.topic-properties
title: Messaging Infrastructure: Topic Properties
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, topic, properties]
---
Display name
: Human-readable name used for logging/monitoring purposes.

Broadcast direction
: See [Broadcasting](#broadcasting) for more details. The default value is `TO_CHILDREN`.

Listener class
: A business interface for a particular topic.
Subscribers register an implementation of this interface at the messaging infrastructure.
Publishers later retrieve objects that conform to the interface (IS-A) and call any methods defined on those implementations.
The messaging infrastructure takes care of dispatching the message to all subscribers of the topic by calling the same method with the same arguments on the registered implementation callbacks.


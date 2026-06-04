---
id: sdk.messaging-infrastructure.tips-and-tricks.relief-listeners-management
title: Messaging Infrastructure: Relief Listeners Management
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, relief, listeners, management]
---
Messaging infrastructure is very light-weight, so it's possible to reuse it at local sub-systems in order to relieve [Subscribers](https://en.wikipedia.org/wiki/Publish%E2%80%93subscribe_pattern) construction.
Let's see what is necessary to do then:

1. Define business interface to work with.

2. Create shared message bus and topic that uses the interface above (shared here means that either subject or subscribers know about them).

A manual implementation would require:

1. Define listener interface (business interface).

2. Provide reference to the subject to all interested listeners.

3. Add listeners storage and listeners management methods (add/remove) to the subject.

4. Manually iterate all listeners and call target callback in all places where new event is fired.


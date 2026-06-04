---
id: sdk.notification-balloons.obsolete-api
title: Notification Balloons: Obsolete API
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, obsolete, api]
---
Part of `sdk.notification-balloons`.

Previously, there were multiple SDK approaches to notification balloons.
They are considered to be obsolete or too complex.

* `NotificationGroup` is considered an internal data structure. Its factory methods are replaced with `Notification` constructor and subsequent builder methods. The `NotificationGroupManager`, its `getNotificationGroup` method are no longer necessary, as the reference to the notification group is resolved via the notification group identifier.

* The `Notifications.Bus.notify` method can be replaced with the `notify` method on the `Notification` instance.

* `NotificationsManager` is considered to be internal. Its methods should be replaced by the corresponding methods in the `Notification` class.

> Source: IntelliJ Platform SDK docs — Notification Balloons: Obsolete API (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

---
id: sdk.notification-balloons.summary
title: Notification Balloons: Summary
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, summary]
---
Part of `sdk.notification-balloons`.

* A notification group is a user-configurable channel for notifications. Directly maps to a configuration item in the IDE settings.

* Timeline notification shows for 10 seconds and is automatically dismissed. The primary action is shown as a hyperlink. Uses a `BALLOON` constant in the notification group.

* Suggestion needs to be dismissed explicitly. The primary action is shown as a button. Uses a `STICKY_BALLON` constant in the notification group. Needs to set `setNotificationType(true)` on the `Notification` instance. Suggestions are shown in a dedicated section in the Notifications tool window.

* Tool Window Notification visually points to the specific tool windows. The primary action is shown as a hyperlink. It is automatically dismissed on click or keypress.

* Use the `Notification` class to create instances, its builder methods to configure, and the `notify` method to show a notification.

> Source: IntelliJ Platform SDK docs — Notification Balloons: Summary (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

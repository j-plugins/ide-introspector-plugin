---
id: sdk.notification-balloons.configuring-notification-display-settings
title: Notification Balloons: Configuring notification display settings
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, configuring, notification, display, settings]
---
Part of `sdk.notification-balloons`.

Some use-cases require a specific display configuration for the notification balloons.
Use them carefully, as they might go against the default user experience guidelines.

### Notification balloons without entries in the Notifications tool window

Occasionally, there are notifications that do not need to be logged in the Notification tool window.
This is the standard case with Tool Window notifications.
However, there are other usages as well.
A Hotswap Reload action or a Breakpoint Hit is shown as notification balloons and then completely hidden.
Such a notification group has the `isLogByDefault` attribute set to `false`.
This matches the Show in tool window option being disabled.

### Notifications tool window entry without a notification balloon

To log a new entry into the Notifications tool entry without showing a balloon, configure the `displayType` to `NONE`.
This matches the Popup type list being set to No popup.

> Source: IntelliJ Platform SDK docs — Notification Balloons: Configuring notification display settings (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

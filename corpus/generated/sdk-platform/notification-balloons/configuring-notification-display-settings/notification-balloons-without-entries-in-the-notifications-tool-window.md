---
id: sdk.notification-balloons.configuring-notification-display-settings.notification-balloons-without-entries-in-the-notifications-tool-window
title: Notification Balloons: Notification balloons without entries in the Notifications tool window
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, notification, balloons, without, entries, notifications]
---
Occasionally, there are notifications that do not need to be logged in the Notification tool window.
This is the standard case with Tool Window notifications.
However, there are other usages as well.
A Hotswap Reload action or a Breakpoint Hit is shown as notification balloons and then completely hidden.
Such a notification group has the `isLogByDefault` attribute set to `false`.
This matches the Show in tool window option being disabled.


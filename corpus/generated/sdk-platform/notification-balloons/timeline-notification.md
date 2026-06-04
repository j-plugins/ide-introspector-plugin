---
id: sdk.notification-balloons.timeline-notification
title: Notification Balloons: Timeline notification
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, timeline, notification]
---
Next, create a new [Notification](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/notification/Notifications.java) instance.
The constructor takes three arguments:

1. Notification group ID

2. Content text

3. Type (controls the icon that's displayed)

Use the `notify` method and pass in the current project to show the notification in its associated frame.

```KOTLIN
Notification("Bagel", "Bagel was eaten", NotificationType.INFORMATION)
   .notify(e.project)
```

Tip:

Use [Plugin DevKit](https://plugins.jetbrains.com/plugin/22851-plugin-devkit/) to get the code insight for notification group identifiers.

By default, a notification is timed, as it will automatically disappear after 10 seconds. However, it will remain in the Notifications tool window, until cleared, in the Timeline section.


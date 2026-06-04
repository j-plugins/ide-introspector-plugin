---
id: sdk.notification-balloons.notification-title-and-body
title: Notification Balloons: Notification Title and Body
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, notification, title, body]
---
To give more context, [use a title and a body](https://plugins.jetbrains.com/docs/intellij/balloon.html#text).
The title briefly describes what happened, and the body explains the impact or what the user can do about it.
The overloaded constructor takes an extra string before the content – that’s the title.

```KOTLIN
Notification("Bagel", "Bagel was eaten", getBagelCounterMessage(), NotificationType.INFORMATION)
  .notify(e.project)
```

Note:

The notification body may contain HTML code for presentation purposes.


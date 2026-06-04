---
id: sdk.notification-balloons.icons
title: Notification Balloons: Icons
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, icons]
---
Part of `sdk.notification-balloons`.

The [UI guidelines recommend](https://plugins.jetbrains.com/docs/intellij/balloon.html#information) using a plugin or functionality icon instead of the generic Information icon.
[Provide an icon](https://plugins.jetbrains.com/docs/intellij/icons.html#icons-class) along with its accompanying constant.
The `Notification.setIcon` overrides the icon from the constructor argument.

```KOTLIN
Notification("Bagel", "Bagel was eaten", NotificationType.INFORMATION)
  .setIcon(Icons.Bagel)
  //...
```

> Source: IntelliJ Platform SDK docs — Notification Balloons: Icons (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

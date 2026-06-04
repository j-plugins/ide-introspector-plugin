---
id: sdk.notification-balloons.notification-group
title: Notification Balloons: Notification Group
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, notification, group]
---
Declare a notification group as an extension in the plugin descriptor.
It is registered using the [com.intellij.notificationGroup](https://jb.gg/ipe?extensions=com.intellij.notificationGroup) extension point.
Set the display type to `BALLOON` constant. Then, provide a human-readable identifier.

```XML
<extensions defaultExtensionNs="com.intellij">
   <notificationGroup id="Bagel" displayType="BALLOON" />
</extensions>
```

Tip:

A good ID completes this phrase: "Notifications in this group tell the user about…". See the [UI Guidelines for further tips on naming conventions](https://plugins.jetbrains.com/docs/intellij/balloon.html#naming-a-notification-group).


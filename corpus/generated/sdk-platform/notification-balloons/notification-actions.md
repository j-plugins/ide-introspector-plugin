---
id: sdk.notification-balloons.notification-actions
title: Notification Balloons: Notification Actions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, notification, actions]
---
A notification balloon [can contain actions](https://plugins.jetbrains.com/docs/intellij/balloon.html#actions), rendered as a link or button.

```KOTLIN
Notification("Bagel", "Bagel was eaten", NotificationType.INFORMATION)
  .addAction(NotificationAction.createSimpleExpiring("Track calories") {
    // add an action
  })
  .notify(e.project)
```

Chain `addAction` method with `createSimpleExpiring` method, which provides a trailing lambda for the action handler.
When the user clicks it, the notification dismisses automatically.
However, it will stay in the Notification tool window with the hyperlink greyed-out.

### Multiple Actions (notification-balloons/notification-actions/multiple-actions.md)

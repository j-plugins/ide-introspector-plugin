---
id: sdk.notification-balloons.tool-window-notifications
title: Notification Balloons: Tool Window Notifications
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, tool, window, notifications]
---
A [tool window](https://plugins.jetbrains.com/docs/intellij/tool-windows.html) can trigger a long-running operation.
For example, the Find in Files action takes a couple of seconds to search for a string in a large project tree, doing that in the background.
When there are no matches, a notification balloon is shown.
However, instead of the usual location, the notification balloon is displayed directly next to the tool window icon.
In the plugin descriptor, declare the notification group with a display type set to `TOOL_WINDOW`.
As the notification group’s notifications are explicitly bound to a specific tool window, provide its identifier in the `toolWindowId` attribute.

```XML
<notificationGroup
  id="Order in Bakery"
  displayType="TOOL_WINDOW"
  toolWindowId="Bakery"
/>

<toolWindow
  id="Bakery"
  ...
/>
```

The `toolWindowId` matches the `id` value declared in the `com.intellij.toolWindow` extension.

In the code, use the standard `Notification.notify` method to show it.

```KOTLIN
Notification("Order in Bakery", "Bagel is ready", NotificationType.INFORMATION)
  .addAction(NotificationAction.createSimpleExpiring("Eat bagel") {
      // handle 'Eat bagel' link
   })
notification.notify(e.project)
```

Such notification is automatically dismissed on any keypress or mouse click.
Due to space constraints, the notification title should be as short as possible.
Additionally, use at most one notification action, which will be rendered as a hyperlink.
Generally, the tool window notifications don’t need to be shown in the Notifications tool window.
See the [corresponding section](#notification-balloons-without-entries-in-the-notifications-tool-window) for the implementation.


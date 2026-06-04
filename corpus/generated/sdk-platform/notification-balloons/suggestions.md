---
id: sdk.notification-balloons.suggestions
title: Notification Balloons: Suggestions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, suggestions]
---
Part of `sdk.notification-balloons`.

In some cases, the functionality needs to prompt or notify the user to take action or provide input.

[Suggestions](https://plugins.jetbrains.com/docs/intellij/balloon.html#suggest-an-action-to-configure-a-project-or-an-ide) show the primary action as a noticeable button.
Unlike timed notifications, suggestions won’t go away on their own.
The user has to act and dismiss them explicitly.
Their notification group is configured as `STICKY_BALLOON`.

```XML
<notificationGroup id="Bagel File"
                   displayType="STICKY_BALLOON" />
```

To mark the notification as a suggestion, set its suggestion type to `true`.

```KOTLIN
Notification("Bagel File", "Bagel file detected", NotificationType.INFORMATION)
  .setSuggestionType(true)
  // ...
```

Suggestions have a dedicated section in the Notifications tool window.

Tip:

A `Notification` has the `expire` method for explicit expirations.

> Source: IntelliJ Platform SDK docs — Notification Balloons: Suggestions (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

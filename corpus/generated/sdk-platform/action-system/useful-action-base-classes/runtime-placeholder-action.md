---
id: sdk.action-system.useful-action-base-classes.runtime-placeholder-action
title: Action System: Runtime Placeholder Action
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, runtime, placeholder, action]
---
For actions registered at runtime (for example, in a tool window toolbar), add an [&lt;action&gt;](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__actions__action) entry with
[EmptyAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/actionSystem/EmptyAction.java)
to "reserve" Action ID, so they become visible in `Settings | Keymap`.


---
id: sdk.action-system.action-implementation.action-ids
title: Action System: Action IDs
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, action, ids]
---
Part of `sdk.action-system.action-implementation`.

Each action and action group must have a unique identifier (see the `id` attribute specification for [action](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__actions__action) and [group](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__actions__group)).

An action requires a unique identifier for every context where it appears in the IDE UI, even if the implementation FQN is shared.
Standard IntelliJ Platform action IDs are defined in [IdeActions](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/actionSystem/IdeActions.java).

> Source: IntelliJ Platform SDK docs — Action System: Action IDs (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

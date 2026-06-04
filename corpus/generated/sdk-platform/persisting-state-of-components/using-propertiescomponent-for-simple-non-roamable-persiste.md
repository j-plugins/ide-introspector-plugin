---
id: sdk.persisting-state-of-components.using-propertiescomponent-for-simple-non-roamable-persiste
title: Persisting State of Components: Using `PropertiesComponent` for Simple Non-Roamable Persiste
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, using, propertiescomponent, for, simple, non]
---
Part of `sdk.persisting-state-of-components`.

Using `PropertiesComponent` for Simple Non-Roamable Persistence

If the plugin needs to persist a few simple values, the easiest way to do so is to use the [PropertiesComponent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/ide/util/PropertiesComponent.java) service.
It can save both application-level values and project-level values in the workspace file.
Roaming is disabled for `PropertiesComponent`, so use it only for temporary, non-roamable properties.

Use the `PropertiesComponent.getInstance()` method for storing application-level values and the `PropertiesComponent.getInstance(Project)` method for storing project-level values.

Since all plugins share the same namespace, it is highly recommended to prefix key names (for example, using plugin ID `com.example.myCustomSetting`).

> Source: IntelliJ Platform SDK docs — Persisting State of Components: Using `PropertiesComponent` for Simple Non-Roamable Persiste (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

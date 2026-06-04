---
id: sdk.settings-guide.extension-points-for-settings
title: Settings Guide: Extension Points for Settings
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, extension, points, for, settings]
---
Part of `sdk.settings-guide`.

Custom Settings implementations are declared in the `[plugin.xml](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)` file using one of two extension points (EP), depending on the level of the Settings.
Many [attributes](#settings-declaration-attributes) are shared between the EP declarations.

Application and Project Settings typically provide an implementation based on the [Configurable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/Configurable.java) interface because they do not have runtime dependencies.
See [Implementations for Settings Extension Points](#implementations-for-settings-extension-points) for more information.

Note:

For performance reasons, it is recommended to declare as much information as possible about a 'Settings' implementation using attributes in the EP element in the `plugin.xml` descriptor.
If it is not declared, the component must be loaded to retrieve it from the implementation, degrading UI responsiveness.

## Subtopics

- Declaring Application Settings — `sdk.settings-guide.extension-points-for-settings.declaring-application-settings`
- Declaring Project Settings — `sdk.settings-guide.extension-points-for-settings.declaring-project-settings`
- Settings Declaration Attributes — `sdk.settings-guide.extension-points-for-settings.settings-declaration-attributes`

> Source: IntelliJ Platform SDK docs — Settings Guide: Extension Points for Settings (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

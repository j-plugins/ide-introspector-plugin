---
id: sdk.settings-guide.implementations-for-settings-extension-points
title: Settings Guide: Implementations for Settings Extension Points
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, implementations, for, settings, extension, points]
---
Part of `sdk.settings-guide`.

Implementations for [com.intellij.applicationConfigurable](https://jb.gg/ipe?extensions=com.intellij.applicationConfigurable) extension point
and [com.intellij.projectConfigurable](https://jb.gg/ipe?extensions=com.intellij.projectConfigurable) extension point
can have one of two bases:

* The [Configurable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/Configurable.java) interface, which provides a named configurable component with a Swing form. Most Settings providers are based on the `Configurable` interface or one of its sub- or supertypes.

* The [ConfigurableProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/ConfigurableProvider.java) class, which can hide a configurable component from the Settings dialog based on runtime conditions.

## Subtopics

- The `Configurable` Interface — `sdk.settings-guide.implementations-for-settings-extension-points.the-configurable-interface`
- The `ConfigurableProvider` Class — `sdk.settings-guide.implementations-for-settings-extension-points.the-configurableprovider-class`

> Source: IntelliJ Platform SDK docs — Settings Guide: Implementations for Settings Extension Points (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

---
id: sdk.custom-settings-groups.implementations-for-parent-child-settings
title: Custom Settings Groups: Implementations for Parent-Child Settings
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, implementations, for, parent, child, settings]
---
Part of `sdk.custom-settings-groups`.

Implementations can be based on [Configurable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/Configurable.java), [ConfigurableProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/ConfigurableProvider.java) or one of their subtypes.
For more information about creating Settings implementations, see [Implementations for Settings Extension Points](https://plugins.jetbrains.com/docs/intellij/settings-guide.html#implementations-for-settings-extension-points).

### Configurable Marker Interfaces

The `Configurable.Composite` interface indicates a configurable component has child components.
The preferred approach is to specify child components in the [EP declaration](#extension-points-for-parent-child-settings-relationships).
Using the `Composite` interface incurs the penalty of loading child classes while building the tree of Settings Swing components.

> Source: IntelliJ Platform SDK docs — Custom Settings Groups: Implementations for Parent-Child Settings (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

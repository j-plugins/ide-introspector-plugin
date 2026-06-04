---
id: sdk.settings-guide.implementations-for-settings-extension-points.the-configurable-interface.intellij-platform-interactions-with-configurable
title: Settings Guide: IntelliJ Platform Interactions with `Configurable
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, intellij, platform, interactions, with, configurable]
---
IntelliJ Platform Interactions with `Configurable`

The instantiation of a generic `Configurable` implementation is documented in the interface file.
A few high-level points are reviewed here:

* The `Configurable.reset()` method is invoked immediately after `Configurable.createComponent()`. Initialization of Setting values in the constructor or `createComponent()` is unnecessary.

* See the [Constructors](#constructors) section for information about when a Settings object is instantiated.

* Once instantiated, a `Configurable` instance's lifetime continues regardless of whether the implementation's Settings are changed, or the user chooses a different entry on the Settings Dialog menu.

* A `Configurable` instance's lifetime ends when OK or Cancel is selected in the Settings Dialog. An instance's `Configurable.disposeUIResources()` is called when the Settings Dialog is closing.

To open the Settings dialog or show a specific `Configurable`, see [ShowSettingsUtil](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/options/ShowSettingsUtil.java).


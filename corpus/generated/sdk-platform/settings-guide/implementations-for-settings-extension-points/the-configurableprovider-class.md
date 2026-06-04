---
id: sdk.settings-guide.implementations-for-settings-extension-points.the-configurableprovider-class
title: Settings Guide: The `ConfigurableProvider` Class
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, configurableprovider, class]
---
Part of `sdk.settings-guide.implementations-for-settings-extension-points`.

The `ConfigurableProvider` Class

The [ConfigurableProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/ConfigurableProvider.java) class only provides a `Configurable` implementation if its runtime conditions are met.
The IntelliJ Platform first calls the `ConfigurableProvider.canCreateConfigurable()`, which evaluates runtime conditions to determine if Settings changes make sense in the current context.
If the Settings make sense to display, `canCreateConfigurable()` returns `true`.
In that case the IntelliJ Platform calls `ConfigurableProvider.createConfigurable()`, which returns the `Configurable` instance for its Settings implementation.

By choosing not to provide a `Configuration` implementation in some circumstances, the `ConfigurableProvider` opts out of the Settings display and modification process.
The use of `ConfigurableProvider` as a basis for a Settings implementation is declared using [attributes](#table-of-attributes) in the EP declaration.

Examples:

* [RunToolbarSettingsConfigurableProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/execution/runToolbar/RunToolbarSettingsConfigurableProvider.kt)

* [VcsManagerConfigurableProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/vcs-impl/src/com/intellij/openapi/vcs/configurable/VcsManagerConfigurableProvider.java)

> Source: IntelliJ Platform SDK docs — Settings Guide: The `ConfigurableProvider` Class (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

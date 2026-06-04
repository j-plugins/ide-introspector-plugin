---
id: sdk.run-configurations.architecture-overview.settingseditor
title: Run Configurations: SettingsEditor
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, settingseditor]
---
`SettingsEditor`

A run configuration may allow editing its general settings and settings specific to a [program runner](https://plugins.jetbrains.com/docs/intellij/execution.html#execution-classes).
If it is required, a `RunConfiguration` implementation should return a [SettingsEditor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/SettingsEditor.java) instance from:

* `getConfigurationEditor()` for editing run configuration settings

* `getRunnerSettingsEditor()` for editing settings for a specific program runner

A `SettingsEditor` implementation must provide the following methods:

* `getComponent()` - creates a UI component for displaying settings controls

* `applyEditorTo()` - copies the current editor UI state into the target settings object

* `resetEditorFrom()` - resets the current editor UI state to the initial settings state

In the case of run configuration settings, the settings object is `RunConfiguration` itself.
Settings specific to a program runner must implement [ConfigurationPerRunnerSettings](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/ConfigurationPerRunnerSettings.java).

If the settings editor requires validation, implement [CheckableRunConfigurationEditor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-api/src/com/intellij/execution/impl/CheckableRunConfigurationEditor.java).

If the settings editor is complex, see [Simplifying Settings Editors](#simplifying-settings-editors) for solutions.

Example: [DemoSettingsEditor](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/run_configuration/src/main/java/org/jetbrains/sdk/runConfiguration/DemoSettingsEditor.java) from the `run_configuration` code sample.


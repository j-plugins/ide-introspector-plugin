---
id: sdk.run-configurations.simplifying-settings-editors.settings-editor-groups
title: Run Configurations: Settings Editor Groups
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, settings, editor, groups]
---
A complex settings editor can be split into smaller editors focused on a specific area, e.g.:

* Configuration - containing the main configuration settings

* Logs - containing settings related to logging

* Coverage - containing settings related to code coverage

* etc.

These editors should be added to the [SettingsEditorGroup](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/SettingsEditorGroup.java) object, which is a `SettingsEditor`'s implementation itself and must be returned from `getConfigurationEditor()` or `getRunnerSettingsEditor()`.
Each editor added to the group is displayed in a separate tab.

Example: [ApplicationConfiguration.getConfigurationEditor()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/execution/impl/src/com/intellij/execution/application/ApplicationConfiguration.java)


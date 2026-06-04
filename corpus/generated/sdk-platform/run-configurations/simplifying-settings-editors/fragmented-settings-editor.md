---
id: sdk.run-configurations.simplifying-settings-editors.fragmented-settings-editor
title: Run Configurations: Fragmented Settings Editor
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, fragmented, settings, editor]
---
Fragmented Settings allow for the creation of a cleaner run configuration settings editor.
The fragmented editor is built of reusable fragments, which can be shared between different run configuration editors.

When a user creates a new run configuration from a template, only essential fragments are displayed at first.
More advanced options are hidden and must be explicitly enabled by the user from the Modify options dropdown.
It makes the editor smaller, freeing it from the clutter of unused settings fields.

To implement a fragmented settings editor in a run configuration, extend [RunConfigurationFragmentedEditor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/execution/ui/RunConfigurationFragmentedEditor.java) and implement `createRunFragments()`.
The method must return a list of [SettingsEditorFragment](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/execution/ui/SettingsEditorFragment.java) instances, which represent particular settings fragments that users can enable and configure.

Examples:

* [JavaApplicationSettingsEditor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/execution/impl/src/com/intellij/execution/application/JavaApplicationSettingsEditor.java)

* [MavenRunConfigurationSettingsEditor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/plugins/maven/src/main/java/org/jetbrains/idea/maven/execution/run/configuration/MavenRunConfigurationSettingsEditor.kt)


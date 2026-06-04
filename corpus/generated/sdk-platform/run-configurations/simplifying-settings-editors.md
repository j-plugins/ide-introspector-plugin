# Simplifying Settings Editors

If a run configuration settings editor is complex, consider implementing one of the following solutions to simplify the UI:

* [Fragmented Settings Editor](#fragmented-settings-editor) - the recommended approach since version 2021.1

* [Settings Editor Groups](#settings-editor-groups)

### Fragmented Settings Editor

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

### Settings Editor Groups

A complex settings editor can be split into smaller editors focused on a specific area, e.g.:

* Configuration - containing the main configuration settings

* Logs - containing settings related to logging

* Coverage - containing settings related to code coverage

* etc.

These editors should be added to the [SettingsEditorGroup](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/SettingsEditorGroup.java) object, which is a `SettingsEditor`'s implementation itself and must be returned from `getConfigurationEditor()` or `getRunnerSettingsEditor()`.
Each editor added to the group is displayed in a separate tab.

Example: [ApplicationConfiguration.getConfigurationEditor()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/execution/impl/src/com/intellij/execution/application/ApplicationConfiguration.java)

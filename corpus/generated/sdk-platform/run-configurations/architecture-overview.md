# Architecture Overview

The following diagram shows the key run configurations classes:

```PLANTUML
@startuml

skinparam DefaultFontName JetBrains Sans
skinparam DefaultFontSize 14
hide empty members
hide circle

interface RunProfile
interface ConfigurationType
abstract class ConfigurationFactory
interface RunConfiguration
abstract class SettingsEditor


ConfigurationType *-- "*" ConfigurationFactory
ConfigurationFactory --> RunConfiguration: creates
RunConfiguration o-- "0..*" SettingsEditor
RunConfiguration -l|> RunProfile

@enduml
```

Run Configuration API (except `SettingsEditor` class, which is a class shared by many IntelliJ Platform APIs) is a part of the [Execution API](https://plugins.jetbrains.com/docs/intellij/execution.html).

### ConfigurationType (sdk.run-configurations.architecture-overview.configurationtype)
### ConfigurationFactory

`ConfigurationFactory`

[ConfigurationFactory](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/ConfigurationFactory.java) classes are responsible for creating [run configuration](#runconfiguration) instances.
The only method required to be implemented is `createTemplateConfiguration()`, which is called once for each project to create the run configuration template.
The actual run configurations are created in the `createConfiguration()` method by cloning the template.

Configuration factory presentation is inherited from the containing configuration type.
If customization is needed, override the presentation methods in the factory class.

By default, configurations created by a given factory are not editable in [dumb mode](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#dumb-mode).
To enable editing them in Dumb Mode, return `true` from `isEditableInDumbMode()`.

### RunConfiguration

`RunConfiguration`

[RunConfiguration](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RunConfiguration.java) extends [RunProfile](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RunProfile.java) and represents a named profile that can be run by the [Execution API](https://plugins.jetbrains.com/docs/intellij/execution.html).

When implementing a run configuration class, consider using one of the standard base classes:

* [RunConfigurationBase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RunConfigurationBase.java) - a general-purpose base class that contains the most basic implementation of a run configuration.

* [LocatableConfigurationBase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/LocatableConfigurationBase.java) - a base class for [configurations that can be created from context](#creating-a-run-configuration-from-context).

* [ModuleBasedConfiguration](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/ModuleBasedConfiguration.java) - a base class for a configuration that is associated with a specific [Module](https://plugins.jetbrains.com/docs/intellij/module.html) (e.g., Java run configurations use the selected module to determine the run classpath).

### SettingsEditor

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

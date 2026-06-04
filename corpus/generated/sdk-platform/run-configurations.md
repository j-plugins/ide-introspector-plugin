# Run Configurations

<tldr>
Product Help: [Run/Debug Configuration](https://www.jetbrains.com/idea/help/run-debug-configuration.html)
</tldr>

A run configuration is a specific type of [run profile](https://plugins.jetbrains.com/docs/intellij/execution.html#configuration-classes).
Run configurations can be managed from the UI and are persisted between IDE restarts.
They allow users to specify execution options like a working directory, environment variables, program arguments, and other parameters required to run a process.
Run configurations can be started from the Run toolbar, the editor, and executed programmatically from actions or other components.

## Architecture Overview (sdk.run-configurations.architecture-overview)
## Persistence

Run configuration settings are persistent.
They are stored in the file system and loaded back after the IDE restart.
Persisting and loading settings are performed by `writeExternal()` and `readExternal()` methods of the `RunConfiguration` class correspondingly.

The actually stored configurations are represented by instances of the [RunnerAndConfigurationSettings](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/RunnerAndConfigurationSettings.java) class, which combines a run configuration with runner-specific settings and stores general run configuration flags and properties.

## Creating a Run Configuration Programmatically

If a plugin requires creating run configurations programmatically, .e.g, from a custom action, perform the following steps:

1. [RunManager.createConfiguration()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/RunManager.kt) - creates an instance of `RunnerAndConfigurationSettings`.

2. [RunManager.addConfiguration()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/RunManager.kt) - makes the created configuration persistent by adding it to either the list of shared configurations stored in a project or to the list of local configurations stored in the workspace file.

## Creating a Run Configuration from Context (sdk.run-configurations.creating-a-run-configuration-from-context)
## Running Configurations from the Gutter

If a run configuration is closely related to a PSI element (e.g., runnable method, test, etc.), it is possible to allow running configurations by [clicking the editor gutter icon](https://www.jetbrains.com/help/idea/running-applications.html#run-from-editor).
It is achieved by implementing [RunLineMarkerContributor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/execution/lineMarker/RunLineMarkerContributor.java), which provides information like the icon, tooltip content, and available actions for a given PSI element.

The standard method for providing the information is `getInfo()`.
If computing the information is slow, implement `getSlowInfo()`, which is used by the editor highlighting mechanism to gather information in batch and apply all the information at once to avoid icons blinking.
If access to indexes is not required, it can be marked [dumb aware](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#DumbAwareAPI).

To provide the standard executor actions like Run, Debug, etc., use [ExecutorAction.getActions()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/execution/lineMarker/ExecutorAction.kt).

## Starting a Run Configuration Programmatically

The easiest way to run an existing run configuration is using [ProgramRunnerUtil.executeConfiguration(RunnerAndConfigurationSettings, Executor)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/execution/ProgramRunnerUtil.java).
`RunnerAndConfigurationSettings` can be retrieved with, e.g., `RunManager.getConfigurationSettings(ConfigurationType)`.
The executor can be retrieved with a static method if a required executor exposes one or with [ExecutorRegistry.getExecutorById()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/ExecutorRegistry.java).

## Validating a Run Configuration

To check whether a run configuration is configured correctly and can be executed, implement the `RunConfiguration.checkConfiguration()`.
In case the run configuration settings are incomplete, the method should throw one of the following exceptions:

* [RuntimeConfigurationWarning](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RuntimeConfigurationWarning.java) - in case of a problem which doesn't affect execution

* [RuntimeConfigurationException](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RuntimeConfigurationException.java) - in case of non-fatal error, which allows executing the configuration

* [RuntimeConfigurationError](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RuntimeConfigurationError.java) - in case of a fatal error that makes it impossible to execute the run configuration

If the configuration contains any warnings or errors, its icon will be patched with the error indicator and the message will be displayed on the configuration settings page. In case of `RuntimeConfigurationError`, if a user tries to execute the configuration, the run configuration settings dialog will be presented so that the user can fix the issues before the execution.

All the mentioned exceptions allow providing a quick fix for the reported issue.
If the quick fix implementation is provided, the quick fix button will be displayed next to the error message.

## Simplifying Settings Editors (sdk.run-configurations.simplifying-settings-editors)
## Refactoring Support

Some run configurations contain references to classes, files, or directories in their settings, and these settings usually need to be updated when the corresponding element is renamed or moved.
To support that, a run configuration needs to implement the [RefactoringListenerProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RefactoringListenerProvider.java) interface.

The `RefactoringListenerProvider.getRefactoringElementListener()`'s implementation should check whether the refactored element is referred from the run configuration.
If it is, return a [RefactoringElementListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/refactoring/listeners/RefactoringElementListener.java) that updates the run configuration according to the new name and location of the element.

## Modifying Existing Run Configurations

Plugins can modify existing run configurations before they are run, e.g., by adding additional process parameters.
However, there is no single platform-wide extension point, and different IDEs provide different configuration base classes and extension points, allowing for their modifications.
To see what is possible in your case, check the [RunConfigurationExtensionBase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configuration/RunConfigurationExtensionBase.java) inheritors.
Examples:

* [RunConfigurationExtension](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/execution/impl/src/com/intellij/execution/RunConfigurationExtension.java) implementations registered in [com.intellij.runConfigurationExtension](https://jb.gg/ipe?extensions=com.intellij.runConfigurationExtension) extension point allow for modifying Java run configurations extending [RunConfigurationBase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RunConfigurationBase.java).

* [PythonRunConfigurationExtension](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/python/src/com/jetbrains/python/run/PythonRunConfigurationExtension.java) implementations registered in [Pythonid.runConfigurationExtension](https://jb.gg/ipe?extensions=Pythonid.runConfigurationExtension) extension point allow for modifying configuration extending [AbstractPythonRunConfiguration](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/python/src/com/jetbrains/python/run/AbstractPythonRunConfiguration.java) etc.

## Referencing Environment Variables in Run Configurations

Run configurations can define user environment variables specific to a given run configuration and include system environment variables.
Sometimes, it is convenient to reference existing variables in newly created variables, e.g., if a user creates an `EXTENDED_PATH` variable and builds it from a custom entry and the system `PATH` variable, they should reference it in the value by surrounding it with the `$` character: `/additional/entry:$PATH$`.

To substitute variable references with the actual references, it is required to call [EnvironmentUtil.inlineParentOccurrences()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/EnvironmentUtil.java) (available since 2023.2).

## Before Run Tasks

Sometimes, it is necessary to perform specific tasks before a configuration is actually run, e.g., build the project, run a build tool preparation task, launch a web browser, etc.
Plugins can provide custom tasks that can be added by users to a created run configuration.

To provide a custom task, implement [BeforeRunTaskProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/BeforeRunTaskProvider.java) and register it
in [com.intellij.stepsBeforeRunProvider](https://jb.gg/ipe?extensions=com.intellij.stepsBeforeRunProvider) extension point
.
The provider implementation is responsible for creating a task instance for a given run configuration and executing the task.

If access to indexes is not required, it can be marked [dumb aware](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#DumbAwareAPI).

## Macros (sdk.run-configurations.macros)

> Source: IntelliJ Platform SDK docs — Run Configurations (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

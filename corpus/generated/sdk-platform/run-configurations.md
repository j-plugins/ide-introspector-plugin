---
id: sdk.run-configurations
title: Run Configurations
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, run, configurations]
---
<tldr>
Product Help: [Run/Debug Configuration](https://www.jetbrains.com/idea/help/run-debug-configuration.html)
</tldr>

A run configuration is a specific type of [run profile](https://plugins.jetbrains.com/docs/intellij/execution.html#configuration-classes).
Run configurations can be managed from the UI and are persisted between IDE restarts.
They allow users to specify execution options like a working directory, environment variables, program arguments, and other parameters required to run a process.
Run configurations can be started from the Run toolbar, the editor, and executed programmatically from actions or other components.

## Architecture Overview (run-configurations/architecture-overview.md)
### ConfigurationType (run-configurations/architecture-overview/configurationtype.md)
### ConfigurationFactory (run-configurations/architecture-overview/configurationfactory.md)
### RunConfiguration (run-configurations/architecture-overview/runconfiguration.md)
### SettingsEditor (run-configurations/architecture-overview/settingseditor.md)
## Persistence (run-configurations/persistence.md)
## Creating a Run Configuration Programmatically (run-configurations/creating-a-run-configuration-programmatically.md)
## Creating a Run Configuration from Context (run-configurations/creating-a-run-configuration-from-context.md)
## Running Configurations from the Gutter (run-configurations/running-configurations-from-the-gutter.md)
## Starting a Run Configuration Programmatically (run-configurations/starting-a-run-configuration-programmatically.md)
## Validating a Run Configuration (run-configurations/validating-a-run-configuration.md)
## Simplifying Settings Editors (run-configurations/simplifying-settings-editors.md)
### Fragmented Settings Editor (run-configurations/simplifying-settings-editors/fragmented-settings-editor.md)
### Settings Editor Groups (run-configurations/simplifying-settings-editors/settings-editor-groups.md)
## Refactoring Support (run-configurations/refactoring-support.md)
## Modifying Existing Run Configurations (run-configurations/modifying-existing-run-configurations.md)
## Referencing Environment Variables in Run Configurations (run-configurations/referencing-environment-variables-in-run-configurations.md)
## Before Run Tasks (run-configurations/before-run-tasks.md)
## Macros (run-configurations/macros.md)
### Adding Macro Selector in Run Configuration Editor (run-configurations/macros/adding-macro-selector-in-run-configuration-editor.md)
### Expanding Macros Before Execution (run-configurations/macros/expanding-macros-before-execution.md)
### Providing Custom Macros (run-configurations/macros/providing-custom-macros.md)

> Source: IntelliJ Platform SDK docs — Run Configurations (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

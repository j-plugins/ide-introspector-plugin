---
id: sdk.run-configurations.validating-a-run-configuration
title: Run Configurations: Validating a Run Configuration
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, validating, run, configuration]
---
Part of `sdk.run-configurations`.

To check whether a run configuration is configured correctly and can be executed, implement the `RunConfiguration.checkConfiguration()`.
In case the run configuration settings are incomplete, the method should throw one of the following exceptions:

* [RuntimeConfigurationWarning](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RuntimeConfigurationWarning.java) - in case of a problem which doesn't affect execution

* [RuntimeConfigurationException](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RuntimeConfigurationException.java) - in case of non-fatal error, which allows executing the configuration

* [RuntimeConfigurationError](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RuntimeConfigurationError.java) - in case of a fatal error that makes it impossible to execute the run configuration

If the configuration contains any warnings or errors, its icon will be patched with the error indicator and the message will be displayed on the configuration settings page. In case of `RuntimeConfigurationError`, if a user tries to execute the configuration, the run configuration settings dialog will be presented so that the user can fix the issues before the execution.

All the mentioned exceptions allow providing a quick fix for the reported issue.
If the quick fix implementation is provided, the quick fix button will be displayed next to the error message.

> Source: IntelliJ Platform SDK docs — Run Configurations: Validating a Run Configuration (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

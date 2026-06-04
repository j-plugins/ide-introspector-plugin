---
id: sdk.run-configurations.starting-a-run-configuration-programmatically
title: Run Configurations: Starting a Run Configuration Programmatically
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, starting, run, configuration, programmatically]
---
Part of `sdk.run-configurations`.

The easiest way to run an existing run configuration is using [ProgramRunnerUtil.executeConfiguration(RunnerAndConfigurationSettings, Executor)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/execution/ProgramRunnerUtil.java).
`RunnerAndConfigurationSettings` can be retrieved with, e.g., `RunManager.getConfigurationSettings(ConfigurationType)`.
The executor can be retrieved with a static method if a required executor exposes one or with [ExecutorRegistry.getExecutorById()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/ExecutorRegistry.java).

> Source: IntelliJ Platform SDK docs — Run Configurations: Starting a Run Configuration Programmatically (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

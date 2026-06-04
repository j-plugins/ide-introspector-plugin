---
id: sdk.run-configurations.persistence
title: Run Configurations: Persistence
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, persistence]
---
Part of `sdk.run-configurations`.

Run configuration settings are persistent.
They are stored in the file system and loaded back after the IDE restart.
Persisting and loading settings are performed by `writeExternal()` and `readExternal()` methods of the `RunConfiguration` class correspondingly.

The actually stored configurations are represented by instances of the [RunnerAndConfigurationSettings](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/RunnerAndConfigurationSettings.java) class, which combines a run configuration with runner-specific settings and stores general run configuration flags and properties.

> Source: IntelliJ Platform SDK docs — Run Configurations: Persistence (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

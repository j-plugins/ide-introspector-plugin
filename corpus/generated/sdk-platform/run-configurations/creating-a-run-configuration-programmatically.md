---
id: sdk.run-configurations.creating-a-run-configuration-programmatically
title: Run Configurations: Creating a Run Configuration Programmatically
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, creating, run, configuration, programmatically]
---
If a plugin requires creating run configurations programmatically, .e.g, from a custom action, perform the following steps:

1. [RunManager.createConfiguration()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/RunManager.kt) - creates an instance of `RunnerAndConfigurationSettings`.

2. [RunManager.addConfiguration()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/RunManager.kt) - makes the created configuration persistent by adding it to either the list of shared configurations stored in a project or to the list of local configurations stored in the workspace file.


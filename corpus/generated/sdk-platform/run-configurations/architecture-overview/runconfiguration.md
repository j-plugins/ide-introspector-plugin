---
id: sdk.run-configurations.architecture-overview.runconfiguration
title: Run Configurations: RunConfiguration
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, runconfiguration]
---
`RunConfiguration`

[RunConfiguration](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RunConfiguration.java) extends [RunProfile](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RunProfile.java) and represents a named profile that can be run by the [Execution API](https://plugins.jetbrains.com/docs/intellij/execution.html).

When implementing a run configuration class, consider using one of the standard base classes:

* [RunConfigurationBase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RunConfigurationBase.java) - a general-purpose base class that contains the most basic implementation of a run configuration.

* [LocatableConfigurationBase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/LocatableConfigurationBase.java) - a base class for [configurations that can be created from context](#creating-a-run-configuration-from-context).

* [ModuleBasedConfiguration](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/ModuleBasedConfiguration.java) - a base class for a configuration that is associated with a specific [Module](https://plugins.jetbrains.com/docs/intellij/module.html) (e.g., Java run configurations use the selected module to determine the run classpath).


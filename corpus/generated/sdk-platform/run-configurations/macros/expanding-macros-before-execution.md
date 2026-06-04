---
id: sdk.run-configurations.macros.expanding-macros-before-execution
title: Run Configurations: Expanding Macros Before Execution
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, expanding, macros, before, execution]
---
Macros used in run configuration must be expanded to actual values before the process execution.
It is usually done in the `RunProfile.getState()` method called during the [execution workflow](https://plugins.jetbrains.com/docs/intellij/execution.html#execution-workflow).
To expand configured values, use one of [ProgramParametersConfigurator](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/execution/util/ProgramParametersConfigurator.java)'s methods: `expandMacros()`, `expandPathAndMacros()`, or `expandMacrosAndParseParameters()`.
See their Javadocs for the details.


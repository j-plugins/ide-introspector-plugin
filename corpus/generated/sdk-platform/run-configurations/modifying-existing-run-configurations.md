---
id: sdk.run-configurations.modifying-existing-run-configurations
title: Run Configurations: Modifying Existing Run Configurations
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, modifying, existing, run, configurations]
---
Part of `sdk.run-configurations`.

Plugins can modify existing run configurations before they are run, e.g., by adding additional process parameters.
However, there is no single platform-wide extension point, and different IDEs provide different configuration base classes and extension points, allowing for their modifications.
To see what is possible in your case, check the [RunConfigurationExtensionBase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configuration/RunConfigurationExtensionBase.java) inheritors.
Examples:

* [RunConfigurationExtension](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/execution/impl/src/com/intellij/execution/RunConfigurationExtension.java) implementations registered in [com.intellij.runConfigurationExtension](https://jb.gg/ipe?extensions=com.intellij.runConfigurationExtension) extension point allow for modifying Java run configurations extending [RunConfigurationBase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RunConfigurationBase.java).

* [PythonRunConfigurationExtension](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/python/src/com/jetbrains/python/run/PythonRunConfigurationExtension.java) implementations registered in [Pythonid.runConfigurationExtension](https://jb.gg/ipe?extensions=Pythonid.runConfigurationExtension) extension point allow for modifying configuration extending [AbstractPythonRunConfiguration](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/python/src/com/jetbrains/python/run/AbstractPythonRunConfiguration.java) etc.

> Source: IntelliJ Platform SDK docs — Run Configurations: Modifying Existing Run Configurations (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

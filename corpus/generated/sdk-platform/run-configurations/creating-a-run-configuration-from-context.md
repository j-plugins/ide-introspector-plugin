---
id: sdk.run-configurations.creating-a-run-configuration-from-context
title: Run Configurations: Creating a Run Configuration from Context
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, creating, run, configuration, from, context]
---
Part of `sdk.run-configurations`.

Run configurations can be created and run from context, e.g., by right-clicking an application main method, a test class/method, etc., directly in the editor or the project view.
This is achieved by implementing [LazyRunConfigurationProducer](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-api/src/com/intellij/execution/actions/LazyRunConfigurationProducer.kt) and registering
it in [com.intellij.runConfigurationProducer](https://jb.gg/ipe?extensions=com.intellij.runConfigurationProducer) extension point
.

The extension requires implementing the following methods:

* `getConfigurationFactory()` - returns the factory creating run configurations of the type specified in the extension class implementation.

* `setupConfigurationFromContext()` - receives a blank configuration of the specified type and a [ConfigurationContext](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-api/src/com/intellij/execution/actions/ConfigurationContext.java) containing information about a source code location (accessible by calling `getLocation()` or `getPsiLocation()`). The implementation needs to check whether the location is applicable to the configuration type (e.g., if it is in a file of the supported language). If it is, put the correct context-specific settings into the run configuration and return `true`. Return `false` otherwise.

* `isConfigurationFromContext()` - checks if a configuration was created from the specified context. This method allows reusing an existing run configuration, which applies to the current context, instead of creating a new one and possibly ignoring the user's customizations in the existing one.

If access to indexes is not required, it can be marked [dumb aware](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#DumbAwareAPI).
If the run configuration requires additional data before it is executed for the first time, override [RunConfigurationProducer.onFirstRun()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-api/src/com/intellij/execution/actions/RunConfigurationProducer.java) to provide it or display UI to get the data from the user.

To support the automatic naming of configurations created from context, the configuration should extend [LocatableConfigurationBase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/LocatableConfigurationBase.java).
It supports generating a name for a configuration from its settings and tracking whether the user changed the name.

> Source: IntelliJ Platform SDK docs — Run Configurations: Creating a Run Configuration from Context (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

---
id: sdk.run-configurations.architecture-overview.configurationfactory
title: Run Configurations: ConfigurationFactory
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, configurationfactory]
---
`ConfigurationFactory`

[ConfigurationFactory](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/ConfigurationFactory.java) classes are responsible for creating [run configuration](#runconfiguration) instances.
The only method required to be implemented is `createTemplateConfiguration()`, which is called once for each project to create the run configuration template.
The actual run configurations are created in the `createConfiguration()` method by cloning the template.

Configuration factory presentation is inherited from the containing configuration type.
If customization is needed, override the presentation methods in the factory class.

By default, configurations created by a given factory are not editable in [dumb mode](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#dumb-mode).
To enable editing them in Dumb Mode, return `true` from `isEditableInDumbMode()`.


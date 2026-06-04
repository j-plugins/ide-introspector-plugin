---
id: sdk.run-configurations.architecture-overview
title: Run Configurations: Architecture Overview
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, architecture, overview]
---
The following diagram shows the key run configurations classes:

```PLANTUML
@startuml

skinparam DefaultFontName JetBrains Sans
skinparam DefaultFontSize 14
hide empty members
hide circle

interface RunProfile
interface ConfigurationType
abstract class ConfigurationFactory
interface RunConfiguration
abstract class SettingsEditor


ConfigurationType *-- "*" ConfigurationFactory
ConfigurationFactory --> RunConfiguration: creates
RunConfiguration o-- "0..*" SettingsEditor
RunConfiguration -l|> RunProfile

@enduml
```

Run Configuration API (except `SettingsEditor` class, which is a class shared by many IntelliJ Platform APIs) is a part of the [Execution API](https://plugins.jetbrains.com/docs/intellij/execution.html).

### ConfigurationType (run-configurations/architecture-overview/configurationtype.md)
### ConfigurationFactory (run-configurations/architecture-overview/configurationfactory.md)
### RunConfiguration (run-configurations/architecture-overview/runconfiguration.md)
### SettingsEditor (run-configurations/architecture-overview/settingseditor.md)

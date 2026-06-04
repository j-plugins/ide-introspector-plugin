---
id: sdk.run-configurations.architecture-overview
title: Run Configurations: Architecture Overview
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, architecture, overview]
---
Part of `sdk.run-configurations`.

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

## Subtopics

- ConfigurationType — `sdk.run-configurations.architecture-overview.configurationtype`
- ConfigurationFactory — `sdk.run-configurations.architecture-overview.configurationfactory`
- RunConfiguration — `sdk.run-configurations.architecture-overview.runconfiguration`
- SettingsEditor — `sdk.run-configurations.architecture-overview.settingseditor`

> Source: IntelliJ Platform SDK docs — Run Configurations: Architecture Overview (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

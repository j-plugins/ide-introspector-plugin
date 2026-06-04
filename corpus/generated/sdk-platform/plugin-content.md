---
id: sdk.plugin-content
title: Plugin Content
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, plugin, content]
---
Plugin distribution is built using the dedicated Gradle `buildPlugin` task (Reference: [2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#buildPlugin),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-buildplugin)) or [Plugin DevKit](https://plugins.jetbrains.com/docs/intellij/deploying-theme.html).

The plugin distribution `.jar` file contains:

* configuration file (`META-INF/plugin.xml`) ([Plugin Configuration File](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html))

* classes implementing the plugin functionality

* recommended: plugin logo file(s) (`META-INF/pluginIcon*.svg`) ([Plugin Logo](https://plugins.jetbrains.com/docs/intellij/plugin-icon-file.html))

Tip:

See [Distribution Size](https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#distribution-size) for important steps to optimize the plugin distribution file.

Targeting a plugin distribution to a specific OS is not possible ([issue](https://youtrack.jetbrains.com/issue/MP-1896)).

## Plugin Without Dependencies

A plugin consisting of a single `.jar` file is placed in the `/plugins` directory.

```PLANTUML
@startuml

skinparam TitleFontName JetBrains Sans
skinparam TitleFontStyle plain
skinparam TitleFontSize 16
skinparam DefaultTextAlignment left

title
  <IDE directory>
  |_ plugins
    |_ sample.jar // (Plugin distribution) //
      |_ com
        |_ company
          |_ Sample.class // (Class ""com.company.Sample"") //
      |_ ...
      |_ META-INF
        |_ plugin.xml // (Plugin Configuration File) //
        |_ pluginIcon.svg // (Plugin Logo) //
        |_ pluginIcon_dark.svg // (Plugin Logo, dark variant) //
end title
@enduml
```

## Plugin With Dependencies

The plugin `.jar` file is placed in the `/lib` folder under the plugin's "root" folder, together with all required bundled libraries.

All JARs from the `/lib` folder are automatically added to the classpath (see also [Plugin Class Loaders](https://plugins.jetbrains.com/docs/intellij/plugin-class-loaders.html)).

Warning: Do Not Repackage Libraries

Do not repackage libraries into the main plugin JAR file.
Otherwise, [Plugin Verifier](https://plugins.jetbrains.com/docs/intellij/verifying-plugin-compatibility.html) will yield false positives for unresolved classes and methods.

```PLANTUML
@startuml

skinparam TitleFontName JetBrains Sans
skinparam TitleFontStyle plain
skinparam TitleFontSize 16
skinparam DefaultTextAlignment left

title
  <IDE directory>
  |_ plugins
    |_ sample
      |_ lib
        |_ lib_foo.jar // (Required bundled library #1) //
        |_ lib_bar.jar // (Required bundled library #2) //
        |_ ...
        |_ sample.jar // (Plugin distribution) //
          |_ com
            |_ company
              |_ Sample.class // (Class ""com.company.Sample"") //
          |_ ...
          |_ META-INF
            |_ plugin.xml // (Plugin Configuration File) //
            |_ pluginIcon.svg // (Plugin Logo) //
            |_ pluginIcon_dark.svg // (Plugin Logo, dark variant) //
end title
@enduml
```

> Source: IntelliJ Platform SDK docs — Plugin Content (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

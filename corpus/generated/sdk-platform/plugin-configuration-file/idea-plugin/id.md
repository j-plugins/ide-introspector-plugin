---
id: sdk.plugin-configuration-file.idea-plugin.id
title: Plugin Configuration File: id
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform]
---
`id`

A unique identifier of the plugin.
It should be a fully qualified name similar to Java packages and must not collide with the ID of existing plugins.
The ID is a technical value used to identify the plugin in the IDE and [JetBrains Marketplace](https://plugins.jetbrains.com).
Please use characters, numbers, and `'.'`/`'-'`/`'_'` symbols only and keep it reasonably short.

Warning:

Make sure to pick a stable ID, as the value cannot be changed later after public release.

Required
: no; ignored in an [additional config file](#additional-plugin-configuration-files)

It is highly recommended to set in `plugin.xml` file.

The element can be skipped in the source `plugin.xml` file if the Gradle plugin `patchPluginXml` task
([2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml))
is enabled and configured.

Default value
: Value of the [&lt;name&gt;](#idea-plugin__name) element.

Example
: ```XML
<id>com.example.framework</id>
```


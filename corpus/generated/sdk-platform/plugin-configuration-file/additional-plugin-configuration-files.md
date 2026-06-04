---
id: sdk.plugin-configuration-file.additional-plugin-configuration-files
title: Plugin Configuration File: Additional Plugin Configuration Files
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, additional, plugin, configuration, files]
---
A plugin can contain additional configuration files beside the main `plugin.xml`.
They have the same format, and they are included with the `config-file` attribute of [&lt;depends&gt;](#idea-plugin__depends) elements specifying [plugin dependencies](https://plugins.jetbrains.com/docs/intellij/plugin-dependencies.html).
However, some elements and attributes required in `plugin.xml` are ignored in additional configuration files.
If the requirements differ, the documentation below will state it explicitly.
One use case for additional configuration files is when a plugin provides optional features that are only available in some IDEs and require [certain modules](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#modules-specific-to-functionality).


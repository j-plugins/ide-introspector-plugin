---
id: sdk.plugin-configuration-file.idea-plugin.version
title: Plugin Configuration File: version
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, version]
---
`version`

<tldr>
Reference: [JetBrains Marketplace: Semantic Versioning](https://plugins.jetbrains.com/docs/marketplace/semver.html)
</tldr>

The plugin version displayed in the Plugins settings dialog and on the
[JetBrains Marketplace](https://plugins.jetbrains.com) plugin page.
Plugins uploaded to the JetBrains Marketplace must follow semantic versioning.

Required
: yes; ignored in an [additional config file](#additional-plugin-configuration-files)

The element can be skipped in the source `plugin.xml` file if the Gradle plugin `patchPluginXml` task
([2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml))
is enabled and configured.

Example
: ```XML
<version>1.3.18</version>
```


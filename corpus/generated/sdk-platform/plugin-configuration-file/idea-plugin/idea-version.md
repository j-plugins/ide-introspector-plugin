---
id: sdk.plugin-configuration-file.idea-plugin.idea-version
title: Plugin Configuration File: idea-version
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, idea, version]
---
`idea-version`



<tldr>
Reference: [Build Number Ranges](https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html)
</tldr>





The plugin's range of compatible IntelliJ-based IDE versions.



Required
: yes; ignored in an [additional config file](#additional-plugin-configuration-files)

The element can be skipped in the source `plugin.xml` file if the Gradle plugin `patchPluginXml` task
([2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml))
is enabled and configured.


Attributes
: * `since-build` (required) The lowest IDE version compatible with the plugin.

  * `until-build` (optional) The highest version of the IDE the plugin is compatible with. It's highly recommended not to set this attribute, so the plugin will be compatible with all IDE versions since the version specified by the `since-build`. If it becomes necessary to specify the highest compatible IDE version later, it'll be possible to do that via JetBrains Marketplace. Only if the publishing process for the plugin is configured to upload a new version for each major IDE version, it makes sense to limit the highest compatible IDE version from the beginning. In that case, use `strict-until-build` instead.

  * `strict-until-build` (optional; available since 2025.3) The highest version of the IDE the plugin is compatible with. Use this attribute only if the publishing process for the plugin is configured to upload a new version for each major IDE version. Otherwise, skip this attribute. If it becomes necessary to specify the highest compatible IDE version later, it'll be possible to do that via JetBrains Marketplace.


Examples
: * Compatibility with a specific build number (2021.3.3) and higher versions: ```XML <idea-version since-build="213.7172.25"/> ```

  * Compatibility with versions from any of `213` branches to any of `221` branches: ```XML <idea-version since-build="213" until-build="221.*"/> ```


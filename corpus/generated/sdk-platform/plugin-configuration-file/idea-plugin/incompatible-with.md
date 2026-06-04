---
id: sdk.plugin-configuration-file.idea-plugin.incompatible-with
title: Plugin Configuration File: incompatible-with
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, incompatible, with]
---
`incompatible-with`



<tldr>
Reference: [Declaring Incompatibility with Plugin](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#declaring-incompatibility-with-module)
</tldr>






The [ID](#idea-plugin__id) or alias of the plugin the current plugin is incompatible with.
The plugin is not loaded if the incompatible plugin is installed in the current IDE.



Required
: no; ignored in an [additional config file](#additional-plugin-configuration-files)


Examples
: * Incompatibility with the Java plugin: ```XML <incompatible-with> com.intellij.java </incompatible-with> ```

  * Incompatibility with the AppCode plugin referenced via its alias: ```XML <incompatible-with> com.intellij.modules.appcode.ide </incompatible-with> ```


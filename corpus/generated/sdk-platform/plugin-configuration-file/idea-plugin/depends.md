---
id: sdk.plugin-configuration-file.idea-plugin.depends
title: Plugin Configuration File: depends
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, depends]
---
Part of `sdk.plugin-configuration-file.idea-plugin`.

`depends`



<tldr>
Reference: [Plugin Dependencies](https://plugins.jetbrains.com/docs/intellij/plugin-dependencies.html)
, [Modules Specific to Functionality](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#modules-specific-to-functionality)
</tldr>





Specifies a dependency on another plugin or a module of an IntelliJ Platform-based product.
A single [&lt;idea-plugin&gt;](#idea-plugin) element can contain multiple `<depends>` elements.



Required
: no; in most cases dependency on the
[platform](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#modules-available-in-all-products)
module is needed


Attributes
: * `optional` (optional) Boolean value defining whether the dependency is optional to load the plugin in the IDE. If the dependency plugin is not installed in the current IDE, and `optional` is: * `true` - the plugin will be loaded * `false` (default) - the plugin will not be loaded

  * `config-file` (required when `optional` is `true`) Relative path to an [additional configuration file](#additional-plugin-configuration-files), loaded only if the dependency plugin is installed in the current IDE.


Examples
: * Required plugin dependency: ```XML <depends>com.example.dependencypluginid</depends> ```

  * Required dependency on the IntelliJ IDEA Java Module: ```XML <depends>com.intellij.modules.java</depends> ```

  * Required module dependency with additional configuration: ```XML <depends config-file="myPluginId-withJava.xml"> com.intellij.modules.java </depends> ```

  * Optional module dependency with additional configuration: ```XML <depends optional="true" config-file="myPluginId-withKotlin.xml"> org.jetbrains.kotlin </depends> ```

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: depends (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

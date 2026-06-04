---
id: sdk.code-inspections.creating-an-inspection.plugin-configuration-file
title: Code Inspections: Plugin Configuration File
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, plugin, configuration, file]
---
The `comparing_string_references_inspection` is described as a [com.intellij.localInspection](https://jb.gg/ipe?extensions=com.intellij.localInspection) extension point
in the `comparing_string_references_inspection` plugin configuration ([plugin.xml](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/comparing_string_references_inspection/src/main/resources/META-INF/plugin.xml)) file.

There exist two types of inspection extensions:

* The [com.intellij.localInspection](https://jb.gg/ipe?extensions=com.intellij.localInspection) extension point is used for inspections that operate on one file at a time, and also operate "on-the-fly" as the user edits the file.

* The [com.intellij.globalInspection](https://jb.gg/ipe?extensions=com.intellij.globalInspection) extension point is used for inspections that operate across multiple files, and the associated fix might, for example, refactor code between files.

The minimum inspection setup must declare the `implementationClass` and `language` attribute (unless the inspection works on any supported language).
As shown in the `comparing_string_references_inspection` plugin configuration file, other attributes can be defined in the `localInspection` element, either with or without localization.
In most cases, it is simplest to define the attributes in the plugin configuration file because the underlying parent classes handle most of the class responsibilities based on the configuration file description.

If required, inspections can define all the attribute information (except `implementationClass`) by overriding methods in the inspection implementation class (not recommended in general).


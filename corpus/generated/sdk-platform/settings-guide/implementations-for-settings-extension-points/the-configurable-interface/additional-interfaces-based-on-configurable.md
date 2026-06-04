---
id: sdk.settings-guide.implementations-for-settings-extension-points.the-configurable-interface.additional-interfaces-based-on-configurable
title: Settings Guide: Additional Interfaces Based on `Configurable
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, additional, interfaces, based, configurable]
---
Additional Interfaces Based on `Configurable`

There are classes in the IntelliJ Platform specialized in particular types of Settings.
These subtypes are based on `com.intellij.openapi.options.ConfigurableEP`.
For example, `Settings | Editor | General | Appearance` allows adding Settings via [EditorSmartKeysConfigurableEP](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-impl/src/com/intellij/application/options/editor/EditorSmartKeysConfigurableEP.java)
registered in [com.intellij.editorSmartKeysConfigurable](https://jb.gg/ipe?extensions=com.intellij.editorSmartKeysConfigurable) extension point
.


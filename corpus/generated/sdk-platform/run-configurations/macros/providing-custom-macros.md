---
id: sdk.run-configurations.macros.providing-custom-macros
title: Run Configurations: Providing Custom Macros
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, providing, custom, macros]
---
If the predefined list of macros is not enough, a plugin can provide custom macros by extending [Macro](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/macro/src/com/intellij/ide/macro/Macro.java) and
registering it in the [com.intellij.macro](https://jb.gg/ipe?extensions=com.intellij.macro) extension point
.


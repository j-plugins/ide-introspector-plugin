---
id: sdk.run-configurations.macros.adding-macro-selector-in-run-configuration-editor
title: Run Configurations: Adding Macro Selector in Run Configuration Editor
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, adding, macro, selector, run, configuration]
---
Macro selecting support can be added to a text field on the run configuration editor by installing it with [MacrosDialog.addMacroSupport()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/ide/macro/MacrosDialog.java) or other similar methods from this class.
After installation, a text field will be extended by a button invoking the macro dialog, which lists available macros with descriptions and previews.
After selecting and accepting a macro from the list, the macro placeholder is inserted into the text field at the current caret position.


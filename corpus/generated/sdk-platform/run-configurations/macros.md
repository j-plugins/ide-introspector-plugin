---
id: sdk.run-configurations.macros
title: Run Configurations: Macros
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, macros]
---
Part of `sdk.run-configurations`.

Macros are dynamic variables, which can be referenced in run configurations and expanded to actual values when a run configuration is executed.

For example, a macro with a name `ProjectFileDir` can be referenced as `$ProjectFileDir$` in a run configuration command line argument.
It is expanded to the absolute path of the current project directory when the run configuration is executed by a user.

A list of built-in macros is available in the [IntelliJ IDEA Web Help](https://www.jetbrains.com/help/idea/built-in-macros.html) and other products' documentation pages.

Tip:

Note that Macro API is not specific to execution or run configuration API and can be used in other places.

### Adding Macro Selector in Run Configuration Editor

Macro selecting support can be added to a text field on the run configuration editor by installing it with [MacrosDialog.addMacroSupport()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/ide/macro/MacrosDialog.java) or other similar methods from this class.
After installation, a text field will be extended by a button invoking the macro dialog, which lists available macros with descriptions and previews.
After selecting and accepting a macro from the list, the macro placeholder is inserted into the text field at the current caret position.

### Expanding Macros Before Execution

Macros used in run configuration must be expanded to actual values before the process execution.
It is usually done in the `RunProfile.getState()` method called during the [execution workflow](https://plugins.jetbrains.com/docs/intellij/execution.html#execution-workflow).
To expand configured values, use one of [ProgramParametersConfigurator](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/execution/util/ProgramParametersConfigurator.java)'s methods: `expandMacros()`, `expandPathAndMacros()`, or `expandMacrosAndParseParameters()`.
See their Javadocs for the details.

### Providing Custom Macros

If the predefined list of macros is not enough, a plugin can provide custom macros by extending [Macro](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/macro/src/com/intellij/ide/macro/Macro.java) and
registering it in the [com.intellij.macro](https://jb.gg/ipe?extensions=com.intellij.macro) extension point
.

> Source: IntelliJ Platform SDK docs — Run Configurations: Macros (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

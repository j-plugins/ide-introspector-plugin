---
id: sdk.run-configurations.macros
title: Run Configurations: Macros
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, macros]
---
Macros are dynamic variables, which can be referenced in run configurations and expanded to actual values when a run configuration is executed.

For example, a macro with a name `ProjectFileDir` can be referenced as `$ProjectFileDir$` in a run configuration command line argument.
It is expanded to the absolute path of the current project directory when the run configuration is executed by a user.

A list of built-in macros is available in the [IntelliJ IDEA Web Help](https://www.jetbrains.com/help/idea/built-in-macros.html) and other products' documentation pages.

Tip:

Note that Macro API is not specific to execution or run configuration API and can be used in other places.

### Adding Macro Selector in Run Configuration Editor (run-configurations/macros/adding-macro-selector-in-run-configuration-editor.md)
### Expanding Macros Before Execution (run-configurations/macros/expanding-macros-before-execution.md)
### Providing Custom Macros (run-configurations/macros/providing-custom-macros.md)

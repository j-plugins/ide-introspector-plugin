---
id: sdk.extension-points.using-extension-points.error-handling
title: Extension Points: Error Handling
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, error, handling]
---
When processing extension implementations or registrations, there might be errors, compatibility and configuration issues.
Use [PluginException](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/diagnostic/PluginException.java) to log and correctly attribute the causing plugin for
[builtin error reporting](https://plugins.jetbrains.com/docs/intellij/ide-infrastructure.html#error-reporting).

To report use of a deprecated API, use `PluginException.reportDeprecatedUsage()` methods.

Examples:

* [CompositeFoldingBuilder.assertSameFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/lang/folding/CompositeFoldingBuilder.java)

* [InspectionProfileEntry.getDisplayName()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/InspectionProfileEntry.java)


---
id: sdk.tool-windows.declarative-setup.conditional-display
title: Tool Windows: Conditional Display
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, conditional, display]
---
If the tool window of a plugin should not be displayed for all projects, the plugin can provide a corresponding condition.

2023.3+:

Implement suspending `ToolWindowFactory.isApplicableAsync(Project)` in Kotlin.

Earlier versions:

Implement `ToolWindowFactory.isApplicable(Project)`.

Tip:

The condition is evaluated only once when the project is loaded.

To show and hide a tool window dynamically while the user is working with the project, use [programmatic setup](#programmatic-setup) for tool window registration.


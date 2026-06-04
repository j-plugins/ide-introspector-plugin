---
id: sdk.run-configurations.running-configurations-from-the-gutter
title: Run Configurations: Running Configurations from the Gutter
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, running, configurations, from, gutter]
---
If a run configuration is closely related to a PSI element (e.g., runnable method, test, etc.), it is possible to allow running configurations by [clicking the editor gutter icon](https://www.jetbrains.com/help/idea/running-applications.html#run-from-editor).
It is achieved by implementing [RunLineMarkerContributor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/execution/lineMarker/RunLineMarkerContributor.java), which provides information like the icon, tooltip content, and available actions for a given PSI element.

The standard method for providing the information is `getInfo()`.
If computing the information is slow, implement `getSlowInfo()`, which is used by the editor highlighting mechanism to gather information in batch and apply all the information at once to avoid icons blinking.
If access to indexes is not required, it can be marked [dumb aware](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#DumbAwareAPI).

To provide the standard executor actions like Run, Debug, etc., use [ExecutorAction.getActions()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution-impl/src/com/intellij/execution/lineMarker/ExecutorAction.kt).


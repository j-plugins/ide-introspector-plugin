---
id: sdk.run-configurations.before-run-tasks
title: Run Configurations: Before Run Tasks
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, before, run, tasks]
---
Sometimes, it is necessary to perform specific tasks before a configuration is actually run, e.g., build the project, run a build tool preparation task, launch a web browser, etc.
Plugins can provide custom tasks that can be added by users to a created run configuration.

To provide a custom task, implement [BeforeRunTaskProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/BeforeRunTaskProvider.java) and register it
in [com.intellij.stepsBeforeRunProvider](https://jb.gg/ipe?extensions=com.intellij.stepsBeforeRunProvider) extension point
.
The provider implementation is responsible for creating a task instance for a given run configuration and executing the task.

If access to indexes is not required, it can be marked [dumb aware](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#DumbAwareAPI).


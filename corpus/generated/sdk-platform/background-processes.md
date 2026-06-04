---
id: sdk.background-processes
title: Background Processes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, background, processes]
---
Background process is a time-consuming computation usually executed on a background thread.
The IntelliJ Platform executes background processes widely and provides two main ways to run them by plugins:

* [Progress API](#progress-api) that allows for cancelling tasks and tracking their progress

* [Application.executeOnPooledThread()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java) methods for running background tasks that don't need progress tracking

## Progress API (background-processes/progress-api.md)
### Starting (background-processes/progress-api/starting.md)
### Cancellation (background-processes/progress-api/cancellation.md)
#### Requesting Cancellation (background-processes/progress-api/cancellation/requesting-cancellation.md)
#### Handling Cancellation (background-processes/progress-api/cancellation/handling-cancellation.md)
### Tracking Progress (background-processes/progress-api/tracking-progress.md)
## Pre-2025.1: `ProcessCanceledException` and Debugging (background-processes/pre-2025-1-processcanceledexception-and-debugging.md)

> Source: IntelliJ Platform SDK docs — Background Processes (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

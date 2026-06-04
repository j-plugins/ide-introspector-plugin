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

## Subtopics

- Progress API — `sdk.background-processes.progress-api`
- Pre-2025.1: `ProcessCanceledException` and Debugging — `sdk.background-processes.pre-2025-1-processcanceledexception-and-debugging`

> Source: IntelliJ Platform SDK docs — Background Processes (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

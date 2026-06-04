---
id: sdk.threading-model.accessing-data
title: Threading Model: Accessing Data
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, accessing, data]
---
Part of `sdk.threading-model`.

The IntelliJ Platform provides a simple API for accessing data under read or write locks in the form of read and write actions.

Read and write actions allow executing a piece of code under a lock, automatically acquiring it before an action starts, and releasing it after the action is finished.

Warning: Minimize Locking Scopes

Always try to wrap only the required operations into read/write actions, minimizing the time of holding locks.
If the read operation itself is long, consider using [non-blocking read actions](#non-blocking-read-actions) to avoid blocking the write lock and EDT.

## Subtopics

- Read Actions — `sdk.threading-model.accessing-data.read-actions`
- Write Actions — `sdk.threading-model.accessing-data.write-actions`

> Source: IntelliJ Platform SDK docs — Threading Model: Accessing Data (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

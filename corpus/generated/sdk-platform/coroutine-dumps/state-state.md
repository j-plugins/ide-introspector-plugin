---
id: sdk.coroutine-dumps.state-state
title: Coroutine Dumps: state: STATE
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, state]
---
Part of `sdk.coroutine-dumps`.

`state: STATE`

A coroutine's state.

Possible states:

* `CREATED` - a coroutine was created but not yet started.

* `SUSPENDED` - a coroutine was executed up until the last frame in the stacktrace. This is where it was last seen running.

* `RUNNING` - a coroutine is currently executed by a thread. Its stacktrace reflects what the coroutine is doing right now (probably blocked waiting for something, otherwise a `RUNNING` coroutine is rarely seen unless it’s doing CPU-intensive work).

> Source: IntelliJ Platform SDK docs — Coroutine Dumps: state: STATE (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

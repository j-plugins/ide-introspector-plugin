---
id: sdk.disposer-and-disposable.the-disposer-singleton.ending-a-disposable-lifecycle
title: Disposer and Disposable: Ending a Disposable Lifecycle
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, ending, disposable, lifecycle]
---
Part of `sdk.disposer-and-disposable.the-disposer-singleton`.

A plugin can manually end a `Disposable` lifecycle by calling `Disposer.dispose(Disposable)`.
This method handles recursively disposing of all the `Disposable` child descendants as well.

> Source: IntelliJ Platform SDK docs — Disposer and Disposable: Ending a Disposable Lifecycle (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

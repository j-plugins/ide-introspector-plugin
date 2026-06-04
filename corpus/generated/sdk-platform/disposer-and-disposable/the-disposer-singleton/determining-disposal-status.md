---
id: sdk.disposer-and-disposable.the-disposer-singleton.determining-disposal-status
title: Disposer and Disposable: Determining Disposal Status
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, determining, disposal, status]
---
Part of `sdk.disposer-and-disposable.the-disposer-singleton`.

You can use `Disposer.isDisposed()` to check whether a `Disposable` has already been disposed.
This check is useful, for example, for an asynchronous callback to a `Disposable` that may be disposed before the callback is executed.
In such a case, the best strategy is usually to do nothing and return early.

Warning:

Non-disposed objects shouldn't hold onto references to disposed objects, as this constitutes a memory leak.
Once a `Disposable` is released, it should be completely inactive, and there's no reason to refer to it anymore.

> Source: IntelliJ Platform SDK docs — Disposer and Disposable: Determining Disposal Status (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

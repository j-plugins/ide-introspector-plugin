---
id: sdk.disposer-and-disposable
title: Disposer and Disposable
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, disposer, disposable]
---
The IntelliJ Platform's [Disposer](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/util/Disposer.java) facilitates resource cleanup.
If a subsystem keeps a set of resources alive coincidently with a parent object's lifetime, the subsystem's resources should be registered with the `Disposer` to be released before or at the same time as the parent object.

Listeners are the most common resource type managed by `Disposer`, but there are other possible types:

* File handles, and database connections,

* Caches and other significant data structures.

The `Disposer` is a singleton that manages a tree of [Disposable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/Disposable.java) instances.
A `Disposable` is an interface for any object providing a `Disposable.dispose()` method to release heavyweight resources after a specific lifetime.

The `Disposer` supports chaining `Disposable` objects in parent-child relationships.

## Subtopics

- Automatically Disposed Objects — `sdk.disposer-and-disposable.automatically-disposed-objects`
- The `Disposer` Singleton — `sdk.disposer-and-disposable.the-disposer-singleton`
- Implementing the `Disposable` Interface — `sdk.disposer-and-disposable.implementing-the-disposable-interface`
- Diagnosing `Disposer` Leaks — `sdk.disposer-and-disposable.diagnosing-disposer-leaks`

> Source: IntelliJ Platform SDK docs — Disposer and Disposable (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

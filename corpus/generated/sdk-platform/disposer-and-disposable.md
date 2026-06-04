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

## Automatically Disposed Objects (disposer-and-disposable/automatically-disposed-objects.md)
## The `Disposer` Singleton (disposer-and-disposable/the-disposer-singleton.md)
### Choosing a Disposable Parent (disposer-and-disposable/the-disposer-singleton/choosing-a-disposable-parent.md)
### Registering Listeners with Parent Disposable (disposer-and-disposable/the-disposer-singleton/registering-listeners-with-parent-disposable.md)
### Determining Disposal Status (disposer-and-disposable/the-disposer-singleton/determining-disposal-status.md)
### Ending a Disposable Lifecycle (disposer-and-disposable/the-disposer-singleton/ending-a-disposable-lifecycle.md)
## Implementing the `Disposable` Interface (disposer-and-disposable/implementing-the-disposable-interface.md)
## Diagnosing `Disposer` Leaks (disposer-and-disposable/diagnosing-disposer-leaks.md)

> Source: IntelliJ Platform SDK docs — Disposer and Disposable (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

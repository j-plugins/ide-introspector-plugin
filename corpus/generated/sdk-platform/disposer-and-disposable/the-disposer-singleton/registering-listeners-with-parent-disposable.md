---
id: sdk.disposer-and-disposable.the-disposer-singleton.registering-listeners-with-parent-disposable
title: Disposer and Disposable: Registering Listeners with Parent Disposable
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, registering, listeners, with, parent, disposable]
---
Part of `sdk.disposer-and-disposable.the-disposer-singleton`.

Many IntelliJ Platform APIs for registering listeners either require passing a parent disposable or have overloads that take a parent disposable, for example:

```JAVA
public interface PomModel {
  // ...
  void addModelListener(PomModelListener listener);
  void addModelListener(PomModelListener listener, Disposable parentDisposable);
  void removeModelListener(PomModelListener listener);
}
```

Methods with a `parentDisposable` parameter automatically unsubscribe the listener when the corresponding parent disposable is disposed.
Using such methods is always preferable to removing listeners explicitly from the `dispose` method because it requires less code and is easier to verify for correctness.

The same rules apply to [message bus](https://plugins.jetbrains.com/docs/intellij/messaging-infrastructure.html) connections.

Always pass a parent disposable to `MessageBus.connect(parentDisposable)`, and make sure it has the shortest possible lifetime.
To choose the correct parent disposable, use the guidelines from the [previous section](#choosing-a-disposable-parent).

> Source: IntelliJ Platform SDK docs — Disposer and Disposable: Registering Listeners with Parent Disposable (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

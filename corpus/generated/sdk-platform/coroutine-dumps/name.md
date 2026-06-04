---
id: sdk.coroutine-dumps.name
title: Coroutine Dumps: name
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, name]
---
Part of `sdk.coroutine-dumps`.

`name`

A coroutine name.

Notable names:

* `ApplicationImpl@xxxxxxxx container` - application coroutine.

* `ProjectImpl@xxxxxxxx container` - project coroutine.

* `com.intellij.*.AClass` - a coroutine bound to some specific class instance, e.g., an extension or a service. Unnamed coroutines are hard to identify, so it is recommended to add `CoroutineName(someName)` into a coroutine context.

* `(a x b)` - an intersection of coroutines `a` and `b`, e.g., `(ApplicationImpl@56422718 x com.example.myplugin)` is an intersection of the application and a plugin scope. See also [Intersection Scopes](https://plugins.jetbrains.com/docs/intellij/coroutine-scopes.html#intersection-scopes).

> Source: IntelliJ Platform SDK docs — Coroutine Dumps: name (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

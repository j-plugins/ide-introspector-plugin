---
id: sdk.coroutine-dumps
title: Coroutine Dumps
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, coroutine, dumps]
---
Tip: Kotlin Coroutines×IntelliJ Platform

This section focuses on explaining coroutines in the specific context of the [IntelliJ Platform](https://plugins.jetbrains.com/docs/intellij/intellij-platform.html).
If you are not experienced with Kotlin Coroutines, it is highly recommended to get familiar with
[Learning Resources](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html#learning-resources) first.

The `Help | Diagnostic Tools | Dump Threads` action creates a thread dump, which is useful when investigating freezes or deadlocks.
Thread dumps include all application threads and coroutines existing at the moment of dump creation.

## Coroutine Dump Format

A coroutine dump format is:

```
- parent coroutine header
	at stackframe
	at stackframe
	...
	- child coroutine 0 header
		at stackframe
		at stackframe
		...
		- grandchild coroutine header
			at stackframe
			at stackframe
			...
	- child coroutine 1 header
	- child coroutine 2 header
		at stackframe
		at stackframe
		...
```

Each coroutine entry starts with a `-` character.
Indentation represents parent-child relationships.
A coroutine entry may not include a stacktrace (see `child coroutine 1 header`) because it has no executable body, or it did not start executing yet.

An example coroutine header:

```
-[x5 of] "my task":StandaloneCoroutine{Active}, state: SUSPENDED [ComponentManager(ApplicationImpl@xxxxxxxx), Dispatchers.EDT]
```

Its format is as follows:

```
-[xN of] "name":CoroutineClass{JobState}, state: STATE [context]
```

## Subtopics

- [xN of] — `sdk.coroutine-dumps.xn-of`
- name — `sdk.coroutine-dumps.name`
- CoroutineClass{JobState} — `sdk.coroutine-dumps.coroutineclass-jobstate`
- state: STATE — `sdk.coroutine-dumps.state-state`
- [context] — `sdk.coroutine-dumps.context`

> Source: IntelliJ Platform SDK docs — Coroutine Dumps (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

---
id: sdk.coroutine-dumps.coroutine-dump-format
title: Coroutine Dumps: Coroutine Dump Format
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, coroutine, dump, format]
---
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

### [xN of] (coroutine-dumps/coroutine-dump-format/xn-of.md)
### name (coroutine-dumps/coroutine-dump-format/name.md)
### CoroutineClass{JobState} (coroutine-dumps/coroutine-dump-format/coroutineclass-jobstate.md)
### state: STATE (coroutine-dumps/coroutine-dump-format/state-state.md)
### [context] (coroutine-dumps/coroutine-dump-format/context.md)

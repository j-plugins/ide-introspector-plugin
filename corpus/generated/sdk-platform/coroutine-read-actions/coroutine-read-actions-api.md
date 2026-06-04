---
id: sdk.coroutine-read-actions.coroutine-read-actions-api
title: Coroutine Read Actions: Coroutine Read Actions API
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, coroutine, read, actions, api]
---
Running RA from coroutines is executed with `*ReadAction*` functions from
[coroutines.kt](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/coroutines.kt)
(see their KDocs for the details).
Functions can be divided into two groups, which differ in reacting to an incoming write action (WA):

| Write Allowing Read Action (WARA) |Write Blocking Read Action (WBRA) |
------------------------------------------------------------------------
| `readAction` |`readActionBlocking` |
| `smartReadAction` |`smartReadActionBlocking` |
| `constrainedReadAction` |`constrainedReadActionBlocking` |

WARA is canceled when a parent coroutine is canceled or a WA arrives.

WBRA is canceled only when a parent coroutine is canceled.
It blocks WA until finishing its lambda.

Warning: Naming Convention

It is important to note that in the coroutines context, default functions
(without the `Blocking` suffix) behavior prioritizes WA.

In contrast, in the non-coroutine context,
[Application.runReadAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java)
and similar methods (without any prefix/suffix) perform RA blocking WA, whereas RA allowing WA are invoked via
the [NonBlockingReadAction API](https://plugins.jetbrains.com/docs/intellij/threading-model.html#non-blocking-read-actions).

Be careful when migrating the code running read actions to coroutines.

### Write Allowing Read Action vs. NonBlockingReadAction (coroutine-read-actions/coroutine-read-actions-api/write-allowing-read-action-vs-nonblockingreadaction.md)
#### Read Action Cancellability (coroutine-read-actions/coroutine-read-actions-api/write-allowing-read-action-vs-nonblockingreadaction/read-action-cancellability.md)

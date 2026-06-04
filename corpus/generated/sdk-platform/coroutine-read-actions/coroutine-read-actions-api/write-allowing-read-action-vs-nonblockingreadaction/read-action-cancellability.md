---
id: sdk.coroutine-read-actions.coroutine-read-actions-api.write-allowing-read-action-vs-nonblockingreadaction.read-action-cancellability
title: Coroutine Read Actions: Read Action Cancellability
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, read, action, cancellability]
---
Suspending read actions use coroutines as the underlying framework.

WARA (invoked with [mentioned *ReadAction functions](#coroutine-read-actions-api))
may make several attempts to execute its lambda.
The block needs to know whether the current attempt was canceled.
`*ReadAction` functions create a child
[Job](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-job/)
for each attempt, and this job becomes canceled when a write action arrives.
`*ReadAction` restarts the block if it was canceled by a write action, or throws
`CancellationException` if the calling coroutine was canceled, causing the cancellation
of the child `Job`.

To check whether the current action was canceled, clients must call [ProgressManager.checkCanceled()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/ProgressManager.java), which was adjusted to work in coroutines.
Clients mustn't throw [ProcessCanceledException](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/base/multiplatform/src/com/intellij/openapi/progress/ProcessCanceledException.kt) manually.


---
id: sdk.threading-model.avoiding-ui-freezes.event-listeners
title: Threading Model: Event Listeners
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, event, listeners]
---
Listeners mustn't perform any heavy operations.
Ideally, they should only clear some caches.

It is also possible to schedule background processing of events.
In such cases, be prepared that some new events might be delivered before the background processing starts – and thus the world might have changed by that moment or even in the middle of background processing.
Consider using [MergingUpdateQueue](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/util/ui/update/MergingUpdateQueue.kt) and [NBRA](#non-blocking-read-actions-api) to mitigate these issues.


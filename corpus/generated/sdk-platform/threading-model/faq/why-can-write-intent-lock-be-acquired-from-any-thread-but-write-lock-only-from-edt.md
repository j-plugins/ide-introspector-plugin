---
id: sdk.threading-model.faq.why-can-write-intent-lock-be-acquired-from-any-thread-but-write-lock-only-from-edt
title: Threading Model: Why can write intent lock be acquired from any thread but write lock only from EDT?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, why, can, write, intent, lock]
---
In the current platform state, technically, write intent lock can be acquired on any thread (it is done only on EDT in practice), but write lock can be acquired only on EDT.

Write intent lock was introduced as a "replacement" for EDT in the context of acquiring write lock.
Instead of allowing to acquire write lock on EDT only, it was planned to make it possible to acquire it from under write intent lock on any thread.
Write intent lock provides read access that was also available on EDT.
This behavior wasn't enabled in production, and the planned locking mechanism has changed.
It is planned to allow for acquiring write lock from any thread, even without a write intent lock.
Write intent lock will be still available and will allow performing read sessions finished with data writing.

Note: Something missing?

If a topic is not covered in the above sections,
let us know via the Feedback widget displayed on the right,
or [other channels](https://plugins.jetbrains.com/docs/intellij/getting-help.html#problems-with-the-guide).

Be specific about the topics and reasons for adding them and leave your email in case we need
more details. Thanks for your feedback!


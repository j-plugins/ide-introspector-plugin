---
id: sdk.coroutine-dumps.coroutine-dump-format.context
title: Coroutine Dumps: [context]
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, context]
---
`[context]`

A coroutine context.
Context elements are separated with `,`.

Notable context elements:

* `no parent and no name` comes from the startup tracer. It should not be present in application/project coroutines and their children.

* `ComponentManager(ApplicationImpl@xxxxxxxx)` - application or project, which serves as the coroutine parent.

* `BlockingEventLoop`, `Dispatchers.Default`, `Dispatchers.IO`, `LimitedDispatcher`, `Dispatchers.EDT` - a [coroutine dispatcher](https://plugins.jetbrains.com/docs/intellij/coroutine-dispatchers.html). Absence means [Dispatchers.Default](https://plugins.jetbrains.com/docs/intellij/coroutine-dispatchers.html#default-dispatcher).

* `ModalityState.xxx` - modality state. Absence means [ModalityState.nonModal()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/ModalityState.java). When EDT is in a modal state, all non-modal coroutines are suspended until the modal state ends, and EDT goes back to non-modal state as well. Some `RUNNING` coroutines might block in an `invokeAndWait` call, which means that `invokeAndWait` used non-modal default modality state for one of two reasons: * the coroutine contains the correct modality state in its context, but `invokeAndWait` is not aware of it * a modal coroutine awaits another unrelated coroutine, which in turn requires non-modal EDT to complete. Same problems can be found in regular thread dumps and blocking code, but coroutines suspend instead of blocking a thread, so it’s only possible to observe the last seen frame, which is usually enough.

Note: Something missing?

If a topic is not covered in the above sections,
let us know via the Feedback widget displayed on the right,
or [other channels](https://plugins.jetbrains.com/docs/intellij/getting-help.html#problems-with-the-guide).

Be specific about the topics and reasons for adding them and leave your email in case we need
more details. Thanks for your feedback!


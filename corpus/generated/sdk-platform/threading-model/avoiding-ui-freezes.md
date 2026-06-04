# Avoiding UI Freezes

### Don't Perform Long Operations on EDT

In particular, don't traverse [VFS](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html), parse [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html), resolve [references,](https://plugins.jetbrains.com/docs/intellij/psi-references.html) or query [indexes](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html).

There are still some cases when the platform itself invokes such expensive code (for example, resolve in `AnAction.update()`), but these are being worked on.
Meanwhile, try to speed up what you can in your plugin as it will be generally beneficial and will also improve background highlighting performance.

#### Action Update

For implementations of [AnAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnAction.java), plugin authors should specifically
review the documentation of `AnAction.getActionUpdateThread()` in the [Action System](https://plugins.jetbrains.com/docs/intellij/action-system.html) section as it describes how threading works for actions.

#### Minimize Write Actions Scope

Write actions currently [have to happen on EDT](#locks-and-edt).
To speed them up, as much as possible should be moved out of the write action into a preparation step which can be then invoked in the [background](https://plugins.jetbrains.com/docs/intellij/background-processes.html) or inside an [NBRA](#non-blocking-read-actions-api).

#### Slow Operations on EDT Assertion

Some of the long operations are reported by [SlowOperations.assertSlowOperationsAreAllowed()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/util/SlowOperations.java).
According to its Javadoc, they must be moved to BGT.
This can be achieved with the techniques mentioned in the Javadoc, [background processes](https://plugins.jetbrains.com/docs/intellij/background-processes.html), [Application.executeOnPooledThread()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java), or [coroutines](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html) (recommended for plugins targeting 2024.1+).
Note that the assertion is enabled in IDE EAP versions, [internal mode](https://plugins.jetbrains.com/docs/intellij/enabling-internal.html), or [development instance](https://plugins.jetbrains.com/docs/intellij/ide-development-instance.html), and regular users don't see them in the IDE.
This will change in the future, so fixing these exceptions is required.

### Event Listeners

Listeners mustn't perform any heavy operations.
Ideally, they should only clear some caches.

It is also possible to schedule background processing of events.
In such cases, be prepared that some new events might be delivered before the background processing starts – and thus the world might have changed by that moment or even in the middle of background processing.
Consider using [MergingUpdateQueue](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/util/ui/update/MergingUpdateQueue.kt) and [NBRA](#non-blocking-read-actions-api) to mitigate these issues.

### VFS Events

Massive batches of VFS events can be pre-processed in the background with [AsyncFileListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/AsyncFileListener.java).

### Investigating UI Freezes

See the [Investigating IntelliJ Platform UI Freezes](https://blog.jetbrains.com/platform/2025/09/investigating-intellij-platform-ui-freezes/) blog post for techniques to investigate UI freezes.

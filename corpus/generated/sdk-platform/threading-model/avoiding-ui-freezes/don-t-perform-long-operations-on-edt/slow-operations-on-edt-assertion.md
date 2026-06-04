---
id: sdk.threading-model.avoiding-ui-freezes.don-t-perform-long-operations-on-edt.slow-operations-on-edt-assertion
title: Threading Model: Slow Operations on EDT Assertion
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, slow, operations, edt, assertion]
---
Some of the long operations are reported by [SlowOperations.assertSlowOperationsAreAllowed()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/util/SlowOperations.java).
According to its Javadoc, they must be moved to BGT.
This can be achieved with the techniques mentioned in the Javadoc, [background processes](https://plugins.jetbrains.com/docs/intellij/background-processes.html), [Application.executeOnPooledThread()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java), or [coroutines](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html) (recommended for plugins targeting 2024.1+).
Note that the assertion is enabled in IDE EAP versions, [internal mode](https://plugins.jetbrains.com/docs/intellij/enabling-internal.html), or [development instance](https://plugins.jetbrains.com/docs/intellij/ide-development-instance.html), and regular users don't see them in the IDE.
This will change in the future, so fixing these exceptions is required.


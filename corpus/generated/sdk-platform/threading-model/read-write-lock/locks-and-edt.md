---
id: sdk.threading-model.read-write-lock.locks-and-edt
title: Threading Model: Locks and EDT
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, locks, edt]
---
Although acquiring all types of locks can be, in theory, done from any threads, the platform implicitly acquires write intent lock and allows acquiring the write lock only on EDT.
It means that writing data can be done only on EDT.

Tip:

It is known that writing data only on EDT has negative consequences of potentially freezing the UI.
There is an in-progress effort to [allow writing data from any thread](https://youtrack.jetbrains.com/issue/IJPL-53).
See the [historical reason](#why-write-actions-are-currently-allowed-only-on-edt) for this behavior in the current platform versions.

The scope of implicitly acquiring the write intent lock on EDT differs depending on the platform version:

2023.3+:

Write intent lock is acquired automatically when operation is invoked on EDT with [Application.invokeLater()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java).

Earlier versions:

Write intent lock is acquired automatically when operation is invoked on EDT with methods such as:

* [Application.invokeLater()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java),

* [SwingUtilities.invokeLater()](https://docs.oracle.com/javase/8/docs/api/javax/swing/SwingUtilities.html#invokeLater-java.lang.Runnable-),

* [UIUtil.invokeAndWaitIfNeeded()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/ui/src/com/intellij/util/ui/UIUtil.java),

* [EdtInvocationManager.invokeLaterIfNeeded()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/ui/EdtInvocationManager.java),

* and other similar methods

It is recommended to use `Application.invokeLater()` if the operation is supposed to write data.
Use other methods for pure UI operations.


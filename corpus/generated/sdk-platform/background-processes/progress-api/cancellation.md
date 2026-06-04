---
id: sdk.background-processes.progress-api.cancellation
title: Background Processes: Cancellation
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, cancellation]
---
Part of `sdk.background-processes.progress-api`.

The most important feature of Progress API is the ability to cancel a process if the result of the computation gets irrelevant.
Cancellation can be performed either by a user (pressing a cancel button) or from code when the current operation becomes obsolete due to some changes in the project.
Examples:

* Cancelling the search for symbol usages (cancellation by user): 1. The user triggers the Find Usages action in a large project. 2. Results are being calculated and gradually presented to the user. 3. The user sees the place they were interested in or realizes that they don't need these results anymore. 4. The user clicks the cancel button in the status bar, and the operation is canceled.

* Code completion (cancellation from code): 1. The user types a letter in the editor. 2. Computation of results for code completion is started. 3. User types another letter. 4. The computation started in 2. is now outdated and is canceled to start computation for the new input.

Being prepared for cancellation requests in plugin code is crucial for saving CPU resources and responsiveness of the IDE.

#### Requesting Cancellation

The process can be marked as canceled by calling `ProgressIndicator.cancel()`.
This method is called by the infrastructure that started the process, for example, when the mentioned cancel button is clicked, or by code responsible for invoking code completion.

The `cancel()` method marks the process as canceled, and it is up to the running operation to actually cancel itself.
See the section below for handling cancellation.

#### Handling Cancellation

The cancellation is handled in the running process code by calling `ProgressIndicator.checkCanceled()`, or `ProgressManager.checkCanceled()`, if no indicator instance is available in the current context.

If the process was [marked as canceled](#requesting-cancellation), then the call to `checkCanceled()` throws an instance of a special unchecked [ProcessCanceledException](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/base/multiplatform/src/com/intellij/openapi/progress/ProcessCanceledException.kt) (PCE) and the actual cancellation happens.
This exception doesn't represent any error and is only used to handle cancellation for convenience.
It allows canceling processes deeply in the call stack, without the need to handle cancellation on each level.

PCE is handled by the infrastructure that started the process and must never be logged or swallowed.
In case of catching it for some reason, it must be rethrown.
Use inspection
Plugin DevKit | Code | Cancellation exception handled incorrectly (2024.3)
(previously named 'ProcessCanceledException' handled incorrectly (2023.3)).

All code working with [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html) or in other kinds of background processes must be prepared for PCE being thrown at any point.

The `checkCanceled()` should be called by the running operation often enough to guarantee the process's smooth cancellation.
PSI internals have a lot of `checkCanceled()` calls inside.
If a process does lengthy non-PSI activity, insert explicit `checkCanceled()` calls so that it happens frequently, for example, on each Nth loop iteration.
Use inspection Plugin DevKit | Code | Cancellation check in loops (2023.1).

Note: Disabling ProcessCanceledException

Throwing PCE from `checkCanceled()` can be disabled in the [internal mode](https://plugins.jetbrains.com/docs/intellij/enabling-internal.html) for development (for example, while debugging the code) by invoking:

* `Tools | Internal Actions | Skip Window Deactivation Events` (2023.2+)

* `Tools | Internal Actions | Disable ProcessCanceledException` (pre-2023.2)

> Source: IntelliJ Platform SDK docs — Background Processes: Cancellation (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

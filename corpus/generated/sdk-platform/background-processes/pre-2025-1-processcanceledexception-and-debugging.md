---
id: sdk.background-processes.pre-2025-1-processcanceledexception-and-debugging
title: Background Processes: Pre-2025.1: `ProcessCanceledException` and Debugging
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, pre, 2025, processcanceledexception, debugging]
---
Pre-2025.1: `ProcessCanceledException` and Debugging

Sometimes, a PCE is thrown from `checkCanceled()` in the code inspected by a plugin developer during a debugging session.
If the developer tries to step over a line and this line throws PCE (potentially from a deep call frame), the next place where the debugger stops is a catch/finally block intercepting the exception.
This greatly breaks the developer's workflow as the analysis must be started over.

Tip: 2025.1+

With the Plugin DevKit plugin installed, the debugger will prevent PCE from being thrown during stepping and evaluation with no additional actions needed.

This situation can be avoided by enabling an action available in the [internal mode](https://plugins.jetbrains.com/docs/intellij/enabling-internal.html):

2023.2+:

`Tools | Internal Actions | Skip Window Deactivation Events`

Action disabling window deactivation events.
This helps avoid PCEs thrown as a result of deactivating the IDE development instance window.
For example, when the IDE window is deactivated, it closes the completion popup, which, in turn, cancels the completion process.

Earlier Versions:

`Tools | Internal Actions | Disable ProcessCanceledException`

Action disabling throwing `ProcessCanceledException`.

Note: Something missing?

If a topic is not covered in the above sections,
let us know via the Feedback widget displayed on the right,
or [other channels](https://plugins.jetbrains.com/docs/intellij/getting-help.html#problems-with-the-guide).

Be specific about the topics and reasons for adding them and leave your email in case we need
more details. Thanks for your feedback!


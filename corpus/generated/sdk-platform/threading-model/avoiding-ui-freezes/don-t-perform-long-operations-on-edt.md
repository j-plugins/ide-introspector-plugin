---
id: sdk.threading-model.avoiding-ui-freezes.don-t-perform-long-operations-on-edt
title: Threading Model: Don't Perform Long Operations on EDT
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, don, perform, long, operations, edt]
---
In particular, don't traverse [VFS](https://plugins.jetbrains.com/docs/intellij/virtual-file-system.html), parse [PSI](https://plugins.jetbrains.com/docs/intellij/psi.html), resolve [references,](https://plugins.jetbrains.com/docs/intellij/psi-references.html) or query [indexes](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html).

There are still some cases when the platform itself invokes such expensive code (for example, resolve in `AnAction.update()`), but these are being worked on.
Meanwhile, try to speed up what you can in your plugin as it will be generally beneficial and will also improve background highlighting performance.

#### Action Update (threading-model/avoiding-ui-freezes/don-t-perform-long-operations-on-edt/action-update.md)
#### Minimize Write Actions Scope (threading-model/avoiding-ui-freezes/don-t-perform-long-operations-on-edt/minimize-write-actions-scope.md)
#### Slow Operations on EDT Assertion (threading-model/avoiding-ui-freezes/don-t-perform-long-operations-on-edt/slow-operations-on-edt-assertion.md)

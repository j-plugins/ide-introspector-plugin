---
id: sdk.coroutine-scopes.using-a-correct-scope.use-service-scopes
title: Coroutine Scopes: Use Service Scopes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, use, service, scopes]
---
If a plugin requires running some code in a coroutine, the approach recommended in most cases is to create a separate [service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html) that will receive its [own scope](#service-scopes) via constructor and launch the coroutine in this scope.
This approach guarantees the usage of the correct scope, preventing leaks and canceling wrong scopes and killing all their (e.g., application's or project's) coroutines accidentally.

Note:

Note that since 2024.2, `AnAction.actionPerformed()` logic can be executed in the [current thread coroutine scope](https://plugins.jetbrains.com/docs/intellij/launching-coroutines.html#using-currentthreadcoroutinescope).

See the [Launching Coroutines](https://plugins.jetbrains.com/docs/intellij/launching-coroutines.html) section for details.

Warning:

The following sections describe the potential problems that would occur if the wrong coroutine scopes were used.
This allows better understanding of the platform scopes and why the [service approach](#use-service-scopes) mentioned above must be used.


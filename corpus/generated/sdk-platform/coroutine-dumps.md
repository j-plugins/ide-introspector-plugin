# Coroutine Dumps

Tip: Kotlin Coroutines×IntelliJ Platform

This section focuses on explaining coroutines in the specific context of the [IntelliJ Platform](https://plugins.jetbrains.com/docs/intellij/intellij-platform.html).
If you are not experienced with Kotlin Coroutines, it is highly recommended to get familiar with
[Learning Resources](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html#learning-resources) first.

The `Help | Diagnostic Tools | Dump Threads` action creates a thread dump, which is useful when investigating freezes or deadlocks.
Thread dumps include all application threads and coroutines existing at the moment of dump creation.

## Coroutine Dump Format (sdk.coroutine-dumps.coroutine-dump-format)

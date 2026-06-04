# Write Actions

#### API

* [WriteAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/WriteAction.java) `run()` or `compute()`: Kotlin: ```KOTLIN WriteAction.run<Throwable> { // write data } ``` Warning: Plugins implemented in Kotlin and targeting versions 2024.1+ should use suspending [writeAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/coroutines.kt). Java: ```JAVA WriteAction.run(() -> { // write data }); ```

##### Alternative APIs

* [Application.runWriteAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java): Kotlin: ```KOTLIN ApplicationManager.application.runWriteAction { // write data } ``` Java: ```JAVA ApplicationManager.getApplication().runWriteAction(() -> { // write data }); ``` Note that this API is considered low-level and should be avoided.

* Kotlin [runWriteAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/actions.kt): ```KOTLIN runWriteAction { // write data } ``` Note that this API is obsolete since 2024.1.

#### Rules

2023.3+:

Writing data is only allowed on EDT invoked with `Application.invokeLater()`.

Write operations must always be wrapped in a write action with one of the [API](#write-actions-api) methods.

Modifying the model is only allowed from write-safe contexts (see [Invoking Operations on EDT and Modality](#invoking-operations-on-edt-and-modality)).

Earlier versions:

Writing data is only allowed on EDT.

Write operations must always be wrapped in a write action with one of the [API](#write-actions-api) methods.

Modifying the model is only allowed from write-safe contexts, including user actions and `SwingUtilities.invokeLater()` calls from them (see [Invoking Operations on EDT and Modality](#invoking-operations-on-edt-and-modality)).

It is forbidden to modify PSI, VFS, or project model from inside UI renderers or `SwingUtilities.invokeLater()`.

Tip:

[Thread Access Info](https://plugins.jetbrains.com/plugin/16815-thread-access-info) plugin visualizes Read/Write Access and Thread information in the debugger.

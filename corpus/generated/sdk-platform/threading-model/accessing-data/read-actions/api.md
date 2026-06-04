---
id: sdk.threading-model.accessing-data.read-actions.api
title: Threading Model: API
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, api]
---
* [ReadAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/ReadAction.java) `run()` or `compute()`: Kotlin: ```KOTLIN val psiFile = ReadAction.compute<PsiFile, Throwable> { // read and return PsiFile } ``` Warning: Plugins implemented in Kotlin and targeting versions 2024.1+ should use suspending [readAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/coroutines.kt). See also [Coroutine Read Actions](https://plugins.jetbrains.com/docs/intellij/coroutine-read-actions.html). Java: ```JAVA PsiFile psiFile = ReadAction.compute(() -> { // read and return PsiFile }); ```

##### Alternative APIs (threading-model/accessing-data/read-actions/api/alternative-apis.md)

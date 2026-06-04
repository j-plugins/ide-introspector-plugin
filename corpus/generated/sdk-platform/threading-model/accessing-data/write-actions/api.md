---
id: sdk.threading-model.accessing-data.write-actions.api
title: Threading Model: API
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, api]
---
* [WriteAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/WriteAction.java) `run()` or `compute()`: Kotlin: ```KOTLIN WriteAction.run<Throwable> { // write data } ``` Warning: Plugins implemented in Kotlin and targeting versions 2024.1+ should use suspending [writeAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/coroutines.kt). Java: ```JAVA WriteAction.run(() -> { // write data }); ```

##### Alternative APIs (threading-model/accessing-data/write-actions/api/alternative-apis.md)

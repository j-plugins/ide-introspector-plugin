---
id: sdk.threading-model.accessing-data.write-actions.api.alternative-apis
title: Threading Model: Alternative APIs
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, alternative, apis]
---
* [Application.runWriteAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java): Kotlin: ```KOTLIN ApplicationManager.application.runWriteAction { // write data } ``` Java: ```JAVA ApplicationManager.getApplication().runWriteAction(() -> { // write data }); ``` Note that this API is considered low-level and should be avoided.

* Kotlin [runWriteAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/actions.kt): ```KOTLIN runWriteAction { // write data } ``` Note that this API is obsolete since 2024.1.


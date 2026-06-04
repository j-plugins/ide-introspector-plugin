---
id: sdk.threading-model.accessing-data.read-actions.api.alternative-apis
title: Threading Model: Alternative APIs
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, alternative, apis]
---
* [Application.runReadAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java): Kotlin: ```KOTLIN val psiFile = ApplicationManager.application.runReadAction { // read and return PsiFile } ``` Java: ```JAVA PsiFile psiFile = ApplicationManager.getApplication() .runReadAction((Computable<PsiFile>)() -> { // read and return PsiFile }); ``` Note that this API is considered low-level and should be avoided.

* Kotlin [runReadAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/actions.kt): ```KOTLIN val psiFile = runReadAction { // read and return PsiFile } ``` Note that this API is obsolete since 2024.1.


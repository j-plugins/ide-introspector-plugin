---
id: sdk.coroutine-scopes.intellij-platform-scopes.main-scopes
title: Coroutine Scopes: Main Scopes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, main, scopes]
---
* Root - the root scope spans all the coroutines. This is the standard root scope launched with [runBlocking](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/run-blocking.html) coroutine builder.

* Application - a scope associated with the [Application](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java) container (component manager) lifetime. It is canceled on application shutdown. This triggers cancellation of the Application×Plugin scope and, subsequently, its children, including the Project×Plugin scope.

* Project - a scope associated with a [Project](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/Project.java) container (component manager) lifetime. It is canceled when a project is being closed. This triggers the cancellation of the Project×Plugin scope and, subsequently, its children.

* Plugin - a scope associated with a plugin lifetime. It is canceled on unloading of the corresponding plugin. This triggers cancellation of the Application×Plugin scope and, subsequently, its children, including the Project×Plugin scope.


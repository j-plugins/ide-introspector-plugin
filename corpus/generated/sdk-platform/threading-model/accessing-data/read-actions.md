---
id: sdk.threading-model.accessing-data.read-actions
title: Threading Model: Read Actions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, read, actions]
---
Part of `sdk.threading-model.accessing-data`.

#### API

* [ReadAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/ReadAction.java) `run()` or `compute()`: Kotlin: ```KOTLIN val psiFile = ReadAction.compute<PsiFile, Throwable> { // read and return PsiFile } ``` Warning: Plugins implemented in Kotlin and targeting versions 2024.1+ should use suspending [readAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/coroutines.kt). See also [Coroutine Read Actions](https://plugins.jetbrains.com/docs/intellij/coroutine-read-actions.html). Java: ```JAVA PsiFile psiFile = ReadAction.compute(() -> { // read and return PsiFile }); ```

##### Alternative APIs

* [Application.runReadAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/Application.java): Kotlin: ```KOTLIN val psiFile = ApplicationManager.application.runReadAction { // read and return PsiFile } ``` Java: ```JAVA PsiFile psiFile = ApplicationManager.getApplication() .runReadAction((Computable<PsiFile>)() -> { // read and return PsiFile }); ``` Note that this API is considered low-level and should be avoided.

* Kotlin [runReadAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/actions.kt): ```KOTLIN val psiFile = runReadAction { // read and return PsiFile } ``` Note that this API is obsolete since 2024.1.

#### Rules

2023.3+:

Reading data is allowed from any thread.

Reading data on EDT invoked with `Application.invokeLater()` doesn't require an explicit read action, as the write intent lock allowing to read data is [acquired implicitly](#locks-and-edt).

Earlier versions:

Reading data is allowed from any thread.

Reading data on EDT doesn't require an explicit read action, as the write intent lock allowing to read data is [acquired implicitly](#locks-and-edt).

In all other cases, it is required to wrap a read operation in a read action with one of the [API](#read-actions-api) methods.

##### Objects Validity

The read objects aren't guaranteed to survive between several consecutive read actions.
Whenever starting a read action, check if the PSI/VFS/project/module is still valid.
Example:

Kotlin:

```KOTLIN
val virtualFile = runReadAction { // read action 1
  // read a virtual file
}
// do other time-consuming work...
val psiFile = runReadAction { // read action 2
  if (virtualFile.isValid()) { // check if the virtual file is valid
    PsiManager.getInstance(project).findFile(virtualFile)
  } else null
}
```

Java:

```JAVA
VirtualFile virtualFile = ReadAction.compute(() -> { // read action 1
  // read a virtual file
});
// do other time-consuming work...
PsiFile psiFile = ReadAction.compute(() -> { // read action 2
  if (virtualFile.isValid()) { // check if the virtual file is valid
    return PsiManager.getInstance(project).findFile(virtualFile);
  }
  return null;
});
```

Between executing first and second read actions, another thread could invalidate the virtual file:

```MERMAID
---
config:
  gantt:
    numberSectionStyles: 2
displayMode: compact
---
gantt
    dateFormat X
    %% do not remove trailing space in axisFormat:
    axisFormat ‎
    section Thread 1
        read action 1       : 0, 1
        time-consuming work : done, 1, 4
        read action 2       : 4, 5
    section Thread 2
        delete virtual file : crit, 2, 3
```

> Source: IntelliJ Platform SDK docs — Threading Model: Read Actions (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

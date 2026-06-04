# Launching Coroutines

Tip: Kotlin Coroutines×IntelliJ Platform

This section focuses on explaining coroutines in the specific context of the [IntelliJ Platform](https://plugins.jetbrains.com/docs/intellij/intellij-platform.html).
If you are not experienced with Kotlin Coroutines, it is highly recommended to get familiar with
[Learning Resources](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html#learning-resources) first.

In the IntelliJ Platform, coroutines can be launched with one of the following approaches:

1. [Service with its own scope](#launching-coroutine-from-service-scope).

2. The [currentThreadCoroutineScope](#using-currentthreadcoroutinescope) function for [executing actions](https://plugins.jetbrains.com/docs/intellij/action-system.html#overriding-the-anactionactionperformed-method).

3. The [runBlockingCancellable](#using-runblockingcancellable) function. (not recommended)

## Launching Coroutine From Service Scope

The recommended approach is creating a [service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html) that receives [its scope](https://plugins.jetbrains.com/docs/intellij/coroutine-scopes.html#service-scopes) via the constructor injection and launching a coroutine from the service methods.
Note that while creating a service instance does allocate additional resources, using a dedicated service and scope remains a lightweight and fundamentally safe solution for launching coroutines.
It should be used whenever possible.

The pattern is as follows:

Application Service:

```KOTLIN
@Service
class MyApplicationService(
  private val cs: CoroutineScope
) {
  fun scheduleSomething() {
    cs.launch {
      // do something
    }
  }
}
```

Project Service:

```KOTLIN
@Service(Service.Level.PROJECT)
class MyProjectService(
  private val project: Project,
  private val cs: CoroutineScope
) {
  fun scheduleSomething() {
    cs.launch {
      // do something
    }
  }
}
```

The injected scope is created per service, so each instance has its own isolated scope with a common parent, which is an [intersection scope](https://plugins.jetbrains.com/docs/intellij/coroutine-scopes.html#intersection-scopes).
The injected scope is canceled when the container (application/project) is shut down or when the plugin is unloaded.

## 

Using `currentThreadCoroutineScope`

Action behavior performed in [AnAction.actionPerformed()](https://plugins.jetbrains.com/docs/intellij/action-system.html#overriding-the-anactionactionperformed-method) can be executed in a coroutine via [currentThreadCoroutineScope](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/coroutines.kt):

```KOTLIN
internal class MyAction : AnAction() {
  override fun actionPerformed(e: AnActionEvent) {
    val file = e.getData(LangDataKeys.PSI_FILE) ?: return
    currentThreadCoroutineScope().launch {
      // use suspending APIs:
      val targets = readAction {
        // do something in read
      }
      withContext(Dispatchers.EDT) {
        // show some UI
      }
    }
  }
  // ...
}
```

Compared to the [service scope](#launching-coroutine-from-service-scope) approach, using `currentThreadCoroutineScope()` enables Action System infrastructure to control the launched coroutine and cancel it if needed.
In the case of service scopes, the infrastructure code can't control a coroutine launched from an action, as service scopes are "more global" and live longer than the action trigger.

## 

Using `runBlockingCancellable`

Warning:

Using `runBlockingCancellable` is not recommended.
Use [service scopes](#launching-coroutine-from-service-scope) whenever possible.

In a standard coroutine-based application, the bridge between the regular blocking code and the suspending code is the [runBlocking](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/run-blocking.html) function.

In the IntelliJ Platform, a similar purpose is achieved by the [runBlockingCancellable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/progress/coroutines.kt) function.
In addition to the same semantics as `runBlocking`, the action gets canceled when the current progress indicator or the current job is canceled.

Note: Something missing?

If a topic is not covered in the above sections,
let us know via the Feedback widget displayed on the right,
or [other channels](https://plugins.jetbrains.com/docs/intellij/getting-help.html#problems-with-the-guide).

Be specific about the topics and reasons for adding them and leave your email in case we need
more details. Thanks for your feedback!

> Source: IntelliJ Platform SDK docs — Launching Coroutines (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

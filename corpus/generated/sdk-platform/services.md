---
id: sdk.services
title: Services
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, services]
---
A service is a plugin component loaded on demand when your plugin calls the `getService()` method of corresponding [ComponentManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/extensions/src/com/intellij/openapi/components/ComponentManager.java) instance (see [Types](#types)).
The IntelliJ Platform ensures that only one instance of a service is loaded even though it is called several times.
Services are used to encapsulate logic operating on a set of related classes or to provide some reusable functionality that can be used across the plugin project.
Conceptually, they don't differ from the service classes in other languages or frameworks.

A service must have an implementation class used for service instantiation.
A service may also have an interface class used to obtain the service instance and provide the service's API.

A service needing a shutdown hook/cleanup routine can implement [Disposable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/Disposable.java) and perform necessary work in `dispose()` (see [Automatically Disposed Objects](https://plugins.jetbrains.com/docs/intellij/disposers.html#automatically-disposed-objects)).

Note: Services as API

If declared services are intended to be used by other plugins depending on your plugin, consider [bundling their sources](https://plugins.jetbrains.com/docs/intellij/bundling-plugin-openapi-sources.html) in the plugin distribution.

## Types

The IntelliJ Platform offers three types of services: application-level services (global singleton), project-level services, and module-level services.
For the latter two, a separate instance of the service is created for each instance of its corresponding scope, see [Project Model Introduction](https://plugins.jetbrains.com/docs/intellij/project-model.html).

Note:

Avoid using module-level services as it can increase memory usage for projects with many modules.

## Constructor

To improve startup performance, avoid any heavy initializations in the constructor.

Project/Module-level service constructors can have a [Project](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/Project.java)/[Module](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/module/Module.java) argument.

Warning: Do not use Constructor Injection

Using constructor injection of dependency services is deprecated (and not supported in [Light Services](#light-services)) for performance reasons.

Other service dependencies must be [acquired only when needed](#retrieving-a-service) in all corresponding methods, e.g., if you need a service to get some data or execute a task, retrieve the service before calling its methods.
Do not retrieve services in constructors to store them in class fields.

Use inspection Plugin DevKit | Code | Non-default constructors for service and extension class to verify code.

### Kotlin Coroutines

When using [Kotlin Coroutines](https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html), a distinct service [scope](https://plugins.jetbrains.com/docs/intellij/coroutine-scopes.html) can be injected as parameter.

The Application Service and Project Service scopes are bound to an application and project [service](#types) lifetimes accordingly.
They are children of the [Intersection Scopes](https://plugins.jetbrains.com/docs/intellij/coroutine-scopes.html#intersection-scopes), which means that they are canceled when the application/project is closed or a plugin is unloaded.

The service scope is provided to services via constructor injection.
The following constructor signatures are supported:

* `MyService(CoroutineScope)` for application and project services

* `MyProjectService(Project, CoroutineScope)` for project services

Each service instance receives its own scope instance.
The injected scopes' contexts contain [Dispatchers.Default](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-dispatchers/-default.html) and [CoroutineName(serviceClass)](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-name/).

See [Launching Coroutine From Service Scope](https://plugins.jetbrains.com/docs/intellij/launching-coroutines.html#launching-coroutine-from-service-scope) for full samples.

## Light Services

A service not going to be overridden or exposed as API to other plugins does not need to be registered in `[plugin.xml](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)` (see [Declaring a Service](#declaring-a-service)).
Instead, annotate the service class with [@Service](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/components/Service.java) (see [Examples](#examples)).
The service instance will be created in the scope according to the caller (see [Retrieving a Service](#retrieving-a-service)).

### Light Service Restrictions

* None of these attributes/restrictions (available for [registration of non-light services](#declaring-a-service)) is allowed: `id`, `os`, `client`, `overrides`, `configurationSchemaKey`/`preload` (Internal API).

* There is no separate headless/test implementation required.

* Service class must be `final`.

* [Constructor injection](#ctor) of dependency services is not supported.

* If an application-level service is a [PersistentStateComponent](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html), roaming must be disabled (`roamingType = RoamingType.DISABLED`).

Use these inspections to verify above restrictions and highlight non-light services that can be converted (2023.3):

* Plugin DevKit | Code | Light service must be final

* Plugin DevKit | Code | Mismatch between light service level and its constructor

* Plugin DevKit | Code | A service can be converted to a light one and corresponding Plugin DevKit | Plugin descriptor | A service can be converted to a light one for `plugin.xml`

### Examples

Java:

Application-level light service:

```JAVA

@Service
public final class MyAppService {

  public void doSomething(String param) {
    // ...
  }

}
```

Project-level light service example:

```JAVA

@Service(Service.Level.PROJECT)
public final class MyProjectService {

  private final Project myProject;

  MyProjectService(Project project) {
    myProject = project;
  }

  public void doSomething(String param) {
    String projectName = myProject.getName();
    // ...
  }

}
```

Kotlin:

Application-level light service:

```KOTLIN
@Service
class MyAppService {
  fun doSomething(param: String) {
    // ...
  }
}
```

Project-level light service example:

```KOTLIN
@Service(Service.Level.PROJECT)
class MyProjectService(private val project: Project) {
  fun doSomething(param: String) {
    val projectName = project.name
    // ...
  }
}
```

## Declaring a Service

To register a non-[Light Service](#light-services), distinct extension points are provided for each type:

* `com.intellij.applicationService` – application-level service

* `com.intellij.projectService` – project-level service

* `com.intellij.moduleService` – module-level service (not recommended, see [Note](#types))

The service implementation is specified in the required `serviceImplementation` attribute.

### Service API

To expose a service's API, create a separate class for `serviceInterface` and extend it in the corresponding class registered in `serviceImplementation`.
If `serviceInterface` isn't specified, it is supposed to have the same value as `serviceImplementation`.
Use inspection Plugin DevKit | Plugin descriptor | Plugin.xml extension registration to highlight redundant `serviceInterface` declarations.

### Additional Attributes

A service can be restricted to a certain OS via the `os` attribute.

To provide a custom implementation for test or headless environment, specify `testServiceImplementation` or `headlessImplementation` respectively.

### Examples

Java:

Application-level service:

* Interface: ```JAVA public interface MyAppService { void doSomething(String param); } ```

* Implementation: ```JAVA final class MyAppServiceImpl implements MyAppService { @Override public void doSomething(String param) { // ... } } ```

Project-level service:

* Interface: ```JAVA public interface MyProjectService { void doSomething(String param); } ```

* Implementation: ```JAVA final class MyProjectServiceImpl implements MyProjectService { private final Project myProject; MyProjectServiceImpl(Project project) { myProject = project; } public void doSomething(String param) { String projectName = myProject.getName(); // ... } } ```

Kotlin:

Application-level service:

* Interface: ```KOTLIN interface MyAppService { fun doSomething(param: String) } ```

* Implementation: ```KOTLIN internal class MyAppServiceImpl : MyAppService { override fun doSomething(param: String) { // ... } } ```

Project-level service:

* Interface: ```KOTLIN interface MyProjectService { fun doSomething(param: String) } ```

* Implementation: ```KOTLIN internal class MyProjectServiceImpl(private val project: Project) : MyProjectService { fun doSomething(param: String) { val projectName = project.name // ... } } ```

Registration in `plugin.xml`:

```XML

<extensions defaultExtensionNs="com.intellij">
  <!-- Declare the application-level service -->
  <applicationService
          serviceInterface="com.example.MyAppService"
          serviceImplementation="com.example.MyAppServiceImpl"/>

  <!-- Declare the project-level service -->
  <projectService
          serviceInterface="com.example.MyProjectService"
          serviceImplementation="com.example.MyProjectServiceImpl"/>
</extensions>
```

## Retrieving a Service

Warning: Correct Service Retrieval

Never acquire service instances prematurely or store them in fields for later use.
Instead, always obtain service instances directly and only at the location where they're needed.
Failing to do so will lead to unexpected exceptions and severe consequences for the plugin's functionality.

Such problems are highlighted via inspections (2023.3):

* Plugin DevKit | Code | Application service assigned to a static final field or immutable property

* Plugin DevKit | Code | Incorrect service retrieving

* Plugin DevKit | Code | Simplifiable service retrieving

Getting a service doesn't need a read action and can be performed from any thread.
If a service is requested from several [threads](https://plugins.jetbrains.com/docs/intellij/threading-model.html), it will be initialized in the first thread, and other threads will be blocked until it is fully initialized.

Java:

```JAVA
MyAppService applicationService =
    ApplicationManager.getApplication().getService(MyAppService.class);

MyProjectService projectService =
    project.getService(MyProjectService.class);
```

Service implementations can wrap these calls with convenient static `getInstance()` or `getInstance(Project)` method:

```JAVA
MyAppService applicationService = MyAppService.getInstance();

MyProjectService projectService = MyProjectService.getInstance(project);
```

Kotlin:

```KOTLIN
val applicationService = service<MyAppService>()

val projectService = project.service<MyProjectService>()
```

### Getting Service Flow

```PLANTUML
@startuml
skinparam monochrome true
skinparam DefaultFontName JetBrains Sans
skinparam DefaultFontSize 13
skinparam DefaultTextAlignment center
skinparam NoteTextAlignment left

' default 1.5
skinparam ActivityBorderThickness 1
' default 2
skinparam PartitionBorderThickness 1.5

:getService;
note right
  Allowed in any thread.
  Call on demand only.
  Never cache the result.
  Do not call in constructors
  unless needed.
end note

if (Is Light Service) then (yes)
else (no)
  if (Is Service Declaration Found) then (yes)
  else (no)
    :Return null;
    detach
  endif
endif

if (Is Created and Initialized?) then (yes)
else (no)
  if (Is Container Active?) then (yes)
    partition "synchronized\non service class" {
      if (Is Created and Initialized?) then (yes)
      else (no)
        if (Is Initializing?) then (yes)
          :Throw
          PluginException
          (Cyclic Service
          Initialization);
          detach
        else (no)
          partition "non-cancelable" {
            :Create Instance]
            note right
              Avoid getting other
              services to reduce
              the initialization tree.
              The fewer the
              dependencies,
              the faster and more
              reliable initialization.
            end note

            :Register to be Disposed
            on Container Dispose
            (Disposable only)]
            :Load Persistent State
            (PersistentStateComponent
            only)]
          }
        endif
      endif
    }
  else (disposed or dispose in progress)
    :Throw
    ProcessCanceledException;
    detach
  endif
endif

:Return Instance;

@enduml
```

## Sample Plugin

To clarify how to use services, consider the maxOpenProjects sample plugin available in the [code samples](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/max_opened_projects).

This plugin has an application service counting the number of currently opened projects in the IDE.
If this number exceeds the maximum number of simultaneously opened projects allowed by the plugin (3), it displays an information message.

See [Code Samples](https://plugins.jetbrains.com/docs/intellij/code-samples.html) on how to set up and run the plugin.

> Source: IntelliJ Platform SDK docs — Services (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

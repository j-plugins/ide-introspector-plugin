---
id: sdk.services.retrieving-a-service
title: Services: Retrieving a Service
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, retrieving, service]
---
Part of `sdk.services`.

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

> Source: IntelliJ Platform SDK docs — Services: Retrieving a Service (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

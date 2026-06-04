---
id: sdk.services.retrieving-a-service
title: Services: Retrieving a Service
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, retrieving, service]
---
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

### Getting Service Flow (services/retrieving-a-service/getting-service-flow.md)

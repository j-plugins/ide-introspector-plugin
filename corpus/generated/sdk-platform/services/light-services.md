---
id: sdk.services.light-services
title: Services: Light Services
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, light, services]
---
Part of `sdk.services`.

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

> Source: IntelliJ Platform SDK docs — Services: Light Services (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

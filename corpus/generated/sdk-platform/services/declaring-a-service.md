---
id: sdk.services.declaring-a-service
title: Services: Declaring a Service
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, declaring, service]
---
Part of `sdk.services`.

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

> Source: IntelliJ Platform SDK docs — Services: Declaring a Service (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

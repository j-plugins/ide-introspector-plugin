---
id: sdk.services.declaring-a-service.examples
title: Services: Examples
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, examples]
---
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


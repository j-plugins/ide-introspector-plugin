---
id: sdk.services.light-services.examples
title: Services: Examples
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, examples]
---
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


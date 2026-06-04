---
id: sdk.services.constructor
title: Services: Constructor
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, constructor]
---
To improve startup performance, avoid any heavy initializations in the constructor.

Project/Module-level service constructors can have a [Project](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/Project.java)/[Module](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/module/Module.java) argument.

Warning: Do not use Constructor Injection

Using constructor injection of dependency services is deprecated (and not supported in [Light Services](#light-services)) for performance reasons.

Other service dependencies must be [acquired only when needed](#retrieving-a-service) in all corresponding methods, e.g., if you need a service to get some data or execute a task, retrieve the service before calling its methods.
Do not retrieve services in constructors to store them in class fields.

Use inspection Plugin DevKit | Code | Non-default constructors for service and extension class to verify code.

### Kotlin Coroutines (services/constructor/kotlin-coroutines.md)

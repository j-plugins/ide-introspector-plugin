---
id: sdk.services.types
title: Services: Types
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, types]
---
The IntelliJ Platform offers three types of services: application-level services (global singleton), project-level services, and module-level services.
For the latter two, a separate instance of the service is created for each instance of its corresponding scope, see [Project Model Introduction](https://plugins.jetbrains.com/docs/intellij/project-model.html).

Note:

Avoid using module-level services as it can increase memory usage for projects with many modules.


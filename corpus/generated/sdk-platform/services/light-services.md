---
id: sdk.services.light-services
title: Services: Light Services
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, light, services]
---
A service not going to be overridden or exposed as API to other plugins does not need to be registered in `[plugin.xml](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)` (see [Declaring a Service](#declaring-a-service)).
Instead, annotate the service class with [@Service](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/components/Service.java) (see [Examples](#examples)).
The service instance will be created in the scope according to the caller (see [Retrieving a Service](#retrieving-a-service)).

### Light Service Restrictions (services/light-services/light-service-restrictions.md)
### Examples (services/light-services/examples.md)

---
id: sdk.services.declaring-a-service
title: Services: Declaring a Service
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, declaring, service]
---
To register a non-[Light Service](#light-services), distinct extension points are provided for each type:

* `com.intellij.applicationService` – application-level service

* `com.intellij.projectService` – project-level service

* `com.intellij.moduleService` – module-level service (not recommended, see [Note](#types))

The service implementation is specified in the required `serviceImplementation` attribute.

### Service API (services/declaring-a-service/service-api.md)
### Additional Attributes (services/declaring-a-service/additional-attributes.md)
### Examples (services/declaring-a-service/examples.md)

---
id: sdk.services.declaring-a-service.service-api
title: Services: Service API
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, service, api]
---
To expose a service's API, create a separate class for `serviceInterface` and extend it in the corresponding class registered in `serviceImplementation`.
If `serviceInterface` isn't specified, it is supposed to have the same value as `serviceImplementation`.
Use inspection Plugin DevKit | Plugin descriptor | Plugin.xml extension registration to highlight redundant `serviceInterface` declarations.


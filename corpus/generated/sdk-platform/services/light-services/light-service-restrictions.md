---
id: sdk.services.light-services.light-service-restrictions
title: Services: Light Service Restrictions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, light, service, restrictions]
---
* None of these attributes/restrictions (available for [registration of non-light services](#declaring-a-service)) is allowed: `id`, `os`, `client`, `overrides`, `configurationSchemaKey`/`preload` (Internal API).

* There is no separate headless/test implementation required.

* Service class must be `final`.

* [Constructor injection](#ctor) of dependency services is not supported.

* If an application-level service is a [PersistentStateComponent](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html), roaming must be disabled (`roamingType = RoamingType.DISABLED`).

Use these inspections to verify above restrictions and highlight non-light services that can be converted (2023.3):

* Plugin DevKit | Code | Light service must be final

* Plugin DevKit | Code | Mismatch between light service level and its constructor

* Plugin DevKit | Code | A service can be converted to a light one and corresponding Plugin DevKit | Plugin descriptor | A service can be converted to a light one for `plugin.xml`


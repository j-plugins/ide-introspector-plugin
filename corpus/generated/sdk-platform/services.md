---
id: sdk.services
title: Services
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, services]
---
A service is a plugin component loaded on demand when your plugin calls the `getService()` method of corresponding [ComponentManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/extensions/src/com/intellij/openapi/components/ComponentManager.java) instance (see [Types](#types)).
The IntelliJ Platform ensures that only one instance of a service is loaded even though it is called several times.
Services are used to encapsulate logic operating on a set of related classes or to provide some reusable functionality that can be used across the plugin project.
Conceptually, they don't differ from the service classes in other languages or frameworks.

A service must have an implementation class used for service instantiation.
A service may also have an interface class used to obtain the service instance and provide the service's API.

A service needing a shutdown hook/cleanup routine can implement [Disposable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/Disposable.java) and perform necessary work in `dispose()` (see [Automatically Disposed Objects](https://plugins.jetbrains.com/docs/intellij/disposers.html#automatically-disposed-objects)).

Note: Services as API

If declared services are intended to be used by other plugins depending on your plugin, consider [bundling their sources](https://plugins.jetbrains.com/docs/intellij/bundling-plugin-openapi-sources.html) in the plugin distribution.

## Subtopics

- Types — `sdk.services.types`
- Constructor — `sdk.services.constructor`
- Light Services — `sdk.services.light-services`
- Declaring a Service — `sdk.services.declaring-a-service`
- Retrieving a Service — `sdk.services.retrieving-a-service`
- Sample Plugin — `sdk.services.sample-plugin`

> Source: IntelliJ Platform SDK docs — Services (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

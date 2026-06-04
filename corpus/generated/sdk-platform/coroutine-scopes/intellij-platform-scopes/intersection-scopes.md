---
id: sdk.coroutine-scopes.intellij-platform-scopes.intersection-scopes
title: Coroutine Scopes: Intersection Scopes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, intersection, scopes]
---
* Application×Plugin - a scope which is an intersection of the Application and Plugin scopes. It is canceled when the application is shutdown or the corresponding plugin is unloaded. This triggers the cancellation of its children and the Project×Plugin scope and, subsequently, its children.

* Project×Plugin - a scope which is an intersection of the Project and Plugin scopes. It is canceled when a project is being closed or the corresponding plugin is unloaded.

Intersection scopes enable creating coroutines whose lifetime is limited by application/project and plugin lifetimes, e.g.,
application/project [services](https://plugins.jetbrains.com/docs/intellij/plugin-services.html) provided by a plugin.


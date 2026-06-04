---
id: sdk.disposer-and-disposable.automatically-disposed-objects
title: Disposer and Disposable: Automatically Disposed Objects
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, automatically, disposed, objects]
---
Many objects are disposed automatically by the platform if they implement the `Disposable` interface.
The most important type of such objects is [services](https://plugins.jetbrains.com/docs/intellij/plugin-services.html).
The platform automatically disposes application-level services when the IDE is closed or the plugin providing the service is unloaded.
Project-level services are disposed on project close or plugin unload events.

Note that extensions registered in `[plugin.xml](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)` are not automatically disposed.
If an extension requires executing some code to dispose it, you need to define a service and to put the code in its `dispose()` method or use it as a parent disposable.


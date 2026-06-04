---
id: sdk.disposer-and-disposable.the-disposer-singleton
title: Disposer and Disposable: The `Disposer` Singleton
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, disposer, singleton]
---
Part of `sdk.disposer-and-disposable`.

The `Disposer` Singleton

The primary purpose of the [Disposer](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/util/Disposer.java) singleton is to enforce the rule that a child `Disposable` never outlives its parent.

The `Disposer` organizes `Disposable` objects in a tree of parent-child relationships.
The tree of `Disposable` objects ensures the `Disposer` releases children of a parent first.
Parent objects always live longer than their children.

The following diagram shows a simplified example of `Disposer`'s tree:

```PLANTUML
@startuml

skinparam DefaultFontName JetBrains Sans
skinparam DefaultFontSize 13
skinparam DefaultTextAlignment center
hide empty members
hide circle

rectangle "Root\nDisposable" as root

rectangle "Application" as application
rectangle "App\nListener" as listener
rectangle "Dialog\nWrapper" as dialogDisposable
rectangle "Dialog\nResource" as dialogResource

rectangle "Services of\nApplication" as applicationServices
rectangle "App\nService 1" as appService1
rectangle "App\nService 2" as appService2

rectangle "My\nProject" as project
rectangle "My\nListener" as projectListener
rectangle "My\nAlarm" as projectAlarm

rectangle "Services of\nMy Project" as projectServices
rectangle "Project\nService A" as projectService1
rectangle "Project\nService B" as projectService2

root -- application
root -- applicationServices
root -- project
root -- projectServices

application -- listener
application -- dialogDisposable
dialogDisposable -- dialogResource

applicationServices -- appService1
applicationServices -- appService2

project -- projectListener
project -- projectAlarm

projectServices -- projectService1
projectServices -- projectService2

@enduml
```

When My Project is closed and its disposal is triggered by the platform, the Disposer API will dispose My Listener and My Alarm before My Project, and Project Service A and Project Service B before Services of My Project.

See [The Disposable Interface](#implementing-the-disposable-interface) for more information about creating `Disposable` classes.

Registering a disposable is performed by calling `Disposer.register()`:

```JAVA
Disposer.register(parentDisposable, childDisposable);
```

## Subtopics

- Choosing a Disposable Parent — `sdk.disposer-and-disposable.the-disposer-singleton.choosing-a-disposable-parent`
- Registering Listeners with Parent Disposable — `sdk.disposer-and-disposable.the-disposer-singleton.registering-listeners-with-parent-disposable`
- Determining Disposal Status — `sdk.disposer-and-disposable.the-disposer-singleton.determining-disposal-status`
- Ending a Disposable Lifecycle — `sdk.disposer-and-disposable.the-disposer-singleton.ending-a-disposable-lifecycle`

> Source: IntelliJ Platform SDK docs — Disposer and Disposable: The `Disposer` Singleton (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

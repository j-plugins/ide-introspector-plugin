# The `Disposer` Singleton

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

### Choosing a Disposable Parent (sdk.disposer-and-disposable.the-disposer-singleton.choosing-a-disposable-parent)
### Registering Listeners with Parent Disposable

Many IntelliJ Platform APIs for registering listeners either require passing a parent disposable or have overloads that take a parent disposable, for example:

```JAVA
public interface PomModel {
  // ...
  void addModelListener(PomModelListener listener);
  void addModelListener(PomModelListener listener, Disposable parentDisposable);
  void removeModelListener(PomModelListener listener);
}
```

Methods with a `parentDisposable` parameter automatically unsubscribe the listener when the corresponding parent disposable is disposed.
Using such methods is always preferable to removing listeners explicitly from the `dispose` method because it requires less code and is easier to verify for correctness.

The same rules apply to [message bus](https://plugins.jetbrains.com/docs/intellij/messaging-infrastructure.html) connections.

Always pass a parent disposable to `MessageBus.connect(parentDisposable)`, and make sure it has the shortest possible lifetime.
To choose the correct parent disposable, use the guidelines from the [previous section](#choosing-a-disposable-parent).

### Determining Disposal Status

You can use `Disposer.isDisposed()` to check whether a `Disposable` has already been disposed.
This check is useful, for example, for an asynchronous callback to a `Disposable` that may be disposed before the callback is executed.
In such a case, the best strategy is usually to do nothing and return early.

Warning:

Non-disposed objects shouldn't hold onto references to disposed objects, as this constitutes a memory leak.
Once a `Disposable` is released, it should be completely inactive, and there's no reason to refer to it anymore.

### Ending a Disposable Lifecycle

A plugin can manually end a `Disposable` lifecycle by calling `Disposer.dispose(Disposable)`.
This method handles recursively disposing of all the `Disposable` child descendants as well.

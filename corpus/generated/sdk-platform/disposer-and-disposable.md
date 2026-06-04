# Disposer and Disposable

The IntelliJ Platform's [Disposer](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/util/Disposer.java) facilitates resource cleanup.
If a subsystem keeps a set of resources alive coincidently with a parent object's lifetime, the subsystem's resources should be registered with the `Disposer` to be released before or at the same time as the parent object.

Listeners are the most common resource type managed by `Disposer`, but there are other possible types:

* File handles, and database connections,

* Caches and other significant data structures.

The `Disposer` is a singleton that manages a tree of [Disposable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/Disposable.java) instances.
A `Disposable` is an interface for any object providing a `Disposable.dispose()` method to release heavyweight resources after a specific lifetime.

The `Disposer` supports chaining `Disposable` objects in parent-child relationships.

## Automatically Disposed Objects

Many objects are disposed automatically by the platform if they implement the `Disposable` interface.
The most important type of such objects is [services](https://plugins.jetbrains.com/docs/intellij/plugin-services.html).
The platform automatically disposes application-level services when the IDE is closed or the plugin providing the service is unloaded.
Project-level services are disposed on project close or plugin unload events.

Note that extensions registered in `[plugin.xml](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html)` are not automatically disposed.
If an extension requires executing some code to dispose it, you need to define a service and to put the code in its `dispose()` method or use it as a parent disposable.

## The `Disposer` Singleton (sdk.disposer-and-disposable.the-disposer-singleton)
## Implementing the `Disposable` Interface

Implementing the `Disposable` Interface

Creating a class requires implementing the `Disposable` interface and defining the `dispose()` method.

In many cases, when the object implements `Disposable` only to be used as a parent disposable, the method's implementation will be completely empty.

An example of a non-trivial `dispose` implementation is shown below:

```JAVA
public class Foo<T> extends JBFoo implements Disposable {

  public Foo(@NotNull Project project,
             @NotNull String name,
             @Nullable FileEditor fileEditor,
             @NotNull Disposable parentDisposable) {
    this(project, name, fileEditor, InitParams.createParams(project),
        DetachedToolWindowManager.getInstance(project));
    Disposer.register(parentDisposable, this);
  }

  @Override
  public void dispose() {
    myFooManager.unregister(this);
    myDetachedToolWindowManager.unregister(myFileEditor);
    KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .removePropertyChangeListener("focusOwner", myMyPropertyChangeListener);
    setToolContext(null);
  }
}
```

A lot of code setting up all the conditions requiring release in `dispose()` has been omitted for simplicity.

Regardless, it illustrates the basic pattern, which is:

* In this case, the parent disposable is passed into the constructor,

* The `Foo` disposable is registered as a child of `parentDisposable` in the constructor.

* The `dispose()` method consolidates the necessary release actions and will be called by the `Disposer`.

Warning:

Never call `Disposable.dispose()` directly because it bypasses the parent-child relationships established in `Disposer`.
Always call `Disposer.dispose(Disposable)` instead.

## Diagnosing `Disposer` Leaks (sdk.disposer-and-disposable.diagnosing-disposer-leaks)

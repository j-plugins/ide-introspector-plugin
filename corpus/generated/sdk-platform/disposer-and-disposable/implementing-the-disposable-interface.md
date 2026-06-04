---
id: sdk.disposer-and-disposable.implementing-the-disposable-interface
title: Disposer and Disposable: Implementing the `Disposable` Interface
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, implementing, disposable, interface]
---
Part of `sdk.disposer-and-disposable`.

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

> Source: IntelliJ Platform SDK docs — Disposer and Disposable: Implementing the `Disposable` Interface (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

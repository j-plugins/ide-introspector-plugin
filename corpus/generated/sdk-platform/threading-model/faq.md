# FAQ

### How to check whether the current thread is the EDT/UI thread?

Use `Application.isDispatchThread()`.

If code must be invoked on EDT and the current thread can be EDT or BGT, use [UIUtil.invokeLaterIfNeeded()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/ui/src/com/intellij/util/ui/UIUtil.java).
If the current thread is EDT, this method will run code immediately or will schedule a later invocation if the current thread is BGT.

### Why write actions are currently allowed only on EDT?

Reading data model was often performed on EDT to display results in the UI.
The IntelliJ Platform is more than 20 years old, and in its beginnings Java didn't offer features like generics and lambdas.
Code that acquired read locks was very verbose.
For convenience, it was decided that reading data can be done on EDT without read locks (even implicitly acquired).

The consequence of this was that writing had to be allowed only on EDT to avoid read/write conflicts.
The nature of EDT provided this possibility out-of-the-box due to being a single thread.
Event queue guaranteed that reads and writes were ordered and executed one by one and couldn't interweave.

### Why can write intent lock be acquired from any thread but write lock only from EDT?

In the current platform state, technically, write intent lock can be acquired on any thread (it is done only on EDT in practice), but write lock can be acquired only on EDT.

Write intent lock was introduced as a "replacement" for EDT in the context of acquiring write lock.
Instead of allowing to acquire write lock on EDT only, it was planned to make it possible to acquire it from under write intent lock on any thread.
Write intent lock provides read access that was also available on EDT.
This behavior wasn't enabled in production, and the planned locking mechanism has changed.
It is planned to allow for acquiring write lock from any thread, even without a write intent lock.
Write intent lock will be still available and will allow performing read sessions finished with data writing.

Note: Something missing?

If a topic is not covered in the above sections,
let us know via the Feedback widget displayed on the right,
or [other channels](https://plugins.jetbrains.com/docs/intellij/getting-help.html#problems-with-the-guide).

Be specific about the topics and reasons for adding them and leave your email in case we need
more details. Thanks for your feedback!

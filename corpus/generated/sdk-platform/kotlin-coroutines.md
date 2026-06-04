# Kotlin Coroutines

The IntelliJ Platform is a multithreading environment that executes many asynchronous and non-blocking tasks to avoid UI freezes.
These tasks are usually executed in background threads, which is a standard approach in the JVM world.

Since version 1.1, [Kotlin](https://plugins.jetbrains.com/docs/intellij/using-kotlin.html) has introduced coroutines as a lightweight and cleaner abstraction over threads, allowing them to be utilized more efficiently.
The IntelliJ Platform started adapting coroutines in its APIs and internal code, and since 2024.1 it is recommended to use the coroutines approach over threads.

Warning:

Plugins must use the bundled Kotlin Coroutines library, see [Kotlin Coroutines Libraries (kotlinx.coroutines)](https://plugins.jetbrains.com/docs/intellij/using-kotlin.html#coroutinesLibraries).

## Coroutines Advantages

The reason for coroutines being lightweight is the fact that they aren't bound to OS native threads, as opposed to the JVM threads.
It enables much less memory consumption and more efficient context switching, which makes the platform and plugins more performant.
For example, it is straightforward to run 100.000 coroutines on a standard computer, which is not possible with threads as it would cause `OutOfMemoryError`.

Besides performance, there are more advantages of using coroutines:

* Coroutines greatly simplify the way of writing non-blocking code. What was usually implemented with hard to understand, implement, and maintain callbacks, with coroutines looks like regular sequential/imperative code.

* Coroutines allow for implementing structured concurrency (coroutines can spawn child coroutines), which allows for easily managing the lifecycle of concurrent tasks and error handling. For example, cancelling a parent coroutine automatically cancels all child coroutines.

* It is trivial to switch execution of the code parts between [UI and background threads](https://plugins.jetbrains.com/docs/intellij/threading-model.html).

## Java Interoperability

Coroutines provide very limited Java interoperability, and coroutine-based APIs can’t be used to the full extent from Java code.

Kotlin Coroutines are relatively new to the IntelliJ Platform and aren't yet widely adopted in public APIs.
In the future, the number of coroutine-based APIs will grow, and using only Java may not be enough to implement a fully functional plugin.
It will be required to use [Kotlin](https://plugins.jetbrains.com/docs/intellij/using-kotlin.html), at least partially, for example, to implement coroutine-based [extension points](https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html).

## Learning Resources

Before going to the next coroutine-related sections, it is highly recommended to go through the following resources.
It will help understand coroutines and become fluent with available APIs:

* [Official Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)

* KotlinConf talks by Roman Elizarov (Kotlin Coroutines architect): * [Introduction to Coroutines](https://www.youtube.com/watch?v=_hfBv0a09Jc) * [Deep Dive into Coroutines on JVM](https://www.youtube.com/watch?v=YrrUCSi72E8) * [Coroutines in Practice](https://www.youtube.com/watch?v=a3agLJQ6vt8) * [Asynchronous Data Streams with Flow](https://www.youtube.com/watch?v=tYcqn48SMT8)

Note: Something missing?

If a topic is not covered in the above sections,
let us know via the Feedback widget displayed on the right,
or [other channels](https://plugins.jetbrains.com/docs/intellij/getting-help.html#problems-with-the-guide).

Be specific about the topics and reasons for adding them and leave your email in case we need
more details. Thanks for your feedback!

> Source: IntelliJ Platform SDK docs — Kotlin Coroutines (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

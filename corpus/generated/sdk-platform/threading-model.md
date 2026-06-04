---
id: sdk.threading-model
title: Threading Model
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, threading, model]
---
Tip:

It is highly recommended that readers unfamiliar with Java threads go through the official [Java Concurrency](https://docs.oracle.com/javase/tutorial/essential/concurrency/index.html) tutorial before reading this section.

The IntelliJ Platform is a highly concurrent environment.
Code is executed in many threads simultaneously.
In general, as in a regular [Swing](https://docs.oracle.com/javase/tutorial/uiswing/) application, threads can be categorized into two main groups:

* [Event Dispatch Thread](https://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html) (EDT) – also known as the UI thread. Its main purpose is handling UI events (such as reacting to clicking a button or updating the UI), but the platform uses it also for writing data. EDT executes events taken from the Event Queue. Operations performed on EDT must be as fast as possible to not block other events in the queue and freeze the UI.

* background threads (BGT) – used for performing long-running and costly operations, or background tasks.

There is only one EDT and multiple BGT in the running application:

```MERMAID
---
config:
  gantt:
    numberSectionStyles: 2
displayMode: compact
---
gantt
    dateFormat X
    %% do not remove trailing space in axisFormat:
    axisFormat ‎
    section EDT
        UI task : 0, 1
        UI task : 1, 2
        write   : 3, 4
        UI task : 4, 5
        write   : 5, 6
        UI task : 7, 8
        UI task : 8, 9
        write   : 9, 10
    section BGT 1
        task : done, 0, 3
        task : done, 4, 6
        task : done, 7, 10
    section BGT 2
        task : done, 1, 2
        task : done, 3, 7
        task : done, 8, 10
    section ...
        ‎ : 0, 0
    section BGT N
        task : done, 0, 2
        task : done, 3, 6
        task : done, 7, 8
        task : done, 9, 10
```

It is possible to switch between BGT and EDT in both directions.
Operations can be scheduled to execute on EDT from BGT (and EDT) with `invokeLater()` methods (see the rest of this page for details).
Executing on BGT from EDT can be achieved with [background processes](https://plugins.jetbrains.com/docs/intellij/background-processes.html).

Warning:

Plugins targeting versions 2024.1+ should use [coroutine dispatchers](https://plugins.jetbrains.com/docs/intellij/coroutine-dispatchers.html) for switching between threads.

## Subtopics

- Read-Write Lock — `sdk.threading-model.read-write-lock`
- Accessing Data — `sdk.threading-model.accessing-data`
- Invoking Operations on EDT and Modality — `sdk.threading-model.invoking-operations-on-edt-and-modality`
- Non-Blocking Read Actions — `sdk.threading-model.non-blocking-read-actions`
- Avoiding UI Freezes — `sdk.threading-model.avoiding-ui-freezes`
- Kotlin Notebooks — `sdk.threading-model.kotlin-notebooks`
- FAQ — `sdk.threading-model.faq`

> Source: IntelliJ Platform SDK docs — Threading Model (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

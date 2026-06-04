---
id: sdk.persistence-model
title: Persistence Model
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, persistence, model]
---
The IntelliJ Platform Persistence Model is used to store a variety of information.
For example, [Run Configurations](https://plugins.jetbrains.com/docs/intellij/run-configurations.html) and [Settings](https://plugins.jetbrains.com/docs/intellij/settings.html) are stored using the Persistence Model.

There are two distinct approaches, depending on the type of data being persisted:

* [Persisting State of Components](https://plugins.jetbrains.com/docs/intellij/persisting-state-of-components.html)

* [Persisting Sensitive Data](https://plugins.jetbrains.com/docs/intellij/persisting-sensitive-data.html)

[Split plugins](https://plugins.jetbrains.com/docs/intellij/split-mode-and-remote-development.html) may also need explicit synchronization metadata for persisted settings.
See [Persistent State Component in Split Mode](https://plugins.jetbrains.com/docs/intellij/persistent-state-in-split-mode.html).

> Source: IntelliJ Platform SDK docs — Persistence Model (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

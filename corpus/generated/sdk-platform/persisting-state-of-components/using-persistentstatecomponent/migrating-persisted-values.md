---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.migrating-persisted-values
title: Persisting State of Components: Migrating Persisted Values
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, migrating, persisted, values]
---
Part of `sdk.persisting-state-of-components.using-persistentstatecomponent`.

If the underlying persistence model or storage format has changed, a [ConverterProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-impl/src/com/intellij/conversion/ConverterProvider.java) can provide [ProjectConverter](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-impl/src/com/intellij/conversion/ProjectConverter.java), whose `getAdditionalAffectedFiles()` method returns affected files to migrate and performs programmatic migration of stored values.

> Source: IntelliJ Platform SDK docs — Persisting State of Components: Migrating Persisted Values (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

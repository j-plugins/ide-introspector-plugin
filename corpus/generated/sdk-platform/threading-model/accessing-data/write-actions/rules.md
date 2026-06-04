---
id: sdk.threading-model.accessing-data.write-actions.rules
title: Threading Model: Rules
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, rules]
---
2023.3+:

Writing data is only allowed on EDT invoked with `Application.invokeLater()`.

Write operations must always be wrapped in a write action with one of the [API](#write-actions-api) methods.

Modifying the model is only allowed from write-safe contexts (see [Invoking Operations on EDT and Modality](#invoking-operations-on-edt-and-modality)).

Earlier versions:

Writing data is only allowed on EDT.

Write operations must always be wrapped in a write action with one of the [API](#write-actions-api) methods.

Modifying the model is only allowed from write-safe contexts, including user actions and `SwingUtilities.invokeLater()` calls from them (see [Invoking Operations on EDT and Modality](#invoking-operations-on-edt-and-modality)).

It is forbidden to modify PSI, VFS, or project model from inside UI renderers or `SwingUtilities.invokeLater()`.

Tip:

[Thread Access Info](https://plugins.jetbrains.com/plugin/16815-thread-access-info) plugin visualizes Read/Write Access and Thread information in the debugger.


---
id: sdk.threading-model.accessing-data.read-actions.rules
title: Threading Model: Rules
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, rules]
---
2023.3+:

Reading data is allowed from any thread.

Reading data on EDT invoked with `Application.invokeLater()` doesn't require an explicit read action, as the write intent lock allowing to read data is [acquired implicitly](#locks-and-edt).

Earlier versions:

Reading data is allowed from any thread.

Reading data on EDT doesn't require an explicit read action, as the write intent lock allowing to read data is [acquired implicitly](#locks-and-edt).

In all other cases, it is required to wrap a read operation in a read action with one of the [API](#read-actions-api) methods.

##### Objects Validity (threading-model/accessing-data/read-actions/rules/objects-validity.md)

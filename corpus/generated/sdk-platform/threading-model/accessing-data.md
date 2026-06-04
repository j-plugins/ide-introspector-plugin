---
id: sdk.threading-model.accessing-data
title: Threading Model: Accessing Data
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, accessing, data]
---
The IntelliJ Platform provides a simple API for accessing data under read or write locks in the form of read and write actions.

Read and write actions allow executing a piece of code under a lock, automatically acquiring it before an action starts, and releasing it after the action is finished.

Warning: Minimize Locking Scopes

Always try to wrap only the required operations into read/write actions, minimizing the time of holding locks.
If the read operation itself is long, consider using [non-blocking read actions](#non-blocking-read-actions) to avoid blocking the write lock and EDT.

### Read Actions (threading-model/accessing-data/read-actions.md)
#### API (threading-model/accessing-data/read-actions/api.md)
##### Alternative APIs (threading-model/accessing-data/read-actions/api/alternative-apis.md)
#### Rules (threading-model/accessing-data/read-actions/rules.md)
##### Objects Validity (threading-model/accessing-data/read-actions/rules/objects-validity.md)
### Write Actions (threading-model/accessing-data/write-actions.md)
#### API (threading-model/accessing-data/write-actions/api.md)
##### Alternative APIs (threading-model/accessing-data/write-actions/api/alternative-apis.md)
#### Rules (threading-model/accessing-data/write-actions/rules.md)

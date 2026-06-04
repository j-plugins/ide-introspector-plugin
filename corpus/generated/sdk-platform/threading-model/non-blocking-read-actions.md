---
id: sdk.threading-model.non-blocking-read-actions
title: Threading Model: Non-Blocking Read Actions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, non, blocking, read, actions]
---
BGT shouldn't hold [read locks](#read-actions) for a long time.
The reason is that if EDT needs a write action (for example, the user types something in the editor), it must be acquired as soon as possible.
Otherwise, the UI will freeze until all BGTs have released their read actions.
The following diagram presents this problem:

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
    section BGT
        very long read action               : 0, 6
    section EDT
        write action (waiting for the lock) : done, 2, 6
        write action (executing)            : 6, 8
        UI freeze                           : crit, 2, 8
        UI update (delayed)                 : crit, 8, 10
```

Sometimes, it is required to run a long read action, and it isn't possible to speed it up.
In such a case, the recommended approach is to cancel the read action whenever there is a write action about to occur and restart that read action later from scratch:

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
    section BGT
        very long read action               : 0, 2
        very long read action (2nd attempt) : 4, 10
        RA canceled                         : milestone, crit, 0, 4
        RA restarted from scratch           : milestone, 4, 4
    section EDT
        write action : 2, 4
        UI update    : 4, 6
```

In this case, the EDT won't be blocked and the UI freeze is avoided.
The total execution time of the read action will be longer due to multiple attempts, but not affecting the UI responsiveness is more important.

The canceling approach is widely used in various areas of the IntelliJ Platform: editor highlighting, code completion, "go to class/file/…" actions all work like this.
Read the [Background Processes](https://plugins.jetbrains.com/docs/intellij/background-processes.html) section for more details.

Warning:

Note that APIs mentioned in [Read Actions API](#read-actions-api) (except suspending `readAction()`) are blocking.

### Non-Blocking Read Actions API (threading-model/non-blocking-read-actions/non-blocking-read-actions-api.md)

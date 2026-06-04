# Invoking Operations on EDT and Modality

Operations that write data on EDT should be invoked with `Application.invokeLater()` because it allows specifying the modality state ([ModalityState](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/ModalityState.java)) for the scheduled operation.
This is not supported by `SwingUtilities.invokeLater()` and similar APIs.

Warning:

Note that `Application.invokeLater()` must be used to write data in versions 2023.3+.

`ModalityState` represents the stack of active modal dialogs and is used in calls to `Application.invokeLater()` to ensure the scheduled runnable can execute within the given modality state, meaning when the same set of modal dialogs or a subset is present.

To better understand what problem `ModalityState` solves, consider the following scenario:

1. A user action is started.

2. In the meantime, another operation is scheduled on EDT with `SwingUtilities.invokeLater()` (without modality state support).

3. The action from 1. now shows a dialog asking a Yes/No question.

4. While the dialog is shown, the operation from 2. is now processed and does changes to the data model, which invalidates PSI.

5. The user clicks Yes or No in the dialog, and it executes some code based on the answer.

6. Now, the code to be executed as the result of the user's answer has to deal with the changed data model it was not prepared for. For example, it was supposed to execute changes in the PSI that might be already invalid.

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
        1. Start action         : 0, 2
        3. Show dialog          : 2, 3
        4. Modify data          : crit, active, 3, 4
        5. Answer dialog        : 4, 5
        6. Work on invalid data : crit, 5, 7
    section BGT
        2. invokeLater()        : crit, active, 1, 2
```

Passing the modality state solves this problem:

1. A user action is started.

2. In the meantime, another operation is scheduled on EDT with `Application.invokeLater()` (supporting modality state).
The operation is scheduled with `ModalityState.defaultModalityState()` (see the table below for other helper methods).

3. The action from 1. now shows a dialog asking a Yes/No question.
This adds a modal dialog to the modality state stack.

4. While the dialog is shown, the scheduled operation waits as it was scheduled with a "lower" modality state than the current state with an additional dialog.

5. The user clicks Yes or No in the dialog, and it executes some code based on the answer.

6. The code is executed on data in the same state as before the dialog was shown.

7. The operation from 1. is executed now without interfering with the user's action.

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
        1. Start action          : 0, 2
        3. Show dialog           : 2, 3
        5. Answer dialog         : 3, 4
        6. Work on correct data  : 4, 6
        7. Modify data           : active, 6, 7
    section BGT
        2. invokeLater()         : active, 1, 2
        4. Wait for dialog close : done, 2, 4
```

The following table presents methods providing useful modality states to be passed to `Application.invokeLater()`:

| [ModalityState](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/application/ModalityState.java) |Description |
-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
| `defaultModalityState()`  Used if none specified   |If invoked from EDT, it uses the `ModalityState.current()`.  If invoked from a background process started with `ProgressManager`, the operation can be executed in the same dialog that the process started.  This is the optimal choice in most cases.   |
| `current()` |The operation can be executed when the modality state stack doesn't grow since the operation was scheduled. |
| `stateForComponent()` |The operation can be executed when the topmost shown dialog is the one that contains the specified component or is one of its parent dialogs. |
| `nonModal()` or  `NON_MODAL`   |The operation will be executed after all modal dialogs are closed. If any of the open (unrelated) projects displays a per-project modal dialog, the operation will be performed after the dialog is closed. |
| `any()` |The operation will be executed as soon as possible regardless of modal dialogs (the same as with `SwingUtilities.invokeLater()`). It can be used for scheduling only pure UI operations. Modifying PSI, VFS, or project model is prohibited.Don't use it unless absolutely needed.   |

Note:

If EDT activity needs to access a [file-based index](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html) (for example, it is doing any project-wide PSI analysis, resolves references, or performs other tasks depending on indexes), use [DumbService.smartInvokeLater()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/DumbService.kt).
This API also supports `ModalityState` and runs the operation after all possible indexing processes have been completed.

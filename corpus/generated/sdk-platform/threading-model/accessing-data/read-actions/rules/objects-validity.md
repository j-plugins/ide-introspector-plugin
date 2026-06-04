---
id: sdk.threading-model.accessing-data.read-actions.rules.objects-validity
title: Threading Model: Objects Validity
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, objects, validity]
---
The read objects aren't guaranteed to survive between several consecutive read actions.
Whenever starting a read action, check if the PSI/VFS/project/module is still valid.
Example:

Kotlin:

```KOTLIN
val virtualFile = runReadAction { // read action 1
  // read a virtual file
}
// do other time-consuming work...
val psiFile = runReadAction { // read action 2
  if (virtualFile.isValid()) { // check if the virtual file is valid
    PsiManager.getInstance(project).findFile(virtualFile)
  } else null
}
```

Java:

```JAVA
VirtualFile virtualFile = ReadAction.compute(() -> { // read action 1
  // read a virtual file
});
// do other time-consuming work...
PsiFile psiFile = ReadAction.compute(() -> { // read action 2
  if (virtualFile.isValid()) { // check if the virtual file is valid
    return PsiManager.getInstance(project).findFile(virtualFile);
  }
  return null;
});
```

Between executing first and second read actions, another thread could invalidate the virtual file:

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
    section Thread 1
        read action 1       : 0, 1
        time-consuming work : done, 1, 4
        read action 2       : 4, 5
    section Thread 2
        delete virtual file : crit, 2, 3
```


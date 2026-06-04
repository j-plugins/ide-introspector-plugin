---
id: sdk.run-configurations.refactoring-support
title: Run Configurations: Refactoring Support
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, refactoring, support]
---
Some run configurations contain references to classes, files, or directories in their settings, and these settings usually need to be updated when the corresponding element is renamed or moved.
To support that, a run configuration needs to implement the [RefactoringListenerProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/RefactoringListenerProvider.java) interface.

The `RefactoringListenerProvider.getRefactoringElementListener()`'s implementation should check whether the refactored element is referred from the run configuration.
If it is, return a [RefactoringElementListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/refactoring/listeners/RefactoringElementListener.java) that updates the run configuration according to the new name and location of the element.


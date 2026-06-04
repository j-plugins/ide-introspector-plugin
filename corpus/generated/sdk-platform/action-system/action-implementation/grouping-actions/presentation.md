---
id: sdk.action-system.action-implementation.grouping-actions.presentation
title: Action System: Presentation
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, presentation]
---
A new [Presentation](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/Presentation.java) gets created for every place where the action appears.
Therefore, the same action can have a different text or icon when it appears in different places of the user interface.
Different presentations for the action are created by copying the Presentation returned by the `AnAction.getTemplatePresentation()` method.


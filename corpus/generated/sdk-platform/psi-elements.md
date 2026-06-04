---
id: sdk.psi-elements
title: PSI Elements
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, psi, elements]
---
A PSI (Program Structure Interface) file represents a hierarchy of PSI elements (so-called PSI trees).
A single [PSI file](https://plugins.jetbrains.com/docs/intellij/psi-files.html) (itself being a PSI element) may expose several PSI trees in specific programming languages (see [File View Providers](https://plugins.jetbrains.com/docs/intellij/file-view-providers.html)).
A PSI element, in its turn, can have child PSI elements.

PSI elements and operations at the level of individual PSI elements are used to explore the source code's internal structure as it is interpreted by the IntelliJ Platform.
For example, you can use PSI elements to perform code analysis, such as [code inspections](https://www.jetbrains.com/help/idea/code-inspection.html) or [intention actions](https://www.jetbrains.com/idea/help/intention-actions.html).

The [PsiElement](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiElement.java) class is the common base class for PSI elements.

Tip:

PSI classes for specific languages usually start with a language prefix, for example, [JsonArray](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/json/gen/com/intellij/json/psi/JsonArray.java).

The Java PSI API, developed many years ago when there was no plan to support other languages, uses the `Psi` prefix, for example, [PsiIdentifier](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/PsiIdentifier.java) or [PsiElementFactory](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/PsiElementFactory.java).
Don't confuse Java with the core PSI API.

To use Java PSI API, [add a dependency](https://plugins.jetbrains.com/docs/intellij/plugin-dependencies.html) on the Java plugin.

## How do I get a PSI element?

| Context |API |
----------------
| [Action](https://plugins.jetbrains.com/docs/intellij/action-system.html) |[AnActionEvent.getData(CommonDataKeys.PSI_ELEMENT)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnActionEvent.java)  Note: If an editor is currently open and the element under caret is a [reference](https://plugins.jetbrains.com/docs/intellij/psi-references.html), this will return the result of resolving the reference.   |
| [PSI File](https://plugins.jetbrains.com/docs/intellij/psi-files.html) |[PsiFile.findElementAt(offset)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiFile.java) - This returns a leaf element at the specified offset, normally a lexer token. Use `PsiTreeUtil.getParentOfType()` to find the element of the exact type.  [PsiRecursiveElementWalkingVisitor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiRecursiveElementWalkingVisitor.java)   |
| [Reference](https://plugins.jetbrains.com/docs/intellij/psi-references.html) |[PsiReference.resolve()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiReference.java) |

## What can I do with PSI elements?

See [PSI Cookbook](https://plugins.jetbrains.com/docs/intellij/psi-cookbook.html) and [Modifying the PSI](https://plugins.jetbrains.com/docs/intellij/modifying-psi.html).

> Source: IntelliJ Platform SDK docs — PSI Elements (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

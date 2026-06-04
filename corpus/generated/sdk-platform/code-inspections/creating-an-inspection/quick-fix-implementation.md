---
id: sdk.code-inspections.creating-an-inspection.quick-fix-implementation
title: Code Inspections: Quick Fix Implementation
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, quick, fix, implementation]
---
The quick fix class acts much like an intention, allowing the user to invoke it on the `PsiElement` (or `TextRange`) highlighted by the inspection.

The `ComparingStringReferencesInspection` implementation uses the nested class `ReplaceWithEqualsQuickFix` to implement a quick fix based on [LocalQuickFix](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/LocalQuickFix.java).
The `ReplaceWithEqualsQuickFix` class allows the user to change the use of `a == b` and `a != b` expression to `a.equals(b)` and `!a.equals(b)` respectively.

The heavy lifting is done in `ReplaceWithEqualsQuickFix.applyFix()`, which manipulates the PSI tree to convert the expressions.
The change to the PSI tree is accomplished by the usual approach to modification:

* Getting a `PsiElementFactory`.

* Creating a new `PsiMethodCallExpression`.

* Substituting the original left and right operands into the new `PsiMethodCallExpression`.

* Replacing the original binary expression with the `PsiMethodCallExpression`.

Note:

In case of providing multiple quick fixes for a single element, their ordering is indeterministic due to performance reasons.
It is possible to push specific items up or down by implementing
[HighPriorityAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInsight/intention/HighPriorityAction.java)
or
[LowPriorityAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInsight/intention/LowPriorityAction.java)
respectively.


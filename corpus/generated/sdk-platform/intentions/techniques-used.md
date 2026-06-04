---
id: sdk.intentions.techniques-used
title: Intentions: Techniques Used
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, techniques, used]
---
The [conditional_operator_intention](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/conditional_operator_intention) sample plugin illustrates the use of the following techniques:

* How to analyze a [PSI tree](https://plugins.jetbrains.com/docs/intellij/psi-files.html).

* How to find a Java token of interest in the PSI tree.

* How to invoke a quick-fix action for a token element under the cursor using the [PsiElementBaseIntentionAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-api/src/com/intellij/codeInsight/intention/PsiElementBaseIntentionAction.java) class.

* How to create a JUnit test for this plugin using the [IdeaTestFixtureFactory](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/testFramework/src/com/intellij/testFramework/fixtures/IdeaTestFixtureFactory.java) class.

* How to add an intention description and before/after examples in the Settings dialog

Note:

In the case of providing multiple intention actions for a single element, their ordering is indeterministic due to performance reasons.
It is possible to push specific items up or down by implementing
[HighPriorityAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInsight/intention/HighPriorityAction.java)
or
[LowPriorityAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInsight/intention/LowPriorityAction.java)
respectively.


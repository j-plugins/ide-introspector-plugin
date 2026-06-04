# Intentions

<tldr>
Product Help: [Intention Actions](https://www.jetbrains.com/help/idea/intention-actions.html)

UI Guidelines: [Inspections](https://plugins.jetbrains.com/docs/intellij/inspections.html)
</tldr>

This topic describes the [conditional_operator_intention](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/conditional_operator_intention), a sample plugin that adds a new [intention action](https://www.jetbrains.com/help/idea/intention-actions.html) to the IDE Intentions list.
In addition, the sample plugin contains a JUnit-based test.

## About Intention Actions

The IntelliJ Platform analyzes your code and helps handle situations that may result in errors.
When a possible problem is suspected, the IDE suggests an appropriate intention action, denoted with special icons.

See the [Inspections](https://plugins.jetbrains.com/docs/intellij/inspections.html) topic in UI Guidelines on naming, writing description, and message texts for inspections/intentions.

You can view a list of all available intention actions as well as enable/disable them using the [Intentions List](https://www.jetbrains.com/help/idea/intention-actions.html#intention-settings) in `Settings | Editor | Intentions`.

See the [Intention Action Preview](https://plugins.jetbrains.com/docs/intellij/code-intentions-preview.html) topic on providing a preview of changes that can be made by executing an intention.

## Techniques Used

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

## Sample Plugin (sdk.intentions.sample-plugin)

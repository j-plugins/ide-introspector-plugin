---
id: sdk.modifying-the-psi
title: Modifying the PSI
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, modifying, psi]
---
The PSI is a read/write representation of the source code as a tree of elements corresponding to a source file's structure.
The PSI can be modified by adding, replacing, and deleting PSI elements.

To perform these operations, use methods such as `PsiElement.add()`, `PsiElement.delete()`, `PsiElement.replace()`, and similar methods allowing to process multiple elements in a single operation, or to specify the exact location in the tree where an element needs to be added.

Like document operations, PSI modifications need to be wrapped in a write action and in command (and can only be performed in the event dispatch thread).
See [the Documents article](https://plugins.jetbrains.com/docs/intellij/documents.html#what-are-the-rules-of-working-with-documents) for more information on commands and write actions.

## Creating the New PSI

The PSI elements to add to the tree or replace existing PSI elements are usually created from text.
In most cases, the flow is:

1. Use [PsiFileFactory.createFileFromText()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiFileFactory.java) to create a new file that contains the code construct that needs to be added to the tree or used as a replacement for an existing element. See also [How do I create a PSI file?](https://plugins.jetbrains.com/docs/intellij/psi-files.html#how-do-i-create-a-psi-file).

2. Traverse the resulting tree to locate the required element and then pass it to `add()` or `replace()`.

Most languages provide factory methods to create specific code constructs more easily, for example:

* [PsiJavaParserFacade](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/PsiJavaParserFacade.java) class contains methods such as `createMethodFromText()`, which creates a Java method from the given text

* [SimpleElementFactory.createProperty()](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/simple_language_plugin/src/main/java/org/intellij/sdk/language/psi/SimpleElementFactory.java) creating a Simple language property

When implementing refactorings, [intentions](https://plugins.jetbrains.com/docs/intellij/code-intentions.html), or inspection [quickfixes](https://plugins.jetbrains.com/docs/intellij/code-inspections-and-intentions.html) that work with existing code, the text passed to the various `createFromText()` methods will combine hard-coded fragments and fragments of code taken from the existing file.
For small code fragments (individual identifiers), append the text from the existing code to the text of the code fragment being built.
In that case, make sure that the resulting text is syntactically correct.
Otherwise, the `createFromText()` method will throw an exception.

For larger code fragments, it's best to perform the modification in several steps:

* create a replacement tree fragment from the text, leaving placeholders for the user code fragments;

* replace the placeholders with the user code fragments;

* replace the element in the original source file with the replacement tree.

This ensures that the user code's formatting is preserved and that the modification does not introduce any unwanted whitespace changes.
Just as everywhere else in the IntelliJ Platform API, the text passed to `createFileFromText()` and other `createFromText()` methods must use only `\n` as line separators.

As an example of this approach, see the quickfix in the `ComparingStringReferencesInspection` [example](https://plugins.jetbrains.com/docs/intellij/code-inspections.html):

```JAVA
public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
  // binaryExpression holds a PSI expression of the form "x == y",
  // which needs to be replaced with "x.equals(y)"
  PsiBinaryExpression binaryExpression = (PsiBinaryExpression) descriptor.getPsiElement();
  IElementType opSign = binaryExpression.getOperationTokenType();
  PsiExpression lExpr = binaryExpression.getLOperand();
  PsiExpression rExpr = binaryExpression.getROperand();
  if (rExpr == null) {
    return;
  }
  // Step 1: Create a replacement fragment from text, with "a" and "b" as placeholders
  PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
  PsiMethodCallExpression equalsCall =
      (PsiMethodCallExpression) factory.createExpressionFromText("a.equals(b)", null);
  // Step 2: Replace "a" and "b" with elements from the original file
  PsiExpression qualifierExpression =
      equalsCall.getMethodExpression().getQualifierExpression();
  assert qualifierExpression != null;
  qualifierExpression.replace(lExpr);
  equalsCall.getArgumentList().getExpressions()[0].replace(rExpr);
  // Step 3: Replace a larger element in the original file with the replacement tree
  PsiExpression result = (PsiExpression) binaryExpression.replace(equalsCall);

  // Steps 4-6 needed only for negation
  if (opSign == JavaTokenType.NE) {
    // Step 4: Create a replacement fragment with negation and negated operand placeholder
    PsiPrefixExpression negation =
        (PsiPrefixExpression) factory.createExpressionFromText("!a", null);
    PsiExpression operand = negation.getOperand();
    assert operand != null;
    // Step 5: Replace operand placeholder with the actual expression
    operand.replace(result);
    // Step 6: Replace the result with the negated expression
    result.replace(negation);
  }
}
```

## Maintaining Tree Structure Consistency

The PSI modification methods do not restrict the way of building the resulting tree structure.

For example, when working with a Java class, it is possible to add a `for` statement as a direct child of a `PsiMethod` element, even though the Java parser will never produce such a structure (the `for` statement will always be a child of the `PsiCodeBlock`) representing the method body.

Modifications that produce incorrect tree structures may appear to work, but they will lead to problems and exceptions later.
Therefore, always ensure that the structure built with PSI modification operations is the same as what the parser would produce when parsing the created code.

To make sure inconsistencies are not introduced, use `PsiTestUtil.checkFileStructure()` in the tests for actions modifying the PSI.
This method ensures that the built structure is the same as what the parser produces.

## Whitespaces and Imports

When working with PSI modification functions, do not create individual whitespace nodes (spaces or line breaks) from text.
Instead, all whitespace modifications are performed by the formatter, which follows the code style settings selected by the user.
Formatting is automatically performed at the end of every command and can be also performed manually with [CodeStyleManager.reformat(PsiElement)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/codeStyle/CodeStyleManager.java) if needed.

Also, when working with Java code (or with code in other languages with a similar import mechanism such as Groovy or Python), do not create imports manually.
Instead, use fully qualified names in generated code and then call [JavaCodeStyleManager.shortenClassReferences()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/codeStyle/JavaCodeStyleManager.java) (or the equivalent API for the code language).
This ensures that the imports are created according to the user's code style settings and inserted into the file's correct place.

## Combining PSI and Document Modifications

In some cases, after modifying a PSI, it is required to perform an operation on the modified document (for example, start a [live template](https://plugins.jetbrains.com/docs/intellij/live-templates.html)).
To complete the PSI-based post-processing (such as formatting) and commit the changes to the document, call [PsiDocumentManager.doPostponedOperationsAndUnblockDocument()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiDocumentManager.java).

> Source: IntelliJ Platform SDK docs — Modifying the PSI (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

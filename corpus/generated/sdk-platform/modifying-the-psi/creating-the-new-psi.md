# Creating the New PSI

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

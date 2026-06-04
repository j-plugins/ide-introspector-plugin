# Modifying the PSI

The PSI is a read/write representation of the source code as a tree of elements corresponding to a source file's structure.
The PSI can be modified by adding, replacing, and deleting PSI elements.

To perform these operations, use methods such as `PsiElement.add()`, `PsiElement.delete()`, `PsiElement.replace()`, and similar methods allowing to process multiple elements in a single operation, or to specify the exact location in the tree where an element needs to be added.

Like document operations, PSI modifications need to be wrapped in a write action and in command (and can only be performed in the event dispatch thread).
See [the Documents article](https://plugins.jetbrains.com/docs/intellij/documents.html#what-are-the-rules-of-working-with-documents) for more information on commands and write actions.

## Creating the New PSI (sdk.modifying-the-psi.creating-the-new-psi)
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

# PSI Files

A PSI (Program Structure Interface) file is the root of a structure representing a file's contents as a hierarchy of elements in a particular programming language.

The [PsiFile](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiFile.java) class is the common base class for all PSI files, while files in a specific language are usually represented by its subclasses.  For example, the [PsiJavaFile](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/PsiJavaFile.java) class represents a Java file, and the [XmlFile](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/xml/xml-psi-api/src/com/intellij/psi/xml/XmlFile.java) class represents an XML file.

Unlike [Virtual Files](https://plugins.jetbrains.com/docs/intellij/virtual-file.html) and [Documents](https://plugins.jetbrains.com/docs/intellij/documents.html), which have application scope (even if multiple projects are open, each file is represented by the same
[VirtualFile](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VirtualFile.java) instance), PSI has [Project](https://plugins.jetbrains.com/docs/intellij/project.html) scope:
the same file is represented by multiple `PsiFile` instances if the file belongs to multiple projects open at the same time.

## How do I get a PSI file?

| Context |API |
----------------
| [Action](https://plugins.jetbrains.com/docs/intellij/action-system.html) |[AnActionEvent.getData(CommonDataKeys.PSI_FILE)](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/AnActionEvent.java) |
| [Document](https://plugins.jetbrains.com/docs/intellij/documents.html) |[PsiDocumentManager.getPsiFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiDocumentManager.java) |
| [PSI Element](https://plugins.jetbrains.com/docs/intellij/psi-elements.html) |[PsiElement.getContainingFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiElement.java) (may return `null` if the PSI element is not contained in a file) |
| [Virtual File](https://plugins.jetbrains.com/docs/intellij/virtual-file.html) |[PsiManager.findFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiManager.java), [PsiUtilCore.toPsiFiles()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/util/PsiUtilCore.java) |
| File Name |[FilenameIndex.getVirtualFilesByName()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/indexing-api/src/com/intellij/psi/search/FilenameIndex.java) and locate via [PsiManager.findFile()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiManager.java) or [PsiUtilCore.toPsiFiles()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/util/PsiUtilCore.java) |

## What can I do with a PSI file?

Most interesting modification operations are performed on the level of individual PSI elements, not files as a whole.

To iterate over the elements in a file, use

```JAVA
psiFile.accept(new PsiRecursiveElementWalkingVisitor() {
  // visitor implementation ...
});
```

See also [Navigating the PSI](https://plugins.jetbrains.com/docs/intellij/navigating-psi.html).

## Where does a PSI file come from?

As PSI is language-dependent, PSI files are created using the [Language](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/lang/Language.java) instance:

```JAVA
LanguageParserDefinitions.INSTANCE
    .forLanguage(MyLanguage.INSTANCE)
    .createFile(fileViewProvider);
```

Like [Documents](https://plugins.jetbrains.com/docs/intellij/documents.html), PSI files are created on-demand when the PSI is accessed for a particular file.

## How long do PSI files persist?

Like [Documents](https://plugins.jetbrains.com/docs/intellij/documents.html), PSI files are weakly referenced from the corresponding `VirtualFile` instances and can be garbage-collected if not referenced by anyone.

## How do I create a PSI file?

[PsiFileFactory.createFileFromText()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiFileFactory.java) creates an in-memory PSI file with the specified contents.

To save the PSI file to disk, use its parent directory's [PsiDirectory.add()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiDirectory.java).

## How do I get notified when PSI files change?

`PsiManager.addPsiTreeChangeListener()` allows you to receive notifications about all changes to the PSI tree of a project.
Alternatively, register [PsiTreeChangeListener](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiTreeChangeListener.java)
in [com.intellij.psi.treeChangeListener](https://jb.gg/ipe?extensions=com.intellij.psi.treeChangeListener) extension point
.

Note:

Please see [PsiTreeChangeEvent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiTreeChangeEvent.java) Javadoc for common problems when dealing with PSI events.

## How do I extend PSI?

PSI can be extended to support additional languages through custom language plugins.
For more details on developing custom language plugins, see the [Custom Language Support](https://plugins.jetbrains.com/docs/intellij/custom-language-support.html) reference guide.

## What are the rules for working with PSI?

Any changes done to the content of PSI files are reflected in documents, so all [rules for working with documents](https://plugins.jetbrains.com/docs/intellij/documents.html#what-are-the-rules-of-working-with-documents) (read/write actions, commands, read-only status handling) are in effect.

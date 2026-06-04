---
id: sdk.psi-files
title: PSI Files
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, psi, files]
---
A PSI (Program Structure Interface) file is the root of a structure representing a file's contents as a hierarchy of elements in a particular programming language.

The [PsiFile](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/PsiFile.java) class is the common base class for all PSI files, while files in a specific language are usually represented by its subclasses.  For example, the [PsiJavaFile](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/PsiJavaFile.java) class represents a Java file, and the [XmlFile](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/xml/xml-psi-api/src/com/intellij/psi/xml/XmlFile.java) class represents an XML file.

Unlike [Virtual Files](https://plugins.jetbrains.com/docs/intellij/virtual-file.html) and [Documents](https://plugins.jetbrains.com/docs/intellij/documents.html), which have application scope (even if multiple projects are open, each file is represented by the same
[VirtualFile](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/vfs/VirtualFile.java) instance), PSI has [Project](https://plugins.jetbrains.com/docs/intellij/project.html) scope:
the same file is represented by multiple `PsiFile` instances if the file belongs to multiple projects open at the same time.

## How do I get a PSI file? (psi-files/how-do-i-get-a-psi-file.md)
## What can I do with a PSI file? (psi-files/what-can-i-do-with-a-psi-file.md)
## Where does a PSI file come from? (psi-files/where-does-a-psi-file-come-from.md)
## How long do PSI files persist? (psi-files/how-long-do-psi-files-persist.md)
## How do I create a PSI file? (psi-files/how-do-i-create-a-psi-file.md)
## How do I get notified when PSI files change? (psi-files/how-do-i-get-notified-when-psi-files-change.md)
## How do I extend PSI? (psi-files/how-do-i-extend-psi.md)
## What are the rules for working with PSI? (psi-files/what-are-the-rules-for-working-with-psi.md)

> Source: IntelliJ Platform SDK docs — PSI Files (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

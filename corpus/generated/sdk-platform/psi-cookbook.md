---
id: sdk.psi-cookbook
title: PSI Cookbook
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, psi, cookbook]
---
This page gives recipes for the most common operations for working with the PSI (Program Structure Interface).

Unlike [Developing Custom Language Plugins](https://plugins.jetbrains.com/docs/intellij/custom-language-support.html), it is about working with the PSI of existing languages (such as Java).

Tip:

See also the [PSI Performance](https://plugins.jetbrains.com/docs/intellij/psi-performance.html) section.

## General

### How do I find a file if I know its name but don't know the path?

[FilenameIndex.getFilesByName()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/indexing-api/src/com/intellij/psi/search/FilenameIndex.java)

### How do I find where a particular PSI element is used?

[ReferencesSearch.search()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/indexing-api/src/com/intellij/psi/search/searches/ReferencesSearch.java)

### How do I rename a PSI element?

[RefactoringFactory.createRename()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/lang-api/src/com/intellij/refactoring/RefactoringFactory.java)

### How can I cause the PSI for a virtual file to be rebuilt?

[FileContentUtil.reparseFiles()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/util/FileContentUtil.java)

## Java Specific

Note:

If your plugin depends on Java functionality, see [Java](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#java).
Also consider using [UAST](https://plugins.jetbrains.com/docs/intellij/uast.html) if your plugin supports other JVM languages.

### How do I find all inheritors of a class?

[ClassInheritorsSearch.search()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-indexing-api/src/com/intellij/psi/search/searches/ClassInheritorsSearch.java)

### How do I find a class by qualified name?

[JavaPsiFacade.findClass()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/JavaPsiFacade.java)

### How do I find a class by short name?

[PsiShortNamesCache.getClassesByName()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-indexing-api/src/com/intellij/psi/search/PsiShortNamesCache.java)

### How do I find a superclass of a Java class?

[PsiClass.getSuperClass()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/PsiClass.java)

### How do I get a reference to the containing package of a Java class?

[PsiUtil.getPackageName()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/util/PsiUtil.java)

### How do I find the methods overriding a specific method?

[OverridingMethodsSearch.search()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-indexing-api/src/com/intellij/psi/search/searches/OverridingMethodsSearch.java)

### How do I create a new class/interface/enum/record in a given directory?

[JavaDirectoryService](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/JavaDirectoryService.java)

### How can I locate specific Java PSI elements within a class/method?

Extend [JavaElementVisitor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/JavaElementVisitor.java)
or [JavaRecursiveElementVisitor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/JavaRecursiveElementVisitor.java) and override relevant method(s).
Pass it to `PsiElement.accept()` of the parent PSI element (for example, [PsiClass](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/PsiClass.java)
or [PsiMethod](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-psi-api/src/com/intellij/psi/PsiMethod.java)).

### How do I check the presence of a JVM library?

Use dedicated (and heavily cached) methods from [JavaLibraryUtil](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/openapi/src/com/intellij/java/library/JavaLibraryUtil.java):

* `hasLibraryClass()` to check presence via known library class FQN

* `hasLibraryJar()` using Maven coordinates (for example, `io.micronaut:micronaut-core`).

> Source: IntelliJ Platform SDK docs — PSI Cookbook (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

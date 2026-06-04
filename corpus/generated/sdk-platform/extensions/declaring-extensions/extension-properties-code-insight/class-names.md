---
id: sdk.extensions.declaring-extensions.extension-properties-code-insight.class-names
title: Extensions: Class names
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, class, names]
---
Property names matching the following list will resolve to a fully qualified class name:

* `implementation`

* `className`

* ending with `Class` (case-sensitive)

* `serviceInterface`/`serviceImplementation`

A required parent type can be specified in the [extension point declaration](https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html) via [&lt;with&gt;](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__extensionPoints__extensionPoint__with):

```XML
<extensionPoint name="myExtension" beanClass="MyExtensionBean">
  <with
      attribute="psiElementClass"
      implements="com.intellij.psi.PsiElement"/>
</extensionPoint>
```


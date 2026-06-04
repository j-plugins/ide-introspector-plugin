---
id: sdk.code-inspections.creating-an-inspection.inspection-description.code-snippets
title: Code Inspections: Code Snippets
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, code, snippets]
---
Using the following HTML structure, the description can embed code snippets that will be displayed with syntax highlighting:

```HTML
<p>
  The following code will be shown with syntax highlighting:
</p>
<pre>
  <code>
    // code snippet
  </code>
</pre>
```

The language will be set according to the [inspection's registration](#plugin-configuration-file) `language` attribute.
If required (e.g., when targeting [UAST](https://plugins.jetbrains.com/docs/intellij/uast.html)), it can be specified explicitly via `<code lang="LanguageID">...</code>`.


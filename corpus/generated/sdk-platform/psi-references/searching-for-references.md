---
id: sdk.psi-references.searching-for-references
title: PSI References: Searching for References
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, searching, for, references]
---
Resolving a reference means going from usage to the corresponding declaration.
To perform the navigation in the opposite direction - from a declaration to its usages - perform a references search.

To perform a search using [ReferencesSearch](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/indexing-api/src/com/intellij/psi/search/searches/ReferencesSearch.java), specify the element to search for, and optionally other parameters such as the scope in which the reference needs to be searched.
The created [Query](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/util/Query.kt) allows obtaining all results at once or iterating over the results one by one.
The latter allows stopping processing as soon as the first (matching) result has been found.


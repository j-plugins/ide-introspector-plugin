---
id: sdk.indexing-and-psi-stubs.improving-indexing-performance.avoid-using-ast
title: Indexing and PSI Stubs: Avoid Using AST
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, avoid, using, ast]
---
Use [lexer](https://plugins.jetbrains.com/docs/intellij/implementing-lexer.html) information instead of parsed trees if possible.

If impossible, use light AST which doesn't create memory-hungry AST nodes inside, so traversing it might be faster.
Obtain [LighterAST](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/lang/LighterAST.java) by casting `FileContent` input parameter to [PsiDependentFileContent](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/util/indexing/PsiDependentFileContent.java) and calling `getLighterAST()`.
Make sure to traverse only the nodes you need to.
See also [RecursiveLighterASTNodeWalkingVisitor](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-impl/src/com/intellij/psi/impl/source/tree/RecursiveLighterASTNodeWalkingVisitor.java) and [LightTreeUtil](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-impl/src/com/intellij/psi/impl/source/tree/LightTreeUtil.java) for useful utility methods.

For [stub index](https://plugins.jetbrains.com/docs/intellij/stub-indexes.html), implement [LightStubBuilder](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-impl/src/com/intellij/psi/stubs/LightStubBuilder.java).

If a custom language contains lazy-parseable elements that never or rarely contain any stubs, consider implementing [StubBuilder.skipChildProcessingWhenBuildingStubs()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/psi/StubBuilder.java) (preferably using Lexer/node text).

For indexing XML, also consider using [NanoXmlUtil](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/indexing-impl/src/com/intellij/util/xml/NanoXmlUtil.java).


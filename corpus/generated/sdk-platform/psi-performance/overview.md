---
id: sdk.psi-performance.overview
title: PSI Performance: Overview
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, overview]
---
PSI has a lot of time-space compromises.
There are tons of PSI elements in IDE memory, so the IntelliJ Platform and language plugins strive to keep them as compact as possible, storing very little data inside.
As a result, many things are recomputed on every call of getter methods on `PsiElement` and its subclasses.
They are not stored inside the fields of `PsiElement`s.

For example, consider the following Java expression:

```JAVA
String.format("Hello, %s!", name);
```

This is its (simplified) PSI:

```
PsiMethodCallExpression:String.format("Hello, %s!", name)
  PsiReferenceExpression:String.format
  PsiExpressionList
    PsiJavaToken:LPARENTH
    PsiLiteralExpression:"Hello, %s!"
    PsiJavaToken:COMMA
    PsiReferenceExpression:name
    PsiJavaToken:RPARENTH
```

Assume that plugin code receives `PsiMethodCallExpression call` and needs to get the first and last expression passed as arguments to this method call.
This can be done with `methodCall.getArgumentList().getExpressions()`, which does the following:

1. `getArgumentList()` traverses the linked list of children, looking for an element of a proper type (in this case – `PsiExpressionList`)

2. `getExpressions()` traverses the children to find all the expressions (elements of `PsiExpression`), then allocates an array of the target size, and then traverses the children again to fill in this array.

These `get*()` methods are not simple getters that return a readily available value – keep this in mind when working with PSI.

As a rule, avoid calling the same method twice, one after another.
Instead, it's better to store the result in a local variable.

Find the first and last expression argument passed to a method call could be implemented as follows:

Inefficient:

```JAVA
PsiExpression first = call.getArgumentList().getExpressions()[0];
int lastIndex = call.getArgumentList().getExpressionCount() - 1;
PsiExpression last = call.getArgumentList().getExpressions()[lastIndex];
```

Optimized:

```JAVA
PsiExpression[] expressions = call.getArgumentList().getExpressions();
PsiExpression first = expressions[0];
PsiExpression last = expressions[expressions.length - 1];
```


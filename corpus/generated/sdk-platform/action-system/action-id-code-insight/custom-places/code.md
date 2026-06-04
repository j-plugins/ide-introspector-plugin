---
id: sdk.action-system.action-id-code-insight.custom-places.code
title: Action System: Code
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, code]
---
For string literal constants, parameters, and return values, use [@Language](https://github.com/JetBrains/java-annotations/tree/24.0.0/common/src/main/java/org/intellij/lang/annotations/Language.java)
annotation with `devkit-action-id`.

```JAVA
public abstract class MyPluginTestCase
    extends LightPlatformCodeInsightTestCase {

  protected void doTestInvokingSomeAction(
      @Language("devkit-action-id") @NonNls final String actionId
      /* more parameters */) {
  }

}
```


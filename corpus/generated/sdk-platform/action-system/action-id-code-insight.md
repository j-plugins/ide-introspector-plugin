---
id: sdk.action-system.action-id-code-insight
title: Action System: Action ID Code Insight
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, action, code, insight]
---
Part of `sdk.action-system`.

Action ID Code Insight

Code insight to defined Actions and Groups is provided by the Plugin DevKit plugin.

### Builtin Places

* IntelliJ Platform API, for example [ActionManager.getAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/editor-ui-api/src/com/intellij/openapi/actionSystem/ActionManager.java)

* Test Framework API, for example [CodeInsightTestFixture.performEditorAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/testFramework/src/com/intellij/testFramework/fixtures/CodeInsightTestFixture.java)

* String literal fields with the name `ACTION_ID`

* Constants defined in [IdeActions](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/actionSystem/IdeActions.java)

### Custom Places

Additional places can be configured to provide Action ID reference using the bundled IntelliLang plugin.
Common use cases include plugin-specific test utility code or configuration files.

#### Code

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

#### Other Places

To setup Action ID references in other places (for example, XML files) perform the following steps:

Procedure: Injecting in other places

> Source: IntelliJ Platform SDK docs — Action System: Action ID Code Insight (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

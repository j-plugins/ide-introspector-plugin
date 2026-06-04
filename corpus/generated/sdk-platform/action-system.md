# Action System

<tldr>
Product Help: [Menus and toolbars](https://www.jetbrains.com/help/idea/customize-actions-menus-and-toolbars.html)

UI Guidelines: [Toolbar](https://plugins.jetbrains.com/docs/intellij/toolbar.html)
</tldr>

The Action System allows plugins to add their items to IntelliJ Platform-based IDE menus and toolbars.
For example, one of the action classes is responsible for the `File | Open File...` menu item and the Open... toolbar button.

Actions in the IntelliJ Platform require a [code implementation](#action-implementation) and must be [registered](#registering-actions).
The action implementation determines the contexts in which an action is available and its functionality when selected in the UI.
Registration determines where an action appears in the IDE UI.
Once implemented and registered, an action receives callbacks from the IntelliJ Platform in response to user gestures.

The [Creating Actions](https://plugins.jetbrains.com/docs/intellij/creating-actions-tutorial.html) tutorial describes the process of adding a custom action to a plugin.
The [Grouping Actions](https://plugins.jetbrains.com/docs/intellij/grouping-actions-tutorial.html) tutorial demonstrates three types of groups that can contain actions.

## Action Implementation (sdk.action-system.action-implementation)
## Registering Actions (sdk.action-system.registering-actions)
## Building a Toolbar/Popup Menu from Actions (sdk.action-system.building-a-toolbar-popup-menu-from-actions)
## Useful Action Base Classes (sdk.action-system.useful-action-base-classes)
## Executing Actions Programmatically

Sometimes, it is required to execute actions programmatically, for example, executing an action implementing logic needed in another place, and the implementation is out of our control.
Executing actions can be achieved with [ActionUtils.invokeAction()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/platform-api/src/com/intellij/openapi/actionSystem/ex/ActionUtil.kt).

Warning:

Executing actions programmatically should be avoided whenever possible.
If an action executed programmatically is under your control, extract its logic to a [service](https://plugins.jetbrains.com/docs/intellij/plugin-services.html) or utility class and call it directly, without the action execution context.

## Action ID Code Insight

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


> Source: IntelliJ Platform SDK docs — Action System (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

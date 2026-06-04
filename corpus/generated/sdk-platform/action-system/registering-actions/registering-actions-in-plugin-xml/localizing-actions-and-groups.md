---
id: sdk.action-system.registering-actions.registering-actions-in-plugin-xml.localizing-actions-and-groups
title: Action System: Localizing Actions and Groups
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, localizing, actions, groups]
---
Part of `sdk.action-system.registering-actions.registering-actions-in-plugin-xml`.

Tip:

Hard-coding presentation in the `AnAction` constructor is discouraged, use inspection Plugin DevKit | Code | Eager creation of action presentation (2023.3) to highlight such problems.

See [Extending DefaultActionGroup](https://plugins.jetbrains.com/docs/intellij/grouping-actions-tutorial.html#extending-defaultactiongroup) for a tutorial of localizing Actions and Groups.

Action and group localization use [resource bundles](https://plugins.jetbrains.com/docs/intellij/internationalization.html#message-bundles) containing property files named `$NAME$Bundle.properties`, each file consisting of `key=value` pairs.
The [action_basics](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/action_basics) plugin demonstrates using a resource bundle to localize the group and action entries added to the Editor Popup Menu.

When localizing actions and groups, the `text` and `description` attributes are not declared in `plugin.xml`.
Instead, those attribute values vary depending on the locale and get declared in a resource bundle.

The name and location of the resource bundle must be declared in the `plugin.xml` file.
In the case of `action_basics`, only a default localization resource bundle (`/resources/messages/BasicActionsBundle.properties`) is provided:

```XML
<resource-bundle>messages.BasicActionsBundle</resource-bundle>
```

##### Dedicated Resource Bundle

If necessary, a dedicated resource bundle to use for actions and groups can be defined on [&lt;actions&gt;](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__actions):

```XML
<actions resource-bundle="messages.MyActionsBundle">
  <!-- action/group defined here will use keys
  from MyActionsBundle.properties -->
</actions>
```

Actions:

For Actions, the key in property files incorporates the action ID in this specific structure:

* `action.<action-id>.text=Translated Action Text`

* `action.<action-id>.description=Translated Action Description`

2020.1

If `<override-text>` is used for an action ID, the key includes the `place` attribute:

* `action.<action-id>.<place>.text=Place-dependent Translated Action Text`

Groups:

For Groups, the key in the property files incorporates the group ID in this specific structure:

* `group.<group-id>.text=Translated Group Text`

* `group.<group-id>.description=Translated Group Description`

2020.3

If `<override-text>` is used for a group ID, the key includes the `place` attribute:

* `group.<group-id>.<place>.text=Place-dependent Translated Group Text`

> Source: IntelliJ Platform SDK docs — Action System: Localizing Actions and Groups (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

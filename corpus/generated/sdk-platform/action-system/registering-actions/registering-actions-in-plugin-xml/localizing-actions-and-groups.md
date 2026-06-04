---
id: sdk.action-system.registering-actions.registering-actions-in-plugin-xml.localizing-actions-and-groups
title: Action System: Localizing Actions and Groups
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, localizing, actions, groups]
---
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

##### Dedicated Resource Bundle (action-system/registering-actions/registering-actions-in-plugin-xml/localizing-actions-and-groups/dedicated-resource-bundle.md)

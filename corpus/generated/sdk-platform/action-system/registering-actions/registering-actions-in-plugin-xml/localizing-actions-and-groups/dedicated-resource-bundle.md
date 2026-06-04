---
id: sdk.action-system.registering-actions.registering-actions-in-plugin-xml.localizing-actions-and-groups.dedicated-resource-bundle
title: Action System: Dedicated Resource Bundle
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, dedicated, resource, bundle]
---
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


---
id: sdk.plugin-configuration-file.idea-plugin.resource-bundle
title: Plugin Configuration File: resource-bundle
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, resource, bundle]
---
Part of `sdk.plugin-configuration-file.idea-plugin`.

`resource-bundle`







A resource bundle to be used with message key attributes in extension declarations and for
[action and group localization](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups).
A single [&lt;idea-plugin&gt;](#idea-plugin) element can contain multiple `<resource-bundle>` elements.



Required
: no


Example
: To load the content of `messages/Bundle.properties` bundle, declare:
: ```XML
<resource-bundle>messages.Bundle</resource-bundle>
```

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: resource-bundle (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

---
id: sdk.notification-balloons.localization
title: Notification Balloons: Localization
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, localization]
---
Part of `sdk.notification-balloons`.

The notification group identifier is not a technical identifier, but a human-readable string directly mapped to the IDE settings user interface.
However, it can be localized.

Tip:

See [Bundled Translations](https://plugins.jetbrains.com/docs/intellij/providing-translations.html#bundled-translations) for more information about directory layout and resource bundle formats.

Make sure that the plugin descriptor declares a [plugin resource bundle](https://plugins.jetbrains.com/docs/intellij/plugin-configuration-file.html#idea-plugin__resource-bundle):

```XML
<resource-bundle>messages.BagelBundle</resource-bundle>
```

Then declare a `key` element that contains a localized notification group ID.

```XML
<notificationGroup id="Bagel File"
  key="bagel.file.notification.group"
  displayType="STICKY_BALLOON" />
```

Warning:

The `id` attribute is mandatory, even if it is localized in the default resource bundle key.
Alternatively, provide a resource bundle name in the `bundle` attribute to override a resource bundle name from the `<resource-bundle>` element.

> Source: IntelliJ Platform SDK docs — Notification Balloons: Localization (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

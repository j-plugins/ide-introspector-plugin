---
id: sdk.code-inspections.creating-an-inspection.inspection-description.settings-link
title: Code Inspections: Settings Link
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, settings, link]
---
To open related [settings](https://plugins.jetbrains.com/docs/intellij/settings.html) directly from the inspection description, add a link with `settings://$CONFIGURABLE_ID$`, optionally followed by `?$SEARCH_STRING$` to pre-select UI element:

```HTML
See <em>Includes</em> tab in <a href="settings://fileTemplates">Settings | Editor | File and Code Templates</a> to configure.
```


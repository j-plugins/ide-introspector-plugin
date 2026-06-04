---
id: sdk.plugin-configuration-file.idea-plugin.application-components.component.skipfordefaultproject
title: Plugin Configuration File: skipForDefaultProject
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, skipfordefaultproject]
---
`skipForDefaultProject`







Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





In the past, if present, the component was not loaded for the default project.


Currently, project components aren't loaded in the default project by default, so this element has no effect.
Use [&lt;loadForDefaultProject&gt;](#idea-plugin__application-components__component__loadForDefaultProject)
if it is required to load a component in the default project.



Deprecated
: since 2020.1


Required
: no


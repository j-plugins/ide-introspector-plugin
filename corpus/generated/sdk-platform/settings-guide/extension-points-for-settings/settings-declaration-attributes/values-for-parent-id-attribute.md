---
id: sdk.settings-guide.extension-points-for-settings.settings-declaration-attributes.values-for-parent-id-attribute
title: Settings Guide: Values for Parent ID Attribute
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, values, for, parent, attribute]
---
Part of `sdk.settings-guide.extension-points-for-settings.settings-declaration-attributes`.

The table below shows all Settings groups and their corresponding value for the `parentId` attribute.
See the [previous section](#table-of-attributes) for all supported attributes.

| Group |`parentId` Value |Details |
------------------------------------
| Appearance & Behavior |`appearance` |This child group contains Settings to personalize IDE appearance, such as: changing themes and font size. Also, it covers Settings to customize behavior such as keymaps, configuring plugins, and system Settings such as password policies, HTTP proxy, updates, and more. |
| Build, Execution, Deployment |`build` |Child group containing Settings to configure project integration with different build tools, modify the default compiler Settings, manage server access configurations, customize the debugger behavior, etc. |
| Build Integration |`build.tools` |A subgroup of `build`. This subgroup configures project integration with build tools such as Maven, Gradle, or Gant. |
| Editor |`editor` |Child group containing Settings to personalize source code appearance, such as fonts, highlighting styles, indents, etc. It also contains Settings to customize the editor's appearance, such as line numbers, caret placement, tabs, source code inspections, setting up templates, and file encodings. |  |
| Languages and Frameworks |`language` |Child group containing Settings related to specific language frameworks and technologies used in the project. |
| 3rd Party Settings |`tools` |Child group containing Settings to configure integration with third-party applications, specify the SSH Terminal connection Settings, manage server certificates and tasks, configure diagrams layout, etc. |
| Super Parent |`root` |The invisible parent of all existing groups. Not used except for IDEs built on top of the IntelliJ Platform, or extensive suites of Settings. You should not place settings in this group. |
| `other`  Do not use   |default |If neither `parentId` nor `groupId` attribute is set, the component is added to the `other` Settings group. This is undesirable; see `other` group description. |
| Catch-all  Deprecated   |`other` |The IntelliJ Platform no longer uses this group. Do not use this group. Use the `tools` group instead. |
| Project-related Settings  Deprecated   |`project` |The IntelliJ Platform no longer uses this group. It was intended to store some project-related settings. Do not use this group. |

> Source: IntelliJ Platform SDK docs — Settings Guide: Values for Parent ID Attribute (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

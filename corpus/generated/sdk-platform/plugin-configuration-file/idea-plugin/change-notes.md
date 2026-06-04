---
id: sdk.plugin-configuration-file.idea-plugin.change-notes
title: Plugin Configuration File: change-notes
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, change, notes]
---
Part of `sdk.plugin-configuration-file.idea-plugin`.

`change-notes`

<tldr>
Reference: [JetBrains Marketplace: Change Notes](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html#change-notes)
</tldr>

A short summary of new features, bugfixes, and changes provided with the latest plugin version.
Change notes are displayed on the [JetBrains Marketplace](https://plugins.jetbrains.com) plugin page and in
the Plugins settings dialog.

Simple HTML elements, like text formatting, paragraphs, lists, etc., are allowed and must be wrapped into
`<![CDATA[` ... `]]>` section.

Required
: no; ignored in an [additional config file](#additional-plugin-configuration-files)

The element can be skipped in the source `plugin.xml` file if the Gradle plugin `patchPluginXml` task
([2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml))
is enabled and configured.

Example
: ```XML
<change-notes><![CDATA[
<h2>New Features</h2>
<ul>
<li>Feature 1</li>
<li>Feature 2</li>
</ul>
<h2>Bug Fixes</h2>
<ul>
<li>Fixed issue 1</li>
<li>Fixed issue 2</li>
</ul>
]]></change-notes>
```

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: change-notes (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

---
id: sdk.plugin-configuration-file.idea-plugin.description
title: Plugin Configuration File: description
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, description]
---
Part of `sdk.plugin-configuration-file.idea-plugin`.

`description`



<tldr>
Reference: [JetBrains Marketplace: Plugin Description](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html#plugin-description)
</tldr>





The plugin description displayed on the [JetBrains Marketplace](https://plugins.jetbrains.com) plugin page and in
the Plugins settings dialog.


Simple HTML elements, like text formatting, paragraphs, lists, etc., are allowed and must be wrapped into
`<![CDATA[` ... `]]>` section.



Required
: yes; ignored in an [additional config file](#additional-plugin-configuration-files)

The element can be skipped in the source `plugin.xml` file if the Gradle plugin `patchPluginXml` task
([2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml))
is enabled and configured.


Example
: ```XML
<description><![CDATA[
Provides support for My Framework.
The support includes:
<ul>
  <li>code completion</li>
  <li>references</li>
</ul>
For more information visit the
<a href="https://example.com">project site</a>.
]]></description>
```

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: description (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

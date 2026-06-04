---
id: sdk.plugin-configuration-file.idea-plugin.xi-include
title: Plugin Configuration File: xi:include
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, include]
---
Part of `sdk.plugin-configuration-file.idea-plugin`.

`xi:include`







Allows including content of another plugin descriptor in this descriptor with
[XInclude](http://www.w3.org/2001/XInclude) standard.



Namespace
: `xi="http://www.w3.org/2001/XInclude"`


Required
: no


Attributes
: * `href` (optional) Path of the plugin descriptor file to include.

  * `xpointer` (optional) Deprecated since 2021.2: The `xpointer` attribute must be `xpointer(/idea-plugin/*)` or not defined. Elements pointer to include. Default value: `xpointer(/idea-plugin/*)`.


Children
: * [&lt;xi:fallback&gt;](#idea-plugin__xi:include__xi:fallback)


Example
: Given a plugin descriptor:
: ```XML
<idea-plugin xmlns:xi="http://www.w3.org/2001/XInclude">
  <id>com.example.myplugin</id>
  <name>Example</name>
  <xi:include href="/META-INF/another-plugin.xml"/>
  ...
</idea-plugin>
```
: and `/META-INF/another-plugin.xml`:
: ```XML
<idea-plugin>
<extensions>...</extensions>
<actions>...</actions>
</idea-plugin>
```
: The effective plugin descriptor loaded to memory will contain the following elements:
: ```XML
<idea-plugin xmlns:xi="http://www.w3.org/2001/XInclude">
  <id>com.example.myplugin</id>
  <name>Example</name>
  <extensions>...</extensions>
  <actions>...</actions>
  ...
</idea-plugin>
```

#### 

`xi:fallback`

Indicates that including the specified file is optional.

If the file referenced in `href` is not found and the `xi:fallback`
element
is missing, the plugin will fail to load.

Namespace
: `xi="http://www.w3.org/2001/XInclude"`

Required
: no

Example
: ```XML
<idea-plugin xmlns:xi="http://www.w3.org/2001/XInclude">
...
<xi:include href="/META-INF/optional-plugin.xml">
<xi:fallback/>
</xi:include>
...
</idea-plugin>
```

> Source: IntelliJ Platform SDK docs — Plugin Configuration File: xi:include (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

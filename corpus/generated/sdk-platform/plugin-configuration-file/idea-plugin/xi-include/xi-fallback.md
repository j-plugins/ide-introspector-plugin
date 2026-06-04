---
id: sdk.plugin-configuration-file.idea-plugin.xi-include.xi-fallback
title: Plugin Configuration File: xi:fallback
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, fallback]
---
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


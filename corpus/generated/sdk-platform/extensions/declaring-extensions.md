---
id: sdk.extensions.declaring-extensions
title: Extensions: Declaring Extensions
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, declaring, extensions]
---
Tip:

Auto-completion, Quick Documentation, and other code insight features are available on extension point tags and attributes in `plugin.xml`.

Procedure: Declaring Extension

To clarify this procedure, consider the following sample section of the `plugin.xml` file that defines two extensions designed
to access the [com.intellij.appStarter](https://jb.gg/ipe?extensions=com.intellij.appStarter)
and [com.intellij.projectTemplatesFactory](https://jb.gg/ipe?extensions=com.intellij.projectTemplatesFactory)

extension points in the IntelliJ Platform,
and one extension to access the `another.plugin.myExtensionPoint` extension point in another plugin `another.plugin`:

```XML
<!--
  Declare extensions to access extension points in the IntelliJ Platform.
  These extension points have been declared using "interface".
 -->
<extensions defaultExtensionNs="com.intellij">
  <appStarter
      implementation="com.example.MyAppStarter"/>
  <projectTemplatesFactory
      implementation="com.example.MyProjectTemplatesFactory"/>
</extensions>

<!--
  Declare extensions to access extension points in a custom plugin "another.plugin".
  The "myExtensionPoint" extension point has been declared using "beanClass"
  and exposes custom properties "key" and "implementationClass".
-->
<extensions defaultExtensionNs="another.plugin">
  <myExtensionPoint
      key="keyValue"
      implementationClass="com.example.MyExtensionPointImpl"/>
</extensions>
```

Procedure: Implementing Extension

### Extension Properties Code Insight (extensions/declaring-extensions/extension-properties-code-insight.md)
#### Required Properties (extensions/declaring-extensions/extension-properties-code-insight/required-properties.md)
#### Class names (extensions/declaring-extensions/extension-properties-code-insight/class-names.md)
#### Custom resolve (extensions/declaring-extensions/extension-properties-code-insight/custom-resolve.md)
#### Deprecation/ApiStatus (extensions/declaring-extensions/extension-properties-code-insight/deprecation-apistatus.md)
#### Enum properties (extensions/declaring-extensions/extension-properties-code-insight/enum-properties.md)
#### I18n (extensions/declaring-extensions/extension-properties-code-insight/i18n.md)

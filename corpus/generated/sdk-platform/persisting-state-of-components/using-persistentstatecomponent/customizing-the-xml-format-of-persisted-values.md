---
id: sdk.persisting-state-of-components.using-persistentstatecomponent.customizing-the-xml-format-of-persisted-values
title: Persisting State of Components: Customizing the XML Format of Persisted Values
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, customizing, xml, format, persisted, values]
---
Note:

Consider using annotation parameters only to achieve backward compatibility.
Otherwise, feel free to file issues about specific serialization cosmetics.

If you want to use the default bean serialization but need to customize the storage format in XML (for example, for compatibility with previous versions of a plugin or externally defined XML formats), use the
[@Tag](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/Tag.java),
[@Attribute](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/Attribute.java),
[@Property](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/Property.java),
[@MapAnnotation](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/MapAnnotation.java),
[@XMap](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/XMap.java),
and [@XCollection](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/util/xmlb/annotations/XCollection.java)
annotations.

If the state to serialize doesn't map cleanly to a JavaBean, then `org.jdom.Element` can be used as the state class.
In that case, use the `getState()` method to build an XML element with an arbitrary structure, which then is saved directly in the state XML file.
In the `loadState()` method, deserialize the JDOM element tree using any custom logic.
This is not recommended and should be avoided whenever possible.

To disable the expansion of path macros ([PathMacro](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/macro/src/com/intellij/ide/macro/PathMacro.java))
in stored values, implement [PathMacroFilter](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/jps/model-serialization/src/com/intellij/openapi/application/PathMacroFilter.java)
and register in [com.intellij.pathMacroFilter](https://jb.gg/ipe?extensions=com.intellij.pathMacroFilter) extension point
.


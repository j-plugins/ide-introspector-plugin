---
id: sdk.extension-points.example
title: Extension Points: Example
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, example]
---
Part of `sdk.extension-points`.

Consider example extension points declarations:

`myPlugin/META-INF/plugin.xml`

```XML
<idea-plugin>
  <id>my.plugin</id>

  <extensionPoints>
    <extensionPoint
        name="myExtensionPoint1"
        interface="com.example.MyInterface"/>

    <extensionPoint
        name="myExtensionPoint2"
        beanClass="com.example.MyBeanClass"/>
  </extensionPoints>

</idea-plugin>
```

The `com.example.MyBeanClass` bean class used in the above `plugin.xml` file is implemented as follows:

`myPlugin/src/com/myplugin/MyBeanClass.java`

```JAVA
public final class MyBeanClass extends AbstractExtensionPointBean {

  @Attribute("key")
  public String key;

  @Attribute("implementationClass")
  public String implementationClass;

  public String getKey() {
    return key;
  }

  public String getClass() {
    return implementationClass;
  }

}
```

Tip:

See [Extension properties code insight](https://plugins.jetbrains.com/docs/intellij/plugin-extensions.html#extension-properties-code-insight) on how to provide smart completion/validation.

For the above extension points, their usage in anotherPlugin would look like this (see also [Declaring Extensions](https://plugins.jetbrains.com/docs/intellij/plugin-extensions.html#declaring-extensions)):

`anotherPlugin/META-INF/plugin.xml`

```XML
<idea-plugin>
  <id>another.plugin</id>

  <!-- Declare dependency on plugin defining extension point: -->
  <depends>my.plugin</depends>

  <!-- Use "my.plugin" namespace: -->
  <extensions defaultExtensionNs="my.plugin">
    <myExtensionPoint1
            key="someKey"
            implementationClass="com.example.MyImplementation"/>

    <myExtensionPoint2
            implementation="another.MyInterfaceImpl"/>
  </extension>

</idea-plugin>
```

> Source: IntelliJ Platform SDK docs — Extension Points: Example (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

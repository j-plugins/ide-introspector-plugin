# idea-plugin

`idea-plugin`

The `plugin.xml` file root element.

Required
: yes

Attributes
: * `url` (optional; ignored in an [additional config file](#additional-plugin-configuration-files)) The link to the plugin homepage displayed on the plugin page in the [JetBrains Marketplace](https://plugins.jetbrains.com).

* `require-restart` (optional) The boolean value determining whether the plugin installation, update, or uninstallation requires an IDE restart (see [Dynamic Plugins](https://plugins.jetbrains.com/docs/intellij/dynamic-plugins.html) for details). Default value: `false`.

Children
: * [&lt;actions&gt;](#idea-plugin__actions)

* [&lt;applicationListeners&gt;](#idea-plugin__applicationListeners)

* [&lt;change-notes&gt;](#idea-plugin__change-notes)

* [&lt;depends&gt;](#idea-plugin__depends)

* [&lt;description&gt;](#idea-plugin__description)

* [&lt;extensionPoints&gt;](#idea-plugin__extensionPoints)

* [&lt;extensions&gt;](#idea-plugin__extensions)

* [&lt;id&gt;](#idea-plugin__id)

* [&lt;idea-version&gt;](#idea-plugin__idea-version)

* [&lt;incompatible-with&gt;](#idea-plugin__incompatible-with)

* [&lt;name&gt;](#idea-plugin__name)

* [&lt;product-descriptor&gt;](#idea-plugin__product-descriptor)

* [&lt;projectListeners&gt;](#idea-plugin__projectListeners)

* [&lt;resource-bundle&gt;](#idea-plugin__resource-bundle)

* [&lt;vendor&gt;](#idea-plugin__vendor)

* [&lt;version&gt;](#idea-plugin__version)

* [&lt;xi:include&gt;](#idea-plugin__xi:include)

* [&lt;application-components&gt;](#idea-plugin__application-components) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

* [&lt;module-components&gt;](#idea-plugin__module-components) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

* [&lt;project-components&gt;](#idea-plugin__project-components) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

### id

`id`

A unique identifier of the plugin.
It should be a fully qualified name similar to Java packages and must not collide with the ID of existing plugins.
The ID is a technical value used to identify the plugin in the IDE and [JetBrains Marketplace](https://plugins.jetbrains.com).
Please use characters, numbers, and `'.'`/`'-'`/`'_'` symbols only and keep it reasonably short.

Warning:

Make sure to pick a stable ID, as the value cannot be changed later after public release.

Required
: no; ignored in an [additional config file](#additional-plugin-configuration-files)

It is highly recommended to set in `plugin.xml` file.

The element can be skipped in the source `plugin.xml` file if the Gradle plugin `patchPluginXml` task
([2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml))
is enabled and configured.

Default value
: Value of the [&lt;name&gt;](#idea-plugin__name) element.

Example
: ```XML
<id>com.example.framework</id>
```

### name

`name`



<tldr>
Reference: [JetBrains Marketplace: Plugin Name](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html#plugin-name)
</tldr>





The user-visible plugin display name (Title Case).



Required
: yes; ignored in an [additional config file](#additional-plugin-configuration-files)


Example
: ```XML
<name>My Framework Support</name>
```

### version

`version`

<tldr>
Reference: [JetBrains Marketplace: Semantic Versioning](https://plugins.jetbrains.com/docs/marketplace/semver.html)
</tldr>

The plugin version displayed in the Plugins settings dialog and on the
[JetBrains Marketplace](https://plugins.jetbrains.com) plugin page.
Plugins uploaded to the JetBrains Marketplace must follow semantic versioning.

Required
: yes; ignored in an [additional config file](#additional-plugin-configuration-files)

The element can be skipped in the source `plugin.xml` file if the Gradle plugin `patchPluginXml` task
([2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml),
[1.x](https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html#tasks-patchpluginxml))
is enabled and configured.

Example
: ```XML
<version>1.3.18</version>
```

### product-descriptor

`product-descriptor`



<tldr>
Reference: [JetBrains Marketplace: How to add required parameters for paid plugins](https://plugins.jetbrains.com/docs/marketplace/add-required-parameters.html)
</tldr>





[Paid](https://plugins.jetbrains.com/build-and-market) or
[Freemium](https://plugins.jetbrains.com/docs/marketplace/freemium.html) plugin descriptor.



Required
: only for paid or freemium plugins; ignored in an [additional config file](#additional-plugin-configuration-files)

Do not add `<product-descriptor>` element in a free plugin.


Attributes
: * `code` (required) The plugin product code used in the JetBrains Sales System. The code must be agreed with JetBrains in advance and follow [the requirements](https://plugins.jetbrains.com/docs/marketplace/add-required-parameters.html).

  * `release-date` (required) Date of the major version release in the `YYYYMMDD` format.

  * `release-version` (required) A major version in a specific number format, for example, `20242` for the 2024.2 major release. See [release-version constraints](https://plugins.jetbrains.com/docs/marketplace/versioning-of-paid-plugins.html#release-version-constraints) for more details.

  * `optional` (optional) The boolean value determining whether the plugin is a [Freemium](https://plugins.jetbrains.com/docs/marketplace/freemium.html) plugin. Default value: `false`.

  * `eap` (optional) Specifies the boolean value determining whether the plugin is an EAP release. Default value: `false`.

### idea-version (sdk.plugin-configuration-file.idea-plugin.idea-version)
### vendor

`vendor`



<tldr>
Reference: [JetBrains Marketplace: Contacts and Resources](https://plugins.jetbrains.com/docs/marketplace/best-practices-for-listing.html#contacts-and-resources)
</tldr>





The vendor name or organization ID (if created) in the Plugins settings dialog and on
the [JetBrains Marketplace](https://plugins.jetbrains.com) plugin page.



Required
: yes; ignored in an [additional config file](#additional-plugin-configuration-files)


Attributes
: * `url` (optional) The URL to the vendor's homepage. Supports `https://` and `http://` scheme links.

  * `email` (optional) The vendor's email address.


Examples
: * Personal vendor with an email address provided: ```XML <vendor email="joe@example.com">Joe Doe</vendor> ```

  * Organizational vendor with a website URL and email address provided: ```XML <vendor url="https://mycompany.example.com" email="contact@example.com"> My Company </vendor> ```

### description

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

### change-notes

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

### depends

`depends`



<tldr>
Reference: [Plugin Dependencies](https://plugins.jetbrains.com/docs/intellij/plugin-dependencies.html)
, [Modules Specific to Functionality](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#modules-specific-to-functionality)
</tldr>





Specifies a dependency on another plugin or a module of an IntelliJ Platform-based product.
A single [&lt;idea-plugin&gt;](#idea-plugin) element can contain multiple `<depends>` elements.



Required
: no; in most cases dependency on the
[platform](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#modules-available-in-all-products)
module is needed


Attributes
: * `optional` (optional) Boolean value defining whether the dependency is optional to load the plugin in the IDE. If the dependency plugin is not installed in the current IDE, and `optional` is: * `true` - the plugin will be loaded * `false` (default) - the plugin will not be loaded

  * `config-file` (required when `optional` is `true`) Relative path to an [additional configuration file](#additional-plugin-configuration-files), loaded only if the dependency plugin is installed in the current IDE.


Examples
: * Required plugin dependency: ```XML <depends>com.example.dependencypluginid</depends> ```

  * Required dependency on the IntelliJ IDEA Java Module: ```XML <depends>com.intellij.modules.java</depends> ```

  * Required module dependency with additional configuration: ```XML <depends config-file="myPluginId-withJava.xml"> com.intellij.modules.java </depends> ```

  * Optional module dependency with additional configuration: ```XML <depends optional="true" config-file="myPluginId-withKotlin.xml"> org.jetbrains.kotlin </depends> ```

### incompatible-with

`incompatible-with`



<tldr>
Reference: [Declaring Incompatibility with Plugin](https://plugins.jetbrains.com/docs/intellij/plugin-compatibility.html#declaring-incompatibility-with-module)
</tldr>






The [ID](#idea-plugin__id) or alias of the plugin the current plugin is incompatible with.
The plugin is not loaded if the incompatible plugin is installed in the current IDE.



Required
: no; ignored in an [additional config file](#additional-plugin-configuration-files)


Examples
: * Incompatibility with the Java plugin: ```XML <incompatible-with> com.intellij.java </incompatible-with> ```

  * Incompatibility with the AppCode plugin referenced via its alias: ```XML <incompatible-with> com.intellij.modules.appcode.ide </incompatible-with> ```

### extensions (sdk.plugin-configuration-file.idea-plugin.extensions)
### extensionPoints (sdk.plugin-configuration-file.idea-plugin.extensionpoints)
### resource-bundle

`resource-bundle`







A resource bundle to be used with message key attributes in extension declarations and for
[action and group localization](https://plugins.jetbrains.com/docs/intellij/action-system.html#localizing-actions-and-groups).
A single [&lt;idea-plugin&gt;](#idea-plugin) element can contain multiple `<resource-bundle>` elements.



Required
: no


Example
: To load the content of `messages/Bundle.properties` bundle, declare:
: ```XML
<resource-bundle>messages.Bundle</resource-bundle>
```

### actions (sdk.plugin-configuration-file.idea-plugin.actions)
### applicationListeners

`applicationListeners`

<tldr>
Reference: [Defining Application-Level Listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html#defining-application-level-listeners)
</tldr>

Defines the application-level listeners.

Required
: no

Children
: * [&lt;listener&gt;](#idea-plugin__applicationListeners__listener)

#### listener

`listener`

<tldr>
Reference: [Listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html)
</tldr>

Defines a single application or project-level listener.
A single [&lt;applicationListeners&gt;](#idea-plugin__applicationListeners) or
[&lt;projectListeners&gt;](#idea-plugin__projectListeners) can contain multiple `<listener>` elements.

Required
: no

Attributes
: * `topic` (required) The fully qualified name of the listener interface corresponding to the type of received events.

* `class` (required) The fully qualified name of the class implementing the listener interface that receives and handles the events.

* `os` (optional; available since 2020.1) Restricts listener instantiation to a specific operating system. Allowed values: * `freebsd` * `mac` * `linux` * `unix` * `windows`

* `activeInTestMode` (optional) Boolean flag defining whether the listener should be instantiated in test mode. Default value: `true`.

* `activeInHeadlessMode` (optional) Boolean flag defining whether the listener should be instantiated in headless mode. Default value: `true`.

Example
: ```XML
<listener
topic="com.intellij.ide.AppLifecycleListener"
class="com.example.MyListener"
os="mac"
activeInTestMode="false"/>
```

### projectListeners

`projectListeners`



<tldr>
Reference: [Defining Project-Level Listeners](https://plugins.jetbrains.com/docs/intellij/plugin-listeners.html#defining-project-level-listeners)
</tldr>






Defines the project-level listeners.



Required
: no


Children
: * [&lt;listener&gt;](#idea-plugin__applicationListeners__listener)

### xi:include

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

#### xi:fallback

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

### application-components (sdk.plugin-configuration-file.idea-plugin.application-components)
### project-components

`project-components`








Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





Defines a list of project [components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html).



Deprecated
: since 2020.1


Required
: no


Children
: * [&lt;component&gt;](#idea-plugin__application-components__component) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

### module-components

`module-components`








Warning: 

Do not use it in new plugins.
See [Components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html) for the migration guide.





Defines a list of module [components](https://plugins.jetbrains.com/docs/intellij/plugin-components.html).



Deprecated
: since 2020.1


Required
: no


Children
: * [&lt;component&gt;](#idea-plugin__application-components__component) ![Deprecated](https://img.shields.io/badge/-Deprecated-7f7f7f?style=flat-square)

---
id: sdk.inspection-options.declarative-inspection-options.custom-options-binding-protocol
title: Inspection Options: Custom Options Binding Protocol
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, custom, options, binding, protocol]
---
The default way of binding option form values to fields may be not enough in more advanced cases.
It is possible to customize the way of binding options by providing a custom
[OptionController](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/options/OptionController.java)
from `InspectionProfileEntry.getOptionController()`.

Consider the `Properties files | Inconsistent resource bundle` global inspection, from the bundled Properties plugin in IntelliJ IDEA, which reports several types of inconsistencies in `.properties` files.
The inspection allows enabling or disabling reporting specific issue types, which are reported by providers implementing a dedicated interface.
Information about enabled providers is stored in a map where the key is a provider ID.
The options panel and value binding are implemented in the following way (see
[InconsistentResourceBundleInspection](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/plugins/java-i18n/src/com/intellij/codeInspection/i18n/inconsistentResourceBundle/InconsistentResourceBundleInspection.java)
for the full implementation context):

```JAVA
private NotNullLazyValue<InconsistentResourceBundleInspectionProvider[]> myProviders = ...;
private Map<String, Boolean> mySettings = new LinkedHashMap<>();

@Override
public @NotNull OptPane getOptionsPane() {
  return new OptPane(ContainerUtil.map(
    myProviders.getValue(),
    provider -> checkbox(provider.getName(), provider.getPresentableName())));
}

@Override
public @NotNull OptionController getOptionController() {
  return OptionController.of(
    (bindId) -> ContainerUtil.getOrElse(mySettings, bindId, true),
    (bindId, value) -> {
      boolean boolValue = (Boolean)value;
      if (boolValue) {
        mySettings.remove(bindId);
      } else {
        mySettings.put(bindId, false);
      }
    });
}
```

Option controls panel is built based on providers’ IDs and presentable names.
This implementation doesn't need to be changed regardless of removing or adding new providers in the future.

Reading and writing options in the map is achieved by registering a custom controller with getter and setter logic provided to the `OptionController.of()` method.

It's possible to compose several option controllers into the hierarchy based on the `bindId` prefix.
It may be useful when some inspections have common configuration options and store the configuration in dedicated objects.
See
[OptComponent.prefix()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/analysis-api/src/com/intellij/codeInspection/options/OptComponent.java)
and `OptionController.onPrefix()` methods for more details and an example implementation:
[MissingJavadocInspection](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/java/java-impl/src/com/intellij/codeInspection/javaDoc/MissingJavadocInspection.java).


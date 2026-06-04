# Custom Settings Groups

As described in [Extension Points for Settings](https://plugins.jetbrains.com/docs/intellij/settings-guide.html#extension-points-for-settings), custom Settings can be declared as children of existing parent groups such as `Tools`.
These parent groups are the existing categories of Settings in the IntelliJ Platform-based IDE.

However, suppose the custom Settings are rich enough to require multiple levels?
For example, a custom Setting implementation has multiple sub-Settings implementations.
Extension Point declarations can create this kind of multilayer Settings hierarchy.

Tip:

See [Inspecting Settings](https://plugins.jetbrains.com/docs/intellij/internal-ui-inspector.html#inspecting-settings) on how to gather information in the IDE instance for Settings dialog.

## Extension Points for Parent-Child Settings Relationships (sdk.custom-settings-groups.extension-points-for-parent-child-settings-relationships)
## Implementations for Parent-Child Settings

Implementations can be based on [Configurable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/Configurable.java), [ConfigurableProvider](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/options/ConfigurableProvider.java) or one of their subtypes.
For more information about creating Settings implementations, see [Implementations for Settings Extension Points](https://plugins.jetbrains.com/docs/intellij/settings-guide.html#implementations-for-settings-extension-points).

### Configurable Marker Interfaces

The `Configurable.Composite` interface indicates a configurable component has child components.
The preferred approach is to specify child components in the [EP declaration](#extension-points-for-parent-child-settings-relationships).
Using the `Composite` interface incurs the penalty of loading child classes while building the tree of Settings Swing components.


> Source: IntelliJ Platform SDK docs — Custom Settings Groups (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

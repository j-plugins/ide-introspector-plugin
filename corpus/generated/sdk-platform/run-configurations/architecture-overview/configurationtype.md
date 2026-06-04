# ConfigurationType

`ConfigurationType`

The entry point of a run configuration implementation is [ConfigurationType](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/ConfigurationType.java).
It is responsible for the run configuration type and instances presentation and contains [configuration factories](#configurationfactory).
A single configuration type can have multiple configuration factories, e.g., the Docker configuration type can create run configurations for:

* Dockerfile

* Docker Image

* Docker-compose

To see the list of configuration types available in the IDE, go to `Run \| Edit Configurations` and click the Add button (+ icon).

`ConfigurationType` implementations are registered in the [com.intellij.configurationType](https://jb.gg/ipe?extensions=com.intellij.configurationType) extension point
.

Standard base classes for configuration type implementations are:

* [SimpleConfigurationType](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/runConfigurationType.kt) - used for configuration types that have a single configuration factory. Actually, this configuration type is also a configuration factory, and there is no need for setting up a factory.

* [ConfigurationTypeBase](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/runConfigurationType.kt) - used for configuration types that have multiple configuration factories. Factories should be added in the constructor by calling the `addFactory()` method.

Marking a configuration type as [dumb aware](https://plugins.jetbrains.com/docs/intellij/indexing-and-psi-stubs.html#DumbAwareAPI) makes all its configurations available during indexing.

Sometimes, it is required to provide run configurations programmatically from contexts external to run configuration UI.
Implementing [VirtualConfigurationType](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/execution/src/com/intellij/execution/configurations/VirtualConfigurationType.java) blocks the possibility of adding and removing run configurations of this type in the Run/Debug Configurations panel.
Editing its template is also not available.

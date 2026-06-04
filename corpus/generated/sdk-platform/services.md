# Services

A service is a plugin component loaded on demand when your plugin calls the `getService()` method of corresponding [ComponentManager](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/extensions/src/com/intellij/openapi/components/ComponentManager.java) instance (see [Types](#types)).
The IntelliJ Platform ensures that only one instance of a service is loaded even though it is called several times.
Services are used to encapsulate logic operating on a set of related classes or to provide some reusable functionality that can be used across the plugin project.
Conceptually, they don't differ from the service classes in other languages or frameworks.

A service must have an implementation class used for service instantiation.
A service may also have an interface class used to obtain the service instance and provide the service's API.

A service needing a shutdown hook/cleanup routine can implement [Disposable](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/src/com/intellij/openapi/Disposable.java) and perform necessary work in `dispose()` (see [Automatically Disposed Objects](https://plugins.jetbrains.com/docs/intellij/disposers.html#automatically-disposed-objects)).

Note: Services as API

If declared services are intended to be used by other plugins depending on your plugin, consider [bundling their sources](https://plugins.jetbrains.com/docs/intellij/bundling-plugin-openapi-sources.html) in the plugin distribution.

## Types

The IntelliJ Platform offers three types of services: application-level services (global singleton), project-level services, and module-level services.
For the latter two, a separate instance of the service is created for each instance of its corresponding scope, see [Project Model Introduction](https://plugins.jetbrains.com/docs/intellij/project-model.html).

Note:

Avoid using module-level services as it can increase memory usage for projects with many modules.

## Constructor (sdk.services.constructor)
## Light Services (sdk.services.light-services)
## Declaring a Service (sdk.services.declaring-a-service)
## Retrieving a Service (sdk.services.retrieving-a-service)
## Sample Plugin

To clarify how to use services, consider the maxOpenProjects sample plugin available in the [code samples](https://github.com/JetBrains/intellij-sdk-code-samples/tree/main/max_opened_projects).

This plugin has an application service counting the number of currently opened projects in the IDE.
If this number exceeds the maximum number of simultaneously opened projects allowed by the plugin (3), it displays an information message.

See [Code Samples](https://plugins.jetbrains.com/docs/intellij/code-samples.html) on how to set up and run the plugin.


> Source: IntelliJ Platform SDK docs — Services (build 261.24374.151). https://plugins.jetbrains.com/docs/intellij/llms.txt

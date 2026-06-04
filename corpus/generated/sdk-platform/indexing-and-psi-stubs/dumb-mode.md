# Dumb Mode

Indexing is a potentially lengthy process.
It's performed in the background, and during this time, all IDE features are restricted to the ones that don't require indexes: basic text editing, version control, etc.
This restriction is managed by [DumbService](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/DumbService.kt).
Violations are reported via [IndexNotReadyException](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/IndexNotReadyException.java), see its documentation for information on how to adapt callers.

`DumbService` provides API to query whether the IDE is currently in "dumb" mode (where index access is not allowed) or "smart" mode (with all index built and ready to use).
It also provides ways of delaying code execution until indexes are ready.

[Video](https://www.youtube.com/v/ApdNfPuGJRU)
Learn how techniques like dumb mode index access, on-demand indexing, and lightweight heuristics can boost plugin performance and streamline your development process,
all while maintaining robust coding assistance.

### DumbAware` API

`DumbAware` API

Tip: Finding Candidates

Use inspection Plugin DevKit | Code | Can be DumbAware (2025.1+) to find implementations
that can potentially be marked as `DumbAware`.

#### Extension Points

Implementations of certain [extension points](https://plugins.jetbrains.com/docs/intellij/plugin-extension-points.html) can be marked as available during Dumb Mode by implementing
[DumbAware](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/DumbAware.java).
Such extension points are marked with the
![DumbAware](https://img.shields.io/badge/-DumbAware-darkgreen?style=flat-square)
tag in [IntelliJ Platform Extension Point and Listener List](https://plugins.jetbrains.com/docs/intellij/intellij-platform-extension-point-list.html).

Commonly used extension points include [CompletionContributor](https://plugins.jetbrains.com/docs/intellij/code-completion.html), [(External)Annotator](https://plugins.jetbrains.com/docs/intellij/syntax-highlighting-and-error-highlighting.html#annotator) and various
[run configuration](https://plugins.jetbrains.com/docs/intellij/run-configurations.html) EPs.
Since 2024.2, this includes also [intentions](https://plugins.jetbrains.com/docs/intellij/code-intentions.html) and [quick-fixes](https://plugins.jetbrains.com/docs/intellij/quick-fix.html).

#### Actions

For [actions](https://plugins.jetbrains.com/docs/intellij/action-system.html) available during Dumb Mode, extend [DumbAwareAction](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/ide-core/src/com/intellij/openapi/project/DumbAwareAction.java) (do not override `AnAction.isDumbAware()` instead).

#### Other API

Other API might indicate its Dumb Mode compatibility by extending [PossiblyDumbAware](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/PossiblyDumbAware.java).

### Testing

To toggle Dumb Mode for testing purposes, invoke `Tools | Internal Actions | Enter/Exit Dumb Mode`
while the IDE is running in [internal mode](https://plugins.jetbrains.com/docs/intellij/enabling-internal.html).

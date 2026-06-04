---
id: sdk.indexing-and-psi-stubs.dumb-mode
title: Indexing and PSI Stubs: Dumb Mode
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, dumb, mode]
---
Indexing is a potentially lengthy process.
It's performed in the background, and during this time, all IDE features are restricted to the ones that don't require indexes: basic text editing, version control, etc.
This restriction is managed by [DumbService](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/DumbService.kt).
Violations are reported via [IndexNotReadyException](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/core-api/src/com/intellij/openapi/project/IndexNotReadyException.java), see its documentation for information on how to adapt callers.

`DumbService` provides API to query whether the IDE is currently in "dumb" mode (where index access is not allowed) or "smart" mode (with all index built and ready to use).
It also provides ways of delaying code execution until indexes are ready.

[Video](https://www.youtube.com/v/ApdNfPuGJRU)
Learn how techniques like dumb mode index access, on-demand indexing, and lightweight heuristics can boost plugin performance and streamline your development process,
all while maintaining robust coding assistance.

### DumbAware` API (indexing-and-psi-stubs/dumb-mode/dumbaware-api.md)
#### Extension Points (indexing-and-psi-stubs/dumb-mode/dumbaware-api/extension-points.md)
#### Actions (indexing-and-psi-stubs/dumb-mode/dumbaware-api/actions.md)
#### Other API (indexing-and-psi-stubs/dumb-mode/dumbaware-api/other-api.md)
### Testing (indexing-and-psi-stubs/dumb-mode/testing.md)

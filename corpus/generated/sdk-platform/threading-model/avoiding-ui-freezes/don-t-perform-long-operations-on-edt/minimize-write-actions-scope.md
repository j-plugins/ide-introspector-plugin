---
id: sdk.threading-model.avoiding-ui-freezes.don-t-perform-long-operations-on-edt.minimize-write-actions-scope
title: Threading Model: Minimize Write Actions Scope
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, minimize, write, actions, scope]
---
Write actions currently [have to happen on EDT](#locks-and-edt).
To speed them up, as much as possible should be moved out of the write action into a preparation step which can be then invoked in the [background](https://plugins.jetbrains.com/docs/intellij/background-processes.html) or inside an [NBRA](#non-blocking-read-actions-api).


---
id: sdk.threading-model.faq.why-write-actions-are-currently-allowed-only-on-edt
title: Threading Model: Why write actions are currently allowed only on EDT?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, why, write, actions, are, currently]
---
Reading data model was often performed on EDT to display results in the UI.
The IntelliJ Platform is more than 20 years old, and in its beginnings Java didn't offer features like generics and lambdas.
Code that acquired read locks was very verbose.
For convenience, it was decided that reading data can be done on EDT without read locks (even implicitly acquired).

The consequence of this was that writing had to be allowed only on EDT to avoid read/write conflicts.
The nature of EDT provided this possibility out-of-the-box due to being a single thread.
Event queue guaranteed that reads and writes were ordered and executed one by one and couldn't interweave.


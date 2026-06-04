---
id: sdk.threading-model.faq.how-to-check-whether-the-current-thread-is-the-edt-ui-thread
title: Threading Model: How to check whether the current thread is the EDT/UI thread?
source: generated
kind: reference
verifiedAgainstBuild: 261.24374.151
tags: [sdk-platform, how, check, whether, current, thread]
---
Use `Application.isDispatchThread()`.

If code must be invoked on EDT and the current thread can be EDT or BGT, use [UIUtil.invokeLaterIfNeeded()](https://github.com/JetBrains/intellij-community/tree/idea/261.24374.151/platform/util/ui/src/com/intellij/util/ui/UIUtil.java).
If the current thread is EDT, this method will run code immediately or will schedule a later invocation if the current thread is BGT.


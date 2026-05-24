package com.github.xepozz.ide.introspector.examples

import com.intellij.openapi.diagnostic.thisLogger

class IntrospectorDemoListenerImpl : IntrospectorDemoListener {
    override fun onIntrospectionPing(message: String) {
        thisLogger().info("IntrospectorDemoListener received: $message")
    }
}

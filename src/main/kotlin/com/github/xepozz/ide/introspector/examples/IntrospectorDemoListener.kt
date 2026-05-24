package com.github.xepozz.ide.introspector.examples

import com.intellij.util.messages.Topic

interface IntrospectorDemoListener {
    fun onIntrospectionPing(message: String)

    companion object {
        @JvmField
        val TOPIC: Topic<IntrospectorDemoListener> = Topic.create(
            "IDE Introspector Demo Ping",
            IntrospectorDemoListener::class.java,
        )
    }
}

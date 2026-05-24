package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

@Serializable
data class ServiceInfo(
    /** Service interface FQN. When XML omits the attribute, equals [serviceImplementation]. */
    val serviceInterface: String,
    /** Concrete implementation FQN. Always present. */
    val serviceImplementation: String,
    /** "application" | "project" | "module". */
    val scope: String,
    /** Raw PreloadMode name: "FALSE" (default), "TRUE", "NOT_HEADLESS", "NOT_LIGHT_EDIT", "AWAIT". */
    val preload: String = "FALSE",
    /** ServiceDescriptor.overrides — true when this declaration replaces a previously-registered service. */
    val overrides: Boolean = false,
    val testServiceImplementation: String? = null,
    val headlessImplementation: String? = null,
    val providedByPluginId: String,
    val providedByPluginName: String?,
)

@Serializable
data class ListServicesResponse(
    val services: List<ServiceInfo>,
    val total: Int,
)

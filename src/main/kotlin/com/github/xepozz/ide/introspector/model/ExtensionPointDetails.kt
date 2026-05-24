package com.github.xepozz.ide.introspector.model

import kotlinx.serialization.Serializable

/**
 * Full descriptor for a single Extension Point — returned by `arch.get_extension_point_details`.
 *
 * Either [beanSchema] (for BEAN_CLASS EPs) or [interfaceMethods] (for INTERFACE EPs) is
 * populated depending on the EP's kind and the caller's `include*` flags. Both fields can
 * be null when the extension class can't be resolved (broken plugin / classloader miss) or
 * when the caller opted out via flags.
 */
@Serializable
data class ExtensionPointDetails(
    val name: String,
    val kind: String,                       // "INTERFACE" | "BEAN_CLASS"
    val interfaceOrBeanClass: String,
    val declaredByPluginId: String,
    val declaredByPluginName: String?,
    val dynamic: Boolean,
    val area: String,                       // "application" | "project"
    val beanSchema: BeanSchema? = null,
    val interfaceMethods: List<MethodSig>? = null,
    val registeredCount: Int? = null,
)

@Serializable
data class BeanSchema(
    val className: String,
    val fields: List<BeanField>,
    val truncated: Boolean = false,         // true if maxFields cap hit
)

@Serializable
data class BeanField(
    val name: String,                       // Java field name
    val xmlAttributeName: String?,          // @Attribute(name=...) override, else field name (null if @Tag-only)
    val xmlTagName: String?,                // @Tag value or @Property(style=TAG) hint
    val type: String,                       // raw type (no generics)
    val required: Boolean,                  // @RequiredElement present
    val defaultValue: String?,              // best-effort: static-final literal only; null otherwise
    val deprecated: Boolean,
)

@Serializable
data class MethodSig(
    val name: String,
    val signature: String,                  // "(ParamType1, ParamType2): ReturnType"
    val returnType: String,
    val deprecated: Boolean,
)

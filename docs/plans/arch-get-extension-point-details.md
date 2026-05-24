# `arch.get_extension_point_details`

## Purpose & motivation

`arch.list_extension_points` enumerates EPs but only surfaces shallow metadata
(name, kind, declaring plugin, count). Plugin developers asking "how do I plug
into EP X?" still have to grep IntelliJ Community for the bean schema (required
vs optional attributes, `@Tag` element names) or the interface methods they
must implement. `arch.get_extension_point_details` closes that gap for ONE EP —
either the bean schema (every `@Attribute`/`@Property`/`@Tag` + `@RequiredElement`)
for `BEAN_CLASS` EPs, or the public method signatures for `INTERFACE` EPs.
JetBrains' built-in MCP server has no equivalent.

**Success criterion:** given an EP name (e.g. `com.intellij.toolWindow`), an
agent can describe every XML attribute the user must / may set in one tool call.

## Tool specification

### `arch.get_extension_point_details`

**Signature:**
```kotlin
@McpTool(name = "arch.get_extension_point_details")
@McpDescription("…")  // see full text below
suspend fun arch_get_extension_point_details(
    @McpDescription("Fully qualified EP name as returned by arch.list_extension_points, e.g. 'com.intellij.toolWindow', 'com.intellij.applicationConfigurable'. Required.")
    name: String,
    @McpDescription("For BEAN_CLASS EPs, harvest the bean class's @Attribute / @Property / @Tag / @RequiredElement annotations into beanSchema. Default true. Set false when you only need kind + declaring plugin.")
    includeBeanSchema: Boolean = true,
    @McpDescription("For INTERFACE EPs, list the extension interface's public abstract methods (signature + return type). Default true.")
    includeInterfaceMethods: Boolean = true,
    @McpDescription("Include the live adapter count (ep.size() — does NOT instantiate extensions) under registeredCount. Default false; flip on when you want the count without a follow-up arch.list_extension_points call.")
    includeRegisteredCount: Boolean = false,
    @McpDescription("Hard cap on bean fields / interface methods returned (per side). Default 200 — protects against pathological beans inheriting from heavy hierarchies.")
    maxFields: Int = 200,
): ExtensionPointDetails?
```

**`@McpDescription` (verbatim, trim-margin):**

```
|Returns the full descriptor for ONE Extension Point: kind, bean class XML schema
|(for BEAN_CLASS EPs) or interface method signatures (for INTERFACE EPs), declared-in
|plugin, area, and dynamic flag. This is the "how do I plug into EP X?" tool — it
|surfaces every @Attribute / @Property / @Tag / @RequiredElement annotation on the
|bean so an agent can generate a correct <extension> XML snippet without grepping
|IntelliJ Community sources.
|
|Use this when: a user asks "what fields does ToolWindowEP take?", "is `id` required
|on com.intellij.applicationConfigurable?", "what methods do I implement for EP X?",
|or you've identified an EP via arch.list_extension_points and need to scaffold an
|extension for it.
|
|Do NOT use this when: you want every EP at once (arch.list_extension_points), the
|list of existing contributors (arch.list_extensions_for_ep), or the source of a
|specific implementation class (code.get_class_source).
|
|Returns: ExtensionPointDetails { name, kind ('INTERFACE'|'BEAN_CLASS'),
|interfaceOrBeanClass (FQCN), declaredByPluginId, declaredByPluginName, dynamic,
|area ('application'|'project'), beanSchema?: { className, fields: [{ name,
|xmlAttributeName, xmlTagName, type, required, defaultValue, deprecated }] },
|interfaceMethods?: [{ name, signature, returnType, deprecated }], registeredCount?: int }.
|Returns null when the EP name is not registered in any open area.
|
|Examples:
|  name="com.intellij.toolWindow"                                        — bean schema for ToolWindowEP (id/anchor/factoryClass/icon…)
|  name="com.intellij.applicationConfigurable", includeRegisteredCount=true — Configurable EP schema + how many are registered
|  name="com.intellij.codeInsight.lineMarkerProvider"                    — INTERFACE EP — lists LineMarkerProvider methods
```

**Response model (`model/ExtensionPointDetails.kt`, new file):**

```kotlin
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
    val xmlAttributeName: String?,          // @Attribute(name=…) override, else field name (null if @Tag-only)
    val xmlTagName: String?,                // @Tag value or @Property(style=ATTRIBUTE/TAG) hint
    val type: String,                       // raw type (no generics)
    val required: Boolean,                  // @RequiredElement present
    val defaultValue: String?,              // best-effort: from default field initializer if reflectively visible
    val deprecated: Boolean,
)

@Serializable
data class MethodSig(
    val name: String,
    val signature: String,                  // "(ParamType1, ParamType2): ReturnType"
    val returnType: String,
    val deprecated: Boolean,
)
```

**Args model (`model/args/`):** none — params are inline on the tool method
(matches the convention used by other `arch.*` tools in `ArchitectureToolset`).

## IntelliJ APIs used

- `ExtensionsArea.getExtensionPointIfRegistered(name)` — public; null if absent
  (reused from `ExtensionPointInspector.locateEp`).
- `com.intellij.openapi.extensions.impl.ExtensionPointImpl` (`@ApiStatus.Internal`,
  reflection only — same pattern as `ExtensionPointInspector.kindAndClass`):
  `getKind()` → `INTERFACE` | `BEAN_CLASS`; `getExtensionClass(): Class<*>`
  (only forces classload, never instantiates); `getPluginDescriptor()`;
  `size()` (adapter count, no instantiation per CLAUDE.md pitfall).
- `com.intellij.util.xmlb.annotations.{Attribute, Property, Tag, RequiredElement}`
  — the XML serialization annotation set. Stable, public.
- `kotlin.Deprecated` + `java.lang.Deprecated` — surface as `deprecated` flag.

**Reflection pattern for bean schema** (mirrors `ExtensionMetadata.harvestBeanFields`,
walks annotations instead of values): walk `cls` and superclasses up to
`Any`/`AbstractExtensionPointBean`; for each non-static field, read
`@Attribute(value=…)`, `@Property`, `@Tag(value=…)`, `@RequiredElement`,
`@Deprecated`. Public unannotated fields ARE serialized by `XmlSerializer`
using the field name — include them with `xmlAttributeName = f.name`. Private
unannotated fields are skipped. Cap with `out.size >= maxFields`.

**Interface methods:** `cls.methods.filter { !it.declaringClass.isObject &&
Modifier.isAbstract(it.modifiers) && !it.isSynthetic }`. Format signature as
`(Type1, Type2): Return` using simple names.

## Threading & EDT model

- Thread-safe. `ExtensionPoint` enumeration is documented thread-safe (used by
  `arch.list_extension_points` without an EDT bounce); reflection on `Class<*>`
  members is JVM-safe. **No `onEdtBlocking` needed.**
- No PSI / VFS access ⇒ no `ReadAction`.
- Cache: skip. Per-call cost is one EP lookup + one reflection walk on a single
  class hierarchy — cheap enough that `TtlCache` is overhead. If repeated calls
  for the same EP show up in profiling, key a `TtlCache<String, ExtensionPointDetails>`
  on `name`.

## Timeout strategy

Bounded by `maxFields = 200` (per CLAUDE.md hard 10 s rule). Worst-case bean
hierarchy is a handful of classes; reflection on 200 fields is sub-100 ms. No
explicit `withTimeoutOrNull` needed, but a defensive `runCatching` wrap around
the reflection walk converts pathological classloader failures into a partial
response with `truncated = true` instead of throwing.

## Edge cases

1. **EP not found** — return `null` (not throw). Locate across application +
   open project areas (mirror `locateEp`).
2. **Bean class with no xmlb annotations** — XmlSerializer still uses public
   field name; include with `xmlAttributeName = f.name`. Skip private unannotated.
3. **Generics on bean fields** — record raw `f.type.name`; generic info isn't
   needed for XML schema and would balloon the response.
4. **INTERFACE EP whose interface is `@ApiStatus.Internal` or an inner class** —
   still reflectable; surface `deprecated` from `@Deprecated`; don't filter out
   (caller asked by name).
5. **Dynamic vs non-dynamic** — surface via existing `ExtensionPointInspector.isDynamic`.
6. **Heavy bean hierarchies** (e.g. extending `JComponent`) — `maxFields = 200`
   cap, sets `BeanSchema.truncated = true`.
7. **`@Property(style = TAG)`** — render as nested element: `xmlAttributeName = null`,
   `xmlTagName = f.name` (or `@Tag` override).
8. **`extensionClass` unresolvable** (broken plugin / classloader miss) —
   catch, omit `beanSchema`/`interfaceMethods`, keep descriptor populated from
   string reflection (`tryReadClassNameField`).
9. **`includeRegisteredCount=true`** — use `ep.size()` (adapter count, no
   instantiation per CLAUDE.md pitfall); never `extensionList.size`.
10. **Same name in both app and project area** — prefer application; record `area`.

## Files to create/modify

| Path | Op | What |
|------|----|------|
| `src/main/kotlin/com/github/xepozz/ide/introspector/tools/ArchitectureToolset.kt` | Edit | Add `arch_get_extension_point_details` `@McpTool` method (verbatim `@McpDescription` from above). |
| `src/main/kotlin/com/github/xepozz/ide/introspector/core/ExtensionPointInspector.kt` | Edit | Add `getDetails(name, includeBeanSchema, includeInterfaceMethods, includeRegisteredCount, maxFields): ExtensionPointDetails?` — reuses `locateEp`, `kindAndClass`, `pluginDescriptorOf`, `isDynamic`. New private `harvestBeanSchema(cls, max)` and `harvestInterfaceMethods(cls, max)` helpers. |
| `src/main/kotlin/com/github/xepozz/ide/introspector/model/ExtensionPointDetails.kt` | Create | The four `@Serializable` data classes above (`ExtensionPointDetails`, `BeanSchema`, `BeanField`, `MethodSig`). |
| `src/test/kotlin/com/github/xepozz/ide/introspector/core/ExtensionPointDetailsTest.kt` | Create | Unit — feed a hand-rolled fake bean (`@Attribute("id") val id: String`, `@RequiredElement` etc.) to `harvestBeanSchema` and assert the `BeanField` list. No IntelliJ runtime needed. |
| `src/test/kotlin/com/github/xepozz/ide/introspector/core/platform/ExtensionPointDetailsPlatformTest.kt` | Create | Platform — `BasePlatformTestCase`; resolve `com.intellij.toolWindow`, assert `kind == "BEAN_CLASS"`, `beanSchema.fields` contains `id`, `anchor`, `factoryClass`, `icon`. |

No new META-INF wiring — `arch.*` is already registered.

## Test plan

**Unit (`ExtensionPointDetailsTest.kt`)** — feed hand-rolled fake bean classes:
- `@Attribute("id") @RequiredElement var id: String = ""` → `xmlAttributeName="id"`, `required=true`.
- Public unannotated field included with `xmlAttributeName=fieldName`.
- Private unannotated and `static` fields excluded.
- `@Tag("nested") var nested: String` → `xmlAttributeName=null`, `xmlTagName="nested"`.
- `@Deprecated` field → `deprecated=true`.
- `maxFields=2` on a 5-field bean → `truncated=true`.

**Platform (`ExtensionPointDetailsPlatformTest.kt`)** — `BasePlatformTestCase`:
- `getDetails("com.intellij.toolWindow")` → non-null, `kind="BEAN_CLASS"`,
  `interfaceOrBeanClass="com.intellij.openapi.wm.ToolWindowEP"`,
  `beanSchema.fields.map{it.name}` ⊇ `{id, anchor, factoryClass}`.
- `getDetails("com.intellij.codeInsight.lineMarkerProvider")` → `kind="INTERFACE"`,
  `interfaceMethods` contains `getLineMarkerInfo`.
- `getDetails("definitely.not.an.ep")` → `null` (not throws).
- `includeRegisteredCount=true` matches `arch.list_extension_points` for the same EP.

## Estimated effort

~1 day total: model + inspector method 3 h, toolset wiring 0.5 h, unit tests
2 h, platform tests 2 h, doc-gen verification 0.5 h.

## Open questions / risks

1. **Render an example `<extension>` snippet from the bean schema?** E.g. emit
   `exampleXml: "<toolWindow id=\"…\" anchor=\"…\" factoryClass=\"…\"/>"`.
   Defer to v2 — agent can synthesize from the schema; trivially additive.
2. **Default value extraction.** Kotlin `val x: String = "y"` compiles the
   literal into `<init>`, unreachable without instantiating. Best effort: read
   `static final` literals; otherwise `defaultValue = null`. We do NOT
   instantiate the bean (would mirror the `ep.extensionList.size` pitfall when
   bean constructors touch project services).
3. **`@MapAnnotation` / `@XCollection`** — used for `Map`/`List` fields in a
   handful of EPs. Skip in v1; extend `BeanField.collectionKind` if reported.
4. **Inherited `AbstractExtensionPointBean` fields** (`order`, `orderId`,
   `pluginDescriptor`) — framework noise. Filter by declaring class by default;
   add `includePlatformBaseFields: Boolean = false` knob if needed.
5. **`ExtensionPointImpl.getKind()` stability** — `@ApiStatus.Internal`, but
   `INTERFACE` / `BEAN_CLASS` names stable since 2019; existing `kindAndClass`
   already handles drift with graceful fallback.

## References

- Existing code: `ExtensionPointInspector.{kindAndClass, tryReadExtensionClass,
  isDynamic, pluginDescriptorOf, locateEp}` — all reused.
  `ExtensionMetadata.harvestBeanFields` is the *value* counterpart of the
  *schema* walker being added.
- IntelliJ source: `platform/extensions/.../ExtensionPointImpl.kt` for
  `getKind` / `getExtensionClass` / `size`; `platform/util/.../xmlb/annotations/`
  for the annotation set.
- JetBrains MCP equivalent: none — closest built-in is `get_class_source` (text,
  not a structured schema).

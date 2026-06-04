package com.github.xepozz.ide.introspector.context

enum class Source { MANUAL, GENERATED }

enum class Kind { SKILL, CONCEPT, REFERENCE, EXAMPLE }

enum class State { DRAFT, VERIFIED, STALE, DEPRECATED }

enum class Severity { ERROR, WARNING }

enum class CorpusLayer {
    MANUAL,
    SDK_PLATFORM,
    INTROSPECTOR_DOCS,
    EXAMPLES,
    UNKNOWN,
}

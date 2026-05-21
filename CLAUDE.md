# Project rules — read these first

## Timeouts: hard cap 10 seconds

Anything async/blocking in this codebase **must** time out at 10 seconds or less.
This applies to:

- `onEdtBlocking` and any other EDT-bouncing helper (`DEFAULT_EDT_TIMEOUT_MS = 10_000L`)
- `ExecSettings.defaultTimeoutMs` and `ExecSettings.maxTimeoutMs` (both capped at 10_000)
- `ExecuteKotlinArgs.timeoutMs` default and the `ExecToolset` parameter
- Anything new — `withTimeoutOrNull`, `Future.get`, network calls, locks, latches

If a tool genuinely needs more time, the right answer is to **make it cheaper** (narrower
scope, fewer features, async streaming) rather than to raise the timeout. Long timeouts in
this plugin freeze the IDE and break MCP-client UX.

## Build verification

`./gradlew build` must pass before any push. The IntelliJ Platform Gradle Plugin has been
known to choke on a stale `asm-all-9.6.1.jar` download from cloudfront — if that comes
back, manually drop the jar into
`~/.gradle/caches/modules-2/files-2.1/org.jetbrains.intellij.deps/asm-all/9.6.1/<sha1>/`
rather than wrestling with the retry logic.

## MCP server API target

We build against `com.intellij.mcpServer:252.28238.29` — the **new** annotation-driven
API (`McpToolset` + `@McpTool` + `@McpDescription`), NOT the older `AbstractMcpTool` /
`org.jetbrains.ide.mcp.Response` model that you'd find in the JetBrains/mcp-server-plugin
open-source mirror. That mirror is stale relative to what ships in the IDE.

## Serialization classloader policy

`kotlinx-serialization-json` is `compileOnly` (not `implementation`) so we use the
platform-bundled copy at runtime. If you change this back to `implementation`, our
`@Serializable` data classes will fail to be picked up by `CallableBridge.serializerOrNull`
because the IDE will see two different `KSerializer` types from two different classloaders.
The symptom is `"Result type X is not serializable"` even though the class is annotated.

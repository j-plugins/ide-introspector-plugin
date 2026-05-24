# `screenshot.highlight` + `screenshot.diff`

## Purpose & motivation

JetBrains' built-in MCP server (2025.2+) ships **zero** screenshot tools — this
group is fully our niche. Today's `ScreenshotToolset` (`capture` / `crop`) lets
an agent see the IDE but cannot point at a target ("where is the breadcrumb
bar?") or verify a visual change ("did Toggle Sidebar actually toggle?"). Two
helpers close that loop: `highlight` overlays a colored bbox around a known
componentId on a fresh capture; `diff` runs a pure-CPU pixel diff between two
caller-supplied base64 PNGs and returns a composite + stats + bbox.

**Success criteria**: (1) one MCP call `highlight(componentId=…)` produces a PNG
with a red rectangle around the Swing component, no client-side image work.
(2) `diff(before, after)` returns `differingPixels`, `diffPercentage`, and a
tight `bbox` so an agent can answer "did this action have a visible effect?"
without a multimodal round trip.

## Tool specification

### `screenshot.highlight`

```kotlin
@McpTool(name = "screenshot.highlight")
@McpDescription("""…verbatim below…""")
suspend fun screenshot_highlight(
    componentId: String,                     // ComponentRegistry id; must still be attached
    target: String = "active_frame",         // 'component'|'active_frame'|'screen'; 'all_frames' rejected
    color: String = "#FF0000",               // CSS hex or named; invalid → red + warning
    thickness: Int = 3,                      // source-px stroke, clamped 1..20
    label: String? = null,                   // UTF-8-truncated to 80, newlines collapsed
    scale: Double = 1.0,                     // applied AFTER overlay so stroke scales too
    format: String = "png",                  // only 'png' in v1
): ImagePayload
// Each parameter carries an @McpDescription matching the prose above.
```

`@McpDescription` verbatim (trim-margin):

```
|Captures a screenshot of the IDE (target='component'|'active_frame'|'screen') AND
|overlays a colored bounding rectangle around the Swing component identified by
|componentId, optionally with a text label. Returns the same base64-PNG
|ImagePayload as screenshot.capture, downscaled to the MCP response budget.
|
|target options (must match the coordinate space the highlight is drawn in):
|  - "component"    — render only the target component; the box fills it. Cheapest.
|  - "active_frame" — render the focused IDE frame, box at frame-relative bounds
|                     (Component.getLocationOnScreen minus frame origin). Off-screen
|                     components are clipped to the frame edge with a warning.
|  - "screen"       — Robot capture of the virtual desktop with the box at the
|                     component's absolute screen coordinates. The only target that
|                     includes popups / tooltips / floating overlays.
|
|Use this when: the user asks "where is X on screen?", or after a ui.find_by_*
|call you want to visually confirm which component matched, or to annotate a
|screenshot for screenshot-and-narrate workflows.
|
|Do NOT use this when: you just want raw pixels (use screenshot.capture), you want
|a crop centered on the component (use screenshot.crop after ui.get_properties), or
|you need to highlight multiple components in one image (not supported in v1).
|
|Returns: { mimeType:"image/png", width:int, height:int, base64:string,
|warnings:string[] } — same shape as screenshot.capture. warnings includes
|"component clipped to frame", "color parse fell back to red", and the standard
|"image downscaled to fit budget" notices.
|
|Examples:
|  componentId="c_a3f2e1b8"                                    — red box on active frame
|  componentId="c_a3f2e1b8", color="#33CC33", thickness=5      — fatter green box
|  componentId="c_a3f2e1b8", target="screen", label="OK btn"   — labelled, includes popups
|  componentId="c_a3f2e1b8", target="component", scale=0.5     — component-only, halved
```

Response: existing `ImagePayload`. Box geometry not serialized (caller-supplied).

### `screenshot.diff`

```kotlin
@McpTool(name = "screenshot.diff")
@McpDescription("""…verbatim below…""")
suspend fun screenshot_diff(
    before: String,                          // base64 PNG (typically a prior capture.base64)
    after: String,                           // base64 PNG
    tolerance: Int = 8,                      // per-channel 0..255, clamped; masks AA jitter
    highlightColor: String = "#FF0000",      // CSS hex / named; invalid → red + warning
    baseTransparency: Float = 0.4f,          // 0.0..1.0, clamped; alpha of grayscale base
    sizeMismatchPolicy: String = "resize",   // 'resize' | 'pad' | 'error'
): ImageDiffPayload
// Each parameter carries an @McpDescription matching the prose above.
```

`@McpDescription` verbatim:

```
|Pixel-diff two base64-encoded PNGs (typically a 'before' and 'after' from two
|prior screenshot.capture calls) and return a composite image highlighting changed
|pixels plus structured diff stats. Pure CPU — no EDT, no IDE state touched.
|
|The output is a desaturated, dimmed version of 'after' with differing pixels
|tinted in highlightColor. A bbox is computed for the smallest axis-aligned
|rectangle containing every differing pixel (null when no pixels differ).
|
|Use this when: an agent needs to verify a UI change had a visible effect ("did
|Toggle Sidebar actually toggle it?"), localize the changed region of a screen
|before zooming in, or compute a quick "% changed" sanity number before spending
|tokens on a vision pass.
|
|Do NOT use this when: you want a fresh screenshot (use screenshot.capture), you
|want to highlight a known component (use screenshot.highlight — no diff needed),
|or you want OCR / semantic comparison (this is pixel math, not vision).
|
|tolerance is per-channel (R/G/B/A independently). 0 = exact match required; 8
|(default) absorbs subpixel-rendering jitter and JBR HiDPI antialiasing noise.
|sizeMismatchPolicy: 'resize' (default, safe for HiDPI/window-resize), 'pad' (top-
|left align, faithful but inflates 'changed' regions), 'error' (reject).
|
|Returns: { mimeType:"image/png", width, height, base64, warnings:string[],
|totalPixels:int, differingPixels:int, diffPercentage:double,
|bbox:{x,y,width,height}? }.
|
|Examples:
|  before=<b64>, after=<b64>                                    — defaults
|  before=<b64>, after=<b64>, tolerance=0                       — exact match
|  before=<b64>, after=<b64>, sizeMismatchPolicy="error"        — strict sizes
|  before=<b64>, after=<b64>, highlightColor="#FFFF00",
|    baseTransparency=0.2f                                      — yellow on dark
```

Response — new `model/ImageDiffPayload.kt` (parallels `ImagePayload`; no
inheritance — `@Serializable` data classes don't cleanly extend):

```kotlin
@Serializable data class BBox(val x: Int, val y: Int, val width: Int, val height: Int)

@Serializable
data class ImageDiffPayload(
    val mimeType: String, val width: Int, val height: Int, val base64: String,
    val warnings: List<String> = emptyList(),
    val totalPixels: Int, val differingPixels: Int,
    val diffPercentage: Double,   // 0.0..100.0, 4 dp
    val bbox: BBox? = null,        // null when differingPixels == 0
)
```

## IntelliJ APIs used

`ComponentRegistry.getInstance().lookup(id)`, `WindowManager.findVisibleFrame()`,
`McpExpectedError`, `Component.getLocationOnScreen()` (EDT-only, throws
`IllegalComponentStateException` when not displayable — wrap in EDT bounce +
`runCatching`). Pure AWT beyond that: `Graphics2D`, `BasicStroke`, `Color`,
`Font`, `BufferedImage`, `Robot`, `Rectangle`, `Base64`. No `@ApiStatus.Internal`.

## Threading, timeout, caching

`highlight` — base render uses `onEdtBlocking { … }` for capture AND
`getLocationOnScreen()` (Swing contract). The overlay (`drawRect` / `drawString`
on the returned `BufferedImage`) runs OFF EDT to keep the critical section
minimal: `val (base, origin, bounds, warns) = onEdtBlocking{…}; val annotated =
ScreenshotCapture.drawHighlight(base, bounds, color, …); return finalise(annotated, scale)`.

`diff` — pure CPU; no EDT / PSI / VFS. Decode → diff → encode on the ktor
coroutine.

**10 s cap** (CLAUDE.md) easily met: `highlight` ≈ existing `capture` cost + ~1
ms overlay (already bounded by `fitWithinBudget`'s 4-pass downscale). `diff` is
O(w × h): 4K (~8 MP) at ~20 ns/pixel ≈ 150 ms diff + ~200 ms encode = <500 ms
worst case. No caching — per-call data.

## Edge cases

`highlight`: (1) **componentId not in registry** — `McpExpectedError("Component
'$id' is no longer attached")`. (2) **Not displayable** when `target ∈
{active_frame, screen}` — `getLocationOnScreen()` throws; catch → `McpExpectedError(
"Component is not currently visible — cannot compute frame/screen coords")`.
`target=component` works on detached components via `paint()`. (3) **Bounds past
frame** — clip to image bounds + warn `"component clipped to frame bounds"`;
draw what's visible. (4) **Zero-area bounds** — draw small marker at origin +
warn. (5) **`target="all_frames"`** — reject explicitly; hint `active_frame` /
`screen`. (6) **Color parse fail / label too long / newlines** — fall back to
red + warn; UTF-8 truncate to 80 chars (existing helper), collapse `\n`/`\r` to
spaces; place label inside-top of box if clipping the top edge.

`diff`: (7) **Base64 / `ImageIO.read` fail** — `McpExpectedError("'<which>' is
not a valid PNG")`. (8) **Mismatched dimensions** — policy-driven: `resize`
(default, HiDPI / window-resize case), `pad` (faithful but inflates diff),
`error` (strict). (9) **AA text jitter** — `tolerance=8` default masks JBR HiDPI
subpixel noise; bump to 12 if platform smoke shows noop > 0.1%. (10) **Alpha
diffed per-channel** — translucent→opaque counts even when RGB matches.
(11) **Identical inputs** — `differingPixels=0`, `bbox=null`,
`diffPercentage=0.0`; output is still a valid grayscale-of-after PNG.
(12) **Output exceeds MCP budget** — same `fitWithinBudget` downscale; bbox is
reported in the **returned (scaled)** coords + warn `"output downscaled by N
halving passes; bbox is in scaled coordinates"`. (13) **Non-ARGB decoded type**
(e.g. `TYPE_BYTE_INDEXED`) — convert once to `TYPE_INT_ARGB`, diff via int-array
fast path.

## Files to create/modify

Edits: `tools/ScreenshotToolset.kt` (add both `@McpTool` methods, reuse
`finalise()`); `core/ScreenshotCapture.kt` (add `drawHighlight(base, bounds,
color, thickness, label)` — pure, off-EDT). New: `core/ImageDiffer.kt`
(headless `diff(before, after, tolerance, color, baseAlpha, policy):
DiffResult`); `model/ImageDiffPayload.kt`; `model/args/ScreenshotArgs.kt` (arg
mirrors for tests); `util/ColorParsing.kt` (`parseCssColor(raw): Color?` — hex
+ named-color table); tests `core/ImageDifferTest`,
`core/ScreenshotHighlightTest`, `util/ColorParsingTest`,
`core/platform/ScreenshotHighlightPlatformTest`. No XML wiring — both tools
join the existing `ScreenshotToolset` already registered by `META-INF/mcp-integration.xml`.

## Test plan

`ImageDifferTest` (unit): identical 8×8 → null bbox; one-pixel change → 1×1
bbox; 3×3 block at (5,5) → `{5,5,3,3}`; `tolerance=10` masks +5 per-channel,
`tolerance=4` reports it; size mismatch — `error` throws, `resize` returns
resized stats, `pad` counts OOB transparent-vs-opaque; alpha 255→128 counts at
`tolerance=0` not `128`; highlight tint at changed-block center > thresh.

`ScreenshotHighlightTest` (unit): `drawHighlight` 100×100 white, bounds
`(10,10,30,30)` red thickness 2 — `(10,10)` red, `(50,50)` white; zero-size →
marker; clipped bounds → warning, no throw; label above with headroom, inside-
top without.

`ColorParsingTest` (unit table): hex `#FF0000` / `#f00` / `#FF0000FF` → red;
case-insensitive `red`/`RED`; `lime` → `(0,255,0)`; junk → null.

`ScreenshotHighlightPlatformTest` (`BasePlatformTestCase`): register a `JButton`
in a JFrame, call `target="component"`, decode PNG, assert stroke pixel on box
edge; `target="active_frame"` verifies size + colored stripe;
`componentId="nonexistent"` → `McpExpectedError`. `diff` is pure CPU — units
cover it; no platform test.

## Estimated effort

ColorParsing+tests 1h; `drawHighlight`+tests 1.5h; `ImageDiffer`+tests 2.5h;
`ImageDiffPayload`+args 0.5h; two `@McpTool` methods + descriptions 1.5h;
platform test 1h; doc-gen verify + smoke 0.5h. **Total ≈ 1 day.**

## Open questions / risks

(1) **Multiple componentIds per highlight call?** Multiplies arg surface; v1
**single** — add `highlight_many` later if demanded. (2) **Crop diff to bbox
before returning?** Shrinks responses for small changes but loses spatial
context; **default: full-frame composite**; add `cropToBbox: Boolean = false`
follow-up arg if budget pain appears. (3) **AA on highlight rectangle** —
`VALUE_ANTIALIAS_ON` looks cleaner but fuzzes stroke edges; **decision: AA on**,
tests sample box interior. (4) **Native DPI vs JBR HiDPI** — `target="screen"`
(Robot) returns physical pixels, `target="active_frame"` returns logical;
existing `capture` has this and we inherit. (5) **Default `tolerance=8`** is a
guess; bump to `12` if platform-smoke noop comparison exceeds 0.1% diff.

## References

Existing: `tools/ScreenshotToolset.kt` (capture render + `finalise()`; crop
coord-space + clip math), `core/ScreenshotCapture.kt#fitWithinBudget`,
`core/ComponentRegistry.kt#lookup`, `util/ImageEncoding.kt` (`encodePngBase64`,
`scaleImage`), `util/EdtHelpers.kt#onEdtBlocking` (with `ModalityState.any()`).
IntelliJ src: `WindowManager.findVisibleFrame`. JetBrains MCP equivalent:
**none** — the shipped server in IntelliJ 2025.2+ has zero screenshot tools.

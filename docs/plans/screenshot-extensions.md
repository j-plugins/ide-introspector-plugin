# `screenshot.highlight` + `screenshot.diff`

## Purpose & motivation

JetBrains' built-in MCP server in 2025.2+ exposes **zero** screenshot tools — this
group is fully our niche. The current `ScreenshotToolset` (`screenshot.capture` /
`screenshot.crop`) lets an agent see the IDE but cannot point at a target ("where is
the breadcrumb bar?") or confirm a visual change ("did my action actually toggle the
sidebar?"). Two tightly-related helpers close that loop:

- `screenshot.highlight` overlays a colored bounding box (and optional label) on a
  captured frame so the user / supervising model can see which component a prior
  `ui.find_by_*` call resolved to. Saves the round trip of "screenshot → crop →
  describe in prose."
- `screenshot.diff` performs a pure-CPU pixel diff between two base64 PNGs the
  caller already has (typically a "before" and "after" from sequential
  `screenshot.capture` calls). Returns the diff image + bbox + percentage so an
  agent can verify "the change had a visible effect" without uploading both
  screenshots to a multimodal model.

**Success criteria**:
- `screenshot.highlight` — a single MCP call ("highlight componentId=c_a3f2…")
  produces a PNG with a red rectangle around that Swing component, no client-side
  image work needed.
- `screenshot.diff` — given two screenshots taken seconds apart, the response
  reports `differingPixels`, `diffPercentage`, and a tight `bbox` of the changed
  region — enabling the agent to decide "did this action have a visible effect?"
  in one structured call.

## Tool specification

### `screenshot.highlight`

**Signature:**

```kotlin
@McpTool(name = "screenshot.highlight")
@McpDescription("""…see below…""")
suspend fun screenshot_highlight(
    @McpDescription("Stable id from a prior ui.find_by_* / ui.get_tree call. Resolved via ComponentRegistry — must still be attached.")
    componentId: String,
    @McpDescription("'component' | 'active_frame' | 'screen'. 'all_frames' is not supported (ambiguous coords).")
    target: String = "active_frame",
    @McpDescription("Box color as CSS hex ('#FF0000', '#F00') or named ('red', 'lime'). Default '#FF0000'. Invalid → red.")
    color: String = "#FF0000",
    @McpDescription("Box stroke thickness in source-image pixels. Clamped to 1..20. Default 3.")
    thickness: Int = 3,
    @McpDescription("Optional label rendered just above (or below if clipped) the box. Truncated at 80 chars.")
    label: String? = null,
    @McpDescription("Post-render scale factor applied AFTER the highlight is drawn. Use 0.5/0.25 to shrink the payload.")
    scale: Double = 1.0,
    @McpDescription("Image format. Only 'png' supported in v1.")
    format: String = "png",
): ImagePayload
```

**`@McpDescription` draft** (verbatim — trim-margin form expected by the reflection bridge):

```
|Captures a screenshot of the IDE (target='component'|'active_frame'|'screen') AND
|overlays a colored bounding rectangle around the Swing component identified by
|componentId, optionally with a text label. The result is the same base64-PNG
|ImagePayload as screenshot.capture, downscaled to the MCP response budget.
|
|target options (must match the coordinate space the highlight is drawn in):
|  - "component"    — render only the target component; the highlight box fills it.
|                     Cheapest. Use when you already know which component you care
|                     about and only need to confirm its bounds visually.
|  - "active_frame" — render the focused IDE frame and draw the box at the
|                     component's frame-relative bounds (using Component.getLocationOnScreen
|                     minus frame origin). Off-screen components are clipped to the
|                     frame edge and a warning is emitted.
|  - "screen"       — Robot capture of the virtual desktop with the box drawn at
|                     the component's absolute screen coordinates. The only option
|                     that includes popups/tooltips/floating overlays.
|
|Use this when: the user asks "where is X on screen?", or after a ui.find_by_*
|call you want a visual confirmation of which component matched, or you need to
|annotate a screenshot for a screenshot-and-narrate workflow.
|
|Do NOT use this when: you just want the raw pixels (use screenshot.capture), you
|want a crop centered on the component (use screenshot.crop after reading
|ui.get_properties → bounds), or you need to highlight multiple components in one
|image (not supported in v1 — issue separate calls or compose client-side).
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
|  componentId="c_a3f2e1b8", target="component", scale=0.5     — component-only crop, halved
```

**Args** — defaults, validation, units:

- `componentId: String` — required. Lookup via `ComponentRegistry.getInstance().lookup(id)`.
  Missing → `McpExpectedError("Component '$id' is no longer attached")`.
- `target: String = "active_frame"` — `"component" | "active_frame" | "screen"`. Anything
  else throws `McpExpectedError("Unknown target: $target")`. `"all_frames"` rejected
  explicitly with a hint to use `active_frame` ("highlight in tiled view is ambiguous").
- `color: String = "#FF0000"` — parsed via new `util/ColorParsing.kt`; on parse
  failure default to `Color.RED` and add a warning. Tolerates `#RGB`, `#RRGGBB`,
  `#RRGGBBAA`, and the common named colors (`red`/`green`/`blue`/`yellow`/`cyan`/
  `magenta`/`white`/`black`/`orange`/`lime`/`gray`).
- `thickness: Int = 3` — clamped to `1..20` (Graphics2D BasicStroke). Drawn in
  source-image pixels (before `scale`).
- `label: String? = null` — `null` skips label drawing. Otherwise UTF-8 truncated
  at 80 chars (existing `Utf8Truncation` helper); drawn in a small system font
  with a contrasting drop-shadow above the box (below if the box is at top edge).
- `scale: Double = 1.0` — same semantics as `screenshot.capture`; applied AFTER
  the highlight is drawn so the box stroke scales proportionally.
- `format: String = "png"` — only `"png"` accepted; future-proof param.

**Response model**: existing `ImagePayload` (already declared in
`tools/ScreenshotToolset.kt`). No new fields. Box geometry intentionally not
serialized — the model already knows it from the request.

### `screenshot.diff`

**Signature:**

```kotlin
@McpTool(name = "screenshot.diff")
@McpDescription("""…see below…""")
suspend fun screenshot_diff(
    @McpDescription("Base64-encoded PNG of the 'before' state. Typically the base64 field of a prior screenshot.capture response.")
    before: String,
    @McpDescription("Base64-encoded PNG of the 'after' state. Same encoding.")
    after: String,
    @McpDescription("Per-channel tolerance (0..255) before a pixel counts as different. Default 8 — masks JBR anti-aliased text jitter.")
    tolerance: Int = 8,
    @McpDescription("CSS hex / named color used to tint differing pixels. Default '#FF0000'.")
    highlightColor: String = "#FF0000",
    @McpDescription("0.0..1.0 alpha applied to the grayscale base before compositing the highlight. Lower = darker base. Default 0.4.")
    baseTransparency: Float = 0.4f,
    @McpDescription("If the two inputs differ in size: 'resize' (scale 'after' to 'before' bilinear), 'pad' (pad smaller with transparent), 'error' (throw). Default 'resize'.")
    sizeMismatchPolicy: String = "resize",
): ImageDiffPayload
```

**`@McpDescription` draft** (verbatim):

```
|Pixel-diff two base64-encoded PNGs (typically a 'before' and 'after' from two
|prior screenshot.capture calls) and return a composite image highlighting changed
|pixels plus structured diff stats. Pure CPU — no EDT, no IDE state touched.
|
|The output image is a desaturated, dimmed version of 'after' with differing
|pixels tinted in highlightColor. A bbox is computed for the smallest axis-aligned
|rectangle containing every differing pixel (null when totalPixels match within
|tolerance).
|
|Use this when: an agent needs to verify a UI change had a visible effect
|("did Toggle Sidebar actually toggle it?"), or to localize the changed region of
|a screen before zooming in, or to compute a quick "% changed" sanity number
|before deciding whether to spend tokens on a vision pass.
|
|Do NOT use this when: you want a fresh screenshot (use screenshot.capture), you
|want to highlight a known component (use screenshot.highlight — no diff needed),
|or you want OCR / semantic comparison (this is pixel math, not vision).
|
|tolerance is per-channel (R/G/B/A independently). 0 = exact match required; 8
|(default) absorbs subpixel-rendering jitter and JBR HiDPI antialiasing noise.
|Anything above ~32 effectively only catches large color changes.
|
|sizeMismatchPolicy: 'resize' bilinearly scales 'after' to 'before' dimensions
|(safe for the common HiDPI / window-resize case), 'pad' aligns top-left and
|treats out-of-bounds as transparent (faithful but produces large 'changed'
|regions), 'error' rejects the call.
|
|Returns: { mimeType:"image/png", width, height, base64, warnings:string[],
|totalPixels:int, differingPixels:int, diffPercentage:double,
|bbox:{x,y,width,height}? }.
|
|Examples:
|  before=<b64>, after=<b64>                                    — defaults: tolerance=8, red overlay
|  before=<b64>, after=<b64>, tolerance=0                       — exact match required
|  before=<b64>, after=<b64>, sizeMismatchPolicy="error"        — reject mismatched sizes
|  before=<b64>, after=<b64>, highlightColor="#FFFF00",
|    baseTransparency=0.2f                                      — yellow on near-black base
```

**Args** — defaults, validation, units:

- `before: String` — required base64-PNG. Decode via `Base64.getDecoder()` then
  `ImageIO.read`. On decode failure → `McpExpectedError("'before' is not a valid
  PNG (base64 decode / ImageIO read failed)")`.
- `after: String` — same.
- `tolerance: Int = 8` — clamped to `0..255`. Per-channel.
- `highlightColor: String = "#FF0000"` — same parser as `highlight`; on failure
  default red + warning.
- `baseTransparency: Float = 0.4f` — clamped to `0.0..1.0`.
- `sizeMismatchPolicy: String = "resize"` — `"resize" | "pad" | "error"`. Default
  `"resize"` because HiDPI / window-resize causes near-identical screenshots to
  differ in pixel dimensions and we want diff to "just work" for the common case.

**Response model** — in `model/ImageDiffPayload.kt`:

```kotlin
@Serializable
data class BBox(val x: Int, val y: Int, val width: Int, val height: Int)

@Serializable
data class ImageDiffPayload(
    val mimeType: String,
    val width: Int,
    val height: Int,
    val base64: String,
    val warnings: List<String> = emptyList(),
    val totalPixels: Int,
    val differingPixels: Int,
    val diffPercentage: Double,   // 0.0..100.0, 4 dp rounding
    val bbox: BBox? = null,        // null when differingPixels == 0
)
```

`ImagePayload` stays unchanged — `ImageDiffPayload` parallels it (not inheritance;
`@Serializable` data classes can't sanely extend each other across this serializer
boundary).

Args mirrors in `model/args/ScreenshotArgs.kt` (create the file if it doesn't
exist — the existing tools inline their args).

## IntelliJ APIs used

- `com.intellij.openapi.wm.WindowManager.findVisibleFrame()` — existing usage in
  `ScreenshotToolset`. No new API.
- `com.intellij.mcpserver.McpExpectedError` — existing error type.
- `ComponentRegistry.getInstance().lookup(id)` — existing service.
- AWT only beyond that: `Graphics2D`, `BasicStroke`, `Color`, `Font`,
  `BufferedImage`, `Robot`, `Rectangle`, `Base64`. No platform-internal APIs.
- `Component.getLocationOnScreen()` for `target="screen"` coordinate translation —
  must be called on EDT and throws `IllegalComponentStateException` if the
  component is not currently displayable. Wrap with the EDT bouncer + `runCatching`.

No `@ApiStatus.Internal` reliance. Stable surface.

## Threading & EDT model

### `screenshot.highlight`

- Base render path identical to `screenshot.capture`: `onEdtBlocking { … }` for
  the `captureComponent` / `captureActiveFrame` / `captureRect` call AND for
  `Component.getLocationOnScreen()` (Swing API contract — must be on EDT).
- The highlight draw (`Graphics2D.drawRect` / `drawString` on the returned
  `BufferedImage`) is pure pixel work on a heap-resident image and does NOT touch
  Swing — must run **off** EDT to avoid stretching the EDT bounce. Pattern:

  ```
  val (baseImage, screenOrigin, compBounds, warnings) = onEdtBlocking { … }
  val annotated = drawHighlight(baseImage, compBounds, screenOrigin, color, …)
  return finalise(annotated, scale)
  ```

  This keeps the EDT critical section to the minimum (paint + bounds read) — the
  EDT-blocking budget is what trips the 10 s cap in heavy IDE states, not the CPU
  overlay.

### `screenshot.diff`

Pure CPU. No EDT, no PSI, no VFS. Decode → diff → encode all on the ktor
coroutine. No `onEdtBlocking` anywhere in this method.

### Caching

Neither tool caches. Screenshots are inherently per-call; diff inputs are
caller-supplied. (Future optimisation: hash inputs and short-TTL cache the
encoded diff — not worth the complexity now.)

## Timeout strategy

Hard 10 s cap per project rule. Both tools well under in normal operation:

- `highlight`: dominated by the existing `screenshot.capture` cost. Adds a single
  `drawRect` + optional `drawString` (~1 ms) and no extra EDT bounce beyond the
  one already in `capture`. Same downscale loop via `ImageEncoding.encodeWithBudget`
  / `ScreenshotCapture.fitWithinBudget` already caps total work.
- `diff`: O(width × height) per-channel diff. A 4K image (≈8 MP) at ~20 ns per
  pixel ARGB read = ~150 ms; encoding adds another ~200 ms. Worst case under
  500 ms. The fit-within-budget downscale loop reuses the existing 4-attempt
  cap.

If a future capture path adds an unbounded operation (large multi-monitor + 4-pass
encode), wrap the per-tool body in `withTimeoutOrNull(10_000)`.

## Edge cases

`screenshot.highlight`:

1. **componentId not in `ComponentRegistry`** — returns 404-style
   `McpExpectedError("Component '$id' is no longer attached")`. Matches the
   existing `screenshot.capture` behaviour for `target="component"`.
2. **Component is not displayable** (`isShowing()==false`) when
   `target ∈ {active_frame, screen}` — `getLocationOnScreen()` throws. Catch and
   throw `McpExpectedError("Component '$id' is not currently visible — cannot
   compute frame/screen coordinates")`. For `target="component"` this is fine —
   `paint()` works on detached components.
3. **Component bounds extend beyond the captured frame** (popup that slid offscreen,
   detached toolwindow) — clip the box to the image bounds and emit a warning
   `"component clipped to frame bounds"`. Still draw what's visible.
4. **Component bounds are zero-area** (`width==0 || height==0`) — draw a small
   `2×2 + thickness` marker centered on `(x,y)` instead, warning
   `"component has zero-size bounds; drew a marker"`.
5. **`target="all_frames"`** — explicitly rejected; ambiguous which frame the
   coordinates belong to. `McpExpectedError("highlight does not support
   target='all_frames' — use 'active_frame' or 'screen'")`.
6. **Color parse failure** — fall back to `Color.RED`, emit warning
   `"color '$raw' could not be parsed; defaulted to red"`. Do not throw.
7. **Label too long / contains newlines** — UTF-8 truncate to 80 chars via the
   existing helper, then collapse `\n`/`\r` to spaces. Drawn single-line.
8. **Label placement near top edge** — if box top < (font height + 4 px),
   draw label INSIDE-TOP of box instead of above. If the box fills the image,
   draw at bottom-left corner.
9. **Anti-aliasing** — enable `RenderingHints.VALUE_ANTIALIAS_ON` for both the
   rectangle stroke and the label text so the box doesn't look jaggy at non-1.0
   scale. See open Qs.
10. **`screenshot.capture target="component"` cannot include popups** — same
    limitation here; document in the description but don't try to detect.

`screenshot.diff`:

11. **Either input fails base64 decode or `ImageIO.read` returns null** —
    `McpExpectedError("'<which>' is not a valid PNG")`. Do not silently produce
    an empty diff.
12. **Mismatched dimensions** — policy-driven:
    - `"resize"` (default) — bilinear scale `after` to `before` size.
    - `"pad"` — align top-left, treat OOB pixels of the smaller image as
      transparent (counts as differing if the other image has opaque pixels
      there).
    - `"error"` — `McpExpectedError("size mismatch: before=${w0}x${h0},
      after=${w1}x${h1}; set sizeMismatchPolicy to 'resize' or 'pad' to allow")`.
13. **Anti-aliased text jitter producing huge diffs at `tolerance=0`** —
    default `tolerance=8` documented to mask this. Empirically masks JBR HiDPI
    subpixel jitter on Linux.
14. **Alpha channel** — diff per-channel including alpha (the IDE renders
    `TYPE_INT_ARGB`). A pixel transitioning from translucent to opaque counts as
    different even if RGB is equal.
15. **Identical inputs** — `differingPixels = 0`, `bbox = null`, `diffPercentage
    = 0.0`. Output image is the grayscale-of-after at `baseTransparency` (still a
    valid PNG; agent can detect "no diff" from the structured fields).
16. **Very large PNGs hitting the MCP response budget** — same `fitWithinBudget`
    downscale loop. Downscaling happens AFTER bbox computation, so `bbox`
    coordinates remain in the **returned (scaled)** image's pixel space; emit a
    warning `"output downscaled by N halving passes; bbox is in scaled
    coordinates"` so the caller knows.
17. **Decoded image with non-ARGB type** — convert via
    `BufferedImage(w, h, TYPE_INT_ARGB)` + `g.drawImage`. Don't try to diff a
    `TYPE_BYTE_INDEXED` buffer directly — `getRGB` is correct but slow; the
    convert + `getRGB` on `TYPE_INT_ARGB` int-array fast path is significantly
    quicker.
18. **`baseTransparency` out of range** — clamp to `0.0..1.0` silently.
19. **`tolerance` negative or >255** — clamp to `0..255` silently.

## Files to create/modify

| Path | Op | What |
|------|----|------|
| `tools/ScreenshotToolset.kt` | Edit | Add `screenshot_highlight` + `screenshot_diff` `@McpTool` methods (~50 LOC + descriptions). Reuse private `finalise` helper for both. |
| `core/ScreenshotCapture.kt` | Edit | Add `drawHighlight(base, bounds, color, thickness, label)` helper returning a new `BufferedImage`. Pure, off-EDT, testable. |
| `core/ImageDiffer.kt` | Create | Headless pixel-diff: `diff(before: BufferedImage, after: BufferedImage, tolerance: Int, highlightColor: Color, baseTransparency: Float, sizeMismatchPolicy: String): DiffResult` returning composite + stats + bbox. Pure JVM, fully unit-testable. |
| `model/ImageDiffPayload.kt` | Create | `@Serializable` `BBox` + `ImageDiffPayload`. Parallel to `ImagePayload` (no inheritance — see note above). |
| `model/args/ScreenshotArgs.kt` | Create | `@Serializable` arg classes mirroring the two methods (consistency with `arch-list-services` etc; not required by the MCP bridge but used by tests). |
| `util/ColorParsing.kt` | Create | `parseCssColor(raw: String): Color?` — hex (`#RGB`, `#RRGGBB`, `#RRGGBBAA`), named-color table, returns null on failure (caller adds warning). Pure, unit-tested. |
| `src/test/kotlin/.../core/ImageDifferTest.kt` | Create | Pure-JVM unit tests for the diff math (see test plan). |
| `src/test/kotlin/.../core/ScreenshotHighlightTest.kt` | Create | Pure-JVM unit tests for `drawHighlight` (synthetic `BufferedImage`, check pixel at box edge). |
| `src/test/kotlin/.../util/ColorParsingTest.kt` | Create | Pure-JVM table-driven tests. |
| `src/test/kotlin/.../core/platform/ScreenshotHighlightPlatformTest.kt` | Create | `BasePlatformTestCase` that registers a `JButton` with `ComponentRegistry`, calls `screenshot_highlight` via the toolset, verifies the returned image dimensions + that a red pixel exists at expected offset. |

No XML wiring — both tools live in the existing `ScreenshotToolset` already
registered by `META-INF/mcp-integration.xml`. No new optional dependency.

## Test plan

**Unit (`ImageDifferTest.kt`)** — pure JVM, no IntelliJ runtime:

- two identical 8×8 ARGB images → `differingPixels=0`, `bbox=null`,
  `diffPercentage=0.0`.
- one pixel changed → `differingPixels=1`, bbox is the 1×1 rect at that pixel.
- 3×3 block of changed pixels at (5,5) in a 32×32 image → bbox `{5,5,3,3}`.
- `tolerance=10` masks a +5 per-channel change (no diff); `tolerance=4` reports
  it.
- size-mismatch with `policy="error"` throws; with `"resize"` succeeds and
  returns the resized stats; with `"pad"` succeeds and treats OOB as transparent
  diff.
- alpha-only change (RGB equal, A 255→128) counts as diff at `tolerance=0`,
  not at `tolerance=128`.
- highlight color tint actually applied (sample center of changed block, assert
  channel > threshold).

**Unit (`ScreenshotHighlightTest.kt`)** — pure JVM:

- `drawHighlight` on a 100×100 white image with bounds `(10,10,30,30)`, red,
  thickness 2 → pixel at `(10,10)` is red; pixel at `(11,11)` is red; pixel at
  `(50,50)` is white.
- zero-size bounds draws marker at the bounds origin (not a no-op).
- bounds clipped to image edge does not throw; emits warning string.
- label rendered above the box when there's headroom; inside-top when not.

**Unit (`ColorParsingTest.kt`)** — table-driven:

- `#FF0000` / `#f00` / `#FF0000FF` → red.
- `red` / `RED` / `Red` → red (case-insensitive).
- `lime` → `(0,255,0)`.
- `not a color` / `#GGG` / `""` / `null` → null.

**Platform (`ScreenshotHighlightPlatformTest.kt`)** — extends
`BasePlatformTestCase`:

- register a `JButton` of known size + location in a parent JFrame, capture via
  `screenshot_highlight target="component"`, decode the PNG, assert a stroke-color
  pixel exists at the box edge.
- call `screenshot_highlight target="active_frame"` for a real frame component;
  verify the returned image equals the frame size and includes a colored stripe.
- assert that with `componentId="nonexistent"` an `McpExpectedError` is thrown.

`screenshot.diff` doesn't need a platform test — it's pure CPU and well-covered
by the unit tests.

## Estimated effort

| Step | Hours |
|------|-------|
| `util/ColorParsing.kt` + tests | 1.0 |
| `core/ScreenshotCapture.drawHighlight` + unit tests | 1.5 |
| `core/ImageDiffer.kt` + comprehensive unit tests | 2.5 |
| `model/ImageDiffPayload.kt` + args | 0.5 |
| `tools/ScreenshotToolset` two `@McpTool` methods + `@McpDescription`s | 1.5 |
| Platform test (`runIde` sandbox tick) | 1.0 |
| Doc-gen verify + manual smoke | 0.5 |
| **Total** | **~1 day combined** |

## Open questions / risks

1. **Multiple componentIds in one highlight call?** Useful for "show me where
   buttons A, B and C are" — but multiplies the color/label parameter surface
   (`List<HighlightSpec>` arg). **Decision for v1: single component**. If demand
   appears, add a sibling `screenshot.highlight_many` rather than overloading.
2. **Crop diff output to bbox before returning?** Would shrink the response for
   small changes — but loses the spatial context of where the change occurred in
   the frame. **Decision: return full-frame composite by default**; add a
   `cropToBbox: Boolean = false` arg in a follow-up if response budget pain
   appears.
3. **Anti-aliasing the highlight rectangle.** `RenderingHints.VALUE_ANTIALIAS_ON`
   gives a cleaner look at non-1.0 `scale` but slightly fuzzes the box edges,
   which may complicate pixel-edge assertions in tests. **Decision: AA on**;
   test assertions sample box interior, not the exact stroke edge.
4. **Native screen DPI vs JBR HiDPI scaling** — `target="screen"` via `Robot`
   returns physical pixels; `target="active_frame"` via `Component.paint()`
   returns the framework's logical pixels. Existing `screenshot.capture` already
   has this inconsistency; we inherit it. Document in description if user reports
   confusion.
5. **Should `screenshot.diff` accept files / VFS paths instead of base64?** Would
   save bandwidth when the caller has the image on disk — but breaks the
   pure-CPU / no-IDE-state contract and adds path-resolution surface. **Decision:
   base64 only** (matches the rest of the screenshot group's output format).
6. **Default `tolerance` value** — `8` is a guess for JBR HiDPI subpixel jitter
   on Linux/macOS. Validate empirically during the platform test pass; bump to
   `12` if "before vs immediately-after-no-action" produces > 0.1% diff.

## References

- Existing code:
  - `tools/ScreenshotToolset.kt#screenshot_capture` — base render path,
    `finalise()` helper, error pattern.
  - `tools/ScreenshotToolset.kt#screenshot_crop` — `coordinateSpace` translation,
    pattern for clip math.
  - `core/ScreenshotCapture.kt#fitWithinBudget` — downscale loop reused by both
    new tools.
  - `core/ComponentRegistry.kt#lookup` — id → Component resolution.
  - `util/ImageEncoding.kt#encodePngBase64` — base64 encode reused by both;
    `scaleImage` reused for size-mismatch resize.
  - `util/EdtHelpers.kt#onEdtBlocking` — EDT bounce with `ModalityState.any()`.
- IntelliJ source:
  - `Component.getLocationOnScreen` (JDK): https://docs.oracle.com/en/java/javase/17/docs/api/java.desktop/java/awt/Component.html#getLocationOnScreen()
  - `WindowManager.findVisibleFrame`: https://github.com/JetBrains/intellij-community/blob/master/platform/platform-api/src/com/intellij/openapi/wm/WindowManager.java
- JetBrains MCP equivalent: **none**. The shipped JetBrains MCP server in
  IntelliJ 2025.2+ has no screenshot tools at all — this whole group is
  greenfield for our plugin.

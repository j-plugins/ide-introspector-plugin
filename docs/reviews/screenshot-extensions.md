# Review: screenshot.highlight + screenshot.diff

Branch: `claude/project-features-analysis-odEwP` @ main HEAD (salvage
`dcd4207` + green-fix `733906d`).
Reviewer scope: `core/ScreenshotCapture.kt#drawHighlight`,
`core/ImageDiffer.kt`, `model/ImageDiffPayload.kt`,
`model/args/ScreenshotArgs.kt` extension, `util/ColorParsing.kt`,
`tools/ScreenshotToolset.kt` (two new `@McpTool`s), three new test files
+ one platform test.

## Verdict
Ship-ready with one MED cleanup. Geometry is correct (stroke ON the
bounds edge — FIXER's intent matches, modulo inherent `Graphics2D` AA
quirks), threading/EDT split is exactly what the plan asked for,
`ImageDiffer` is pure-CPU with sound size-mismatch and alpha semantics,
bbox math is right, and all 12 checklist items pass. The only must-fix
is the duplicated dead `if/else` branch in `drawHighlight` (Finding 1);
everything else is polish.

## Summary
- One new core file (`ImageDiffer.kt` 258 LOC), extension to
  `ScreenshotCapture` (`drawHighlight` + `HighlightResult`, ~110 LOC),
  one new model (`ImageDiffPayload.kt`), one util (`ColorParsing.kt`),
  two new `@McpTool`s on `ScreenshotToolset.kt` (highlight @153, diff @272),
  arg mirrors appended.
- No new XML wiring — both tools join the existing `ScreenshotToolset`.
- `ImageDiffPayload` parallels `ImagePayload` field-for-field (NOT
  inheritance — CLAUDE.md classloader policy honoured). Reuses `Bounds`
  rather than introducing `BBox` (shape-identical — reuse is right).
- Hard rules: no new timeouts (inherits `onEdtBlocking`'s 10 s cap);
  `kotlinx-serialization-json` stays `compileOnly`; these ARE pre-built
  tools, not new exec surface.
- `@McpDescription` strings are the plan's 5-section convention verbatim;
  per-parameter descriptions present on every arg.
- Off-EDT overlay vs on-EDT base: `screenshot_highlight` does
  `onEdtBlocking { capture + relativeBounds }` then calls `drawHighlight`
  OUTSIDE the bounce (`tools/ScreenshotToolset.kt:225-269`). Good.
- `screenshot_diff` does ZERO EDT work — pure ktor coroutine path.

## Findings

### 1. [MED] `drawHighlight` has a duplicated dead `if/else` branch

`core/ScreenshotCapture.kt:115-125`:

```kotlin
val out = if (base.type == BufferedImage.TYPE_INT_ARGB) {
    val copy = BufferedImage(base.width, base.height, BufferedImage.TYPE_INT_ARGB)
    val cg = copy.createGraphics()
    try { cg.drawImage(base, 0, 0, null) } finally { cg.dispose() }
    copy
} else {
    val copy = BufferedImage(base.width, base.height, BufferedImage.TYPE_INT_ARGB)
    val cg = copy.createGraphics()
    try { cg.drawImage(base, 0, 0, null) } finally { cg.dispose() }
    copy
}
```

Both branches are byte-identical — leftover from a refactor where one
was presumably meant to be a fast-path. The comment ("paint() output is
already TYPE_INT_ARGB but some callers may hand us TYPE_INT_RGB")
implies the ARGB branch was meant to skip re-allocation. Two options:
(a) drop the conditional (current behaviour, just clearer), or (b)
restore the optimisation via `image.copyData()`. `ImageDiffer.ensureArgb`
(`core/ImageDiffer.kt:183-189`) implements ARGB-passthrough correctly but
isn't reusable here — `drawHighlight`'s contract is "returns a COPY"
(enforced by `ScreenshotHighlightTest#base image is not mutated`). Take
(a) for v1.

### 2. [LOW] `drawHighlight` stroke spills outside bounds at frame edges

`core/ScreenshotCapture.kt:155-161`:

```kotlin
val drawW = (intersected.width - 1).coerceAtLeast(1)
val drawH = (intersected.height - 1).coerceAtLeast(1)
g.drawRect(drawX, drawY, drawW, drawH)
```

The `-1` correctly places the stroke ON the bounds edge — `Rectangle`'s
half-open convention runs x=10..39 for `(10,10,30,30)`, and
`drawRect(10,10,29,29)` puts the geometric right edge at pixel column 39.
Matches the FIXER's intent. However:

- Thickness>1 + AA: stroke is centred on the geometric line, so it
  extends `thickness/2` pixels OUTSIDE the rectangle. When `bounds.y==0`,
  half the stroke clips off-image and the visible stroke is
  half-thickness on that edge only.
- Thickness==1 + AA: stroke straddles the pixel boundary, producing a
  fuzzy 2-pixel stripe rather than a crisp 1-pixel line.

Both are inherent `Graphics2D` quirks. Standard fix: offset by +0.5 and
use `Rectangle2D.Double`:

```kotlin
g.draw(Rectangle2D.Double(drawX + 0.5, drawY + 0.5, drawW - 1.0, drawH - 1.0))
```

Snaps stroke centre to pixel centre, puts the entire stroke INSIDE the
bounds. Tests pass either way (lenient `channelTol=80`); visual polish.

### 3. [LOW] `ColorParsing` doesn't accept `#RGBA` (4-char hex)

`util/ColorParsing.kt:28-49` accepts 3/6/8 hex. CSS Color Level 4 also
defines `#RGBA` (`#F00A` → red @ 0xAA alpha). Plan specified 3/6/8 only,
so spec-compliant, but agents copying web styling will hit this. 4-line
add: `4 -> Color(expand(hex[0]), expand(hex[1]), expand(hex[2]), expand(hex[3]))`.

### 4. [LOW] `decodePng` accepts any format `ImageIO.read` recognises

`tools/ScreenshotToolset.kt:402-407`. Description says "base64-encoded
PNG" but JPEG / GIF / BMP would silently decode. Error message on
failure says "is not a valid PNG", so a JPEG input would *succeed* with
no signal. Sniff the first 8 bytes for the PNG magic
(`89 50 4E 47 0D 0A 1A 0A`) and reject — 2-line fix that makes the error
message honest.

### 5. [LOW] `screenshot_diff` budget-downscale path is untested

`tools/ScreenshotToolset.kt:354-366` rescales bbox into the scaled
coordinate space when `fitWithinBudget` downscales. Math is correct
(`sx = fitted.width / composite.width`, multiply, clamp), but no test
hits this path — `ImageDifferTest` exercises the differ directly. Add a
unit test (pure CPU, no `BasePlatformTestCase`) that feeds large enough
images to force the 4-halving-passes cap. The `===` identity check
(`fitted === diff.composite`) is correct — `fitWithinBudget` returns the
same reference when size is already under budget.

### 6. [LOW] `tolerance` clamps silently at the tool layer

`tools/ScreenshotToolset.kt:315`. Description says "Clamped 0..255"; the
clamp happens inside `ImageDiffer.diff` at `:67`. Negative or huge
tolerance silently snaps with no signal. Either warn when clamping kicks
in, or tighten the description to "Clamped silently". Internally
consistent with `ImageDifferTest`'s clamp coverage.

### 7. [LOW] `ImageDiffer` composite build walks pixels twice

`core/ImageDiffer.kt:75-104` does `desaturate(a)` + Graphics2D draw, then
a SECOND pass to apply alpha factor per pixel. Fuse into one loop:
compute luma + alpha + write in a single traversal. 4K saves ~30% encode
wall-time. Plan's <500ms target is already met; perf nit.

Also: `g.composite = AlphaComposite.Clear; g.fillRect(...)` at `:82-83`
is redundant — a freshly allocated `TYPE_INT_ARGB` is already
zero-initialised. 3 lines to drop.

### 8. [LOW] `screenshot_highlight` `format` parameter is unused

`tools/ScreenshotToolset.kt:204` — only `png` is supported; argument is
ignored. Matches the `screenshot.capture` / `screenshot.crop`
precedent, so consistent. Either validate `format == "png"` and throw,
or drop until v2. Not a regression introduced here.

### 9. [LOW] Platform test only covers `target="component"` path

`ScreenshotHighlightPlatformTest` has 2 cases: highlight on a button,
out-of-image bounds. Plan called for `target="active_frame"` end-to-end
and `componentId="nonexistent"` rejection. Neither present — both are
the riskiest EDT-bouncing paths (`relativeBounds` /
`absoluteBoundsOrNull`) and they're untested at the platform layer.
20-min add: extend the existing test to register the button, look up a
bogus id (`McpExpectedError`), then call `screenshot_highlight` with
`target="active_frame"` via the toolset.

`screenshot.diff` is pure CPU — correctly has no platform test per plan.

### 10. [INFO] `SizeMismatchException` is a `RuntimeException`

`core/ImageDiffer.kt:47`. Used only by `screenshot_diff` (catch + rethrow
as `McpExpectedError`). Works but couples loosely. A `sealed` return
type (`DiffResult.Success | DiffResult.SizeMismatch`) would be cleaner;
not worth blocking on.

### 11. [INFO] `ColorParsing` lacks `rgb()` / `rgba()` / `hsl()` syntax

Out of scope per plan; current named-color table covers the basic 17 +
greys / fuchsia / aqua. Fine for v1.

## Threading & EDT model — OK

- `screenshot_highlight` isolates EDT-only work (`captureComponent` /
  `captureActiveFrame` / `captureRect` / `getLocationOnScreen`) inside
  `onEdtBlocking { … }` and runs `drawHighlight` off-EDT — exactly the
  plan's "keep the critical section minimal".
- `screenshot_diff` does ZERO EDT work.
- `onEdtBlocking` uses `ModalityState.any()` — not held back by modal
  dialogs (e.g. exec confirmation).
- `mcpError(...)` thrown inside the EDT block propagates correctly:
  `runCatching(block).getOrThrow()` re-raises on the ktor coroutine.
- `runCatching { component.locationOnScreen }` handles
  `IllegalComponentStateException` for detached components → caller
  throws `McpExpectedError`.

## Timeouts — OK

No new `withTimeoutOrNull`, no new `Future.get`, no new latches.
`onEdtBlocking`'s 10 s cap covers `highlight`'s base-capture; `diff` is
pure CPU bounded by input size (~500ms for 4K per plan estimate).

## Serialization classloader policy — OK

`kotlinx-serialization-json` remains `compileOnly`. `ImageDiffPayload`
does NOT extend `ImagePayload` — parallel fields per CLAUDE.md.
`Bounds` is reused (already `@Serializable`).

## `@McpDescription` quality — OK

Both descriptions follow the plan's 5-section convention verbatim. Two
micro-quibbles:
- `screenshot_highlight`'s `target` description doesn't mention
  rejection of `'all_frames'` (body does throw with a hint).
- `screenshot_diff`'s `tolerance` says "Clamped 0..255" without warning
  about silent clamping (Finding 6).

`docs/MCP_TOOLS.md` regenerated by the KSP processor on `./gradlew build`.

## Test coverage — mostly good, two gaps

Present:
- `ImageDifferTest` (12 cases) — identical inputs, single-pixel + 3×3
  block bbox, tolerance masking (negative / huge), per-channel alpha,
  all three size policies, highlight tint, percentage rounding,
  non-ARGB input. Covers the plan's test plan bullets.
- `ScreenshotHighlightTest` (8 cases) — happy path, zero-area marker,
  off-image marker, partial clip warning, thickness clamping
  (low/high), label with newlines, null label, base-not-mutated.
- `ColorParsingTest` (14 cases) — null / blank / hash-only / all hex
  forms / case / non-hex / odd length / named (red, lime vs green,
  gray/grey) / unknown / whitespace. Comprehensive.
- `ScreenshotHighlightPlatformTest` (2 cases) — JFrame+JButton stroke
  render, off-image warning.

Missing: Finding 5 (budget rescale), Finding 9 (active_frame +
nonexistent-id platform coverage).

## Edge case coverage vs plan — checklist

| Plan edge case | Coverage |
|---|---|
| `highlight` componentId not in registry | `:209-210` → `McpExpectedError`. Untested (Finding 9). |
| `highlight` component not displayable | `:382-385` runCatching on `locationOnScreen` → error. Untested. |
| `highlight` bounds past frame | `core/ScreenshotCapture.kt:144-148` intersection + marker. Unit-tested. |
| `highlight` zero-area bounds | `:137-143` marker + warning. Unit-tested. |
| `highlight` `target="all_frames"` | `:206-208` reject + hint. Untested (trivial). |
| `highlight` color parse / label | `:213-223` warning + truncate + collapse. Label collapse tested. |
| `diff` base64 / ImageIO fail | `:324-325` → error. Untested (trivial). |
| `diff` mismatched dimensions × policy | `core/ImageDiffer.kt:153-181`. All 3 policies unit-tested. |
| `diff` alpha per-channel | Unit-tested both directions. |
| `diff` identical inputs | Unit-tested (null bbox). |
| `diff` output exceeds budget | `:354-366` rescale path. Untested (Finding 5). |
| `diff` non-ARGB decoded type | Unit-tested (TYPE_INT_RGB → ARGB). |

## File / line references

- `core/ScreenshotCapture.kt:105-191` — `drawHighlight` + result
- `core/ScreenshotCapture.kt:115-125` — duplicated branch (Finding 1)
- `core/ScreenshotCapture.kt:155-161` — stroke-on-edge math (Finding 2)
- `core/ImageDiffer.kt:59-147` — `diff` main path
- `core/ImageDiffer.kt:75-104` — composite + alpha pass (Finding 7)
- `core/ImageDiffer.kt:153-181` — `align` size-mismatch policy
- `model/ImageDiffPayload.kt:1-27` — payload, parallels ImagePayload (OK)
- `util/ColorParsing.kt:28-49` — hex 3/6/8 only (Finding 3)
- `tools/ScreenshotToolset.kt:153-270` — `screenshot_highlight`
- `tools/ScreenshotToolset.kt:272-378` — `screenshot_diff`
- `tools/ScreenshotToolset.kt:354-366` — bbox rescale on overflow
- `tools/ScreenshotToolset.kt:402-407` — decodePng (Finding 4)
- `model/args/ScreenshotArgs.kt:24-43` — arg mirrors (OK)
- `src/test/.../core/ImageDifferTest.kt` — 12 cases (OK)
- `src/test/.../core/ScreenshotHighlightTest.kt` — 8 cases (OK)
- `src/test/.../core/platform/ScreenshotHighlightPlatformTest.kt`
  — 2 cases, missing nonexistent-id + active_frame (Finding 9)
- `src/test/.../util/ColorParsingTest.kt` — 14 cases (OK)

## Recommended actions before merge

1. **Finding 1** (MED, 1 min) — collapse the duplicated `if/else` in
   `drawHighlight`. Misleading code.
2. **Finding 5** (LOW, 20 min) — `ScreenshotDiffTest` for downscale +
   bbox-in-scaled-coords.
3. **Finding 9** (LOW, 20 min) — extend platform test with
   nonexistent-id rejection + active_frame end-to-end.
4. Findings 2 / 3 / 4 / 6 / 7 / 8 / 10 — polish, pick up next pass.

Sources consulted:
- Plan `docs/plans/screenshot-extensions.md`
- [`Graphics2D` Javadoc — `drawRect` outline & `BasicStroke` line geometry](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/Graphics2D.html)
- [`BufferedImage` type constants — `TYPE_INT_ARGB` vs `TYPE_INT_ARGB_PRE`](https://docs.oracle.com/en/java/javase/21/docs/api/java.desktop/java/awt/image/BufferedImage.html)
- [CSS Color Module Level 4 — `#RGBA` short-hex form](https://www.w3.org/TR/css-color-4/#hex-notation)
- [pixelmatch — per-channel tolerance + bbox prior art](https://github.com/mapbox/pixelmatch)
- CLAUDE.md (timeouts, serialization classloader, EDT threading,
  `@McpDescription` 5-section convention)

package com.github.xepozz.ide.introspector.util

import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.attribute.FileTime
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.imageio.ImageIO

/** Returns a scaled copy of [src]. If [factor]≈1.0 returns [src] unchanged. */
fun scaleImage(src: BufferedImage, factor: Double): BufferedImage {
    if (kotlin.math.abs(factor - 1.0) < 1e-3) return src
    val w = kotlin.math.max(1, (src.width * factor).toInt())
    val h = kotlin.math.max(1, (src.height * factor).toInt())
    val dst = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)
    val g: Graphics2D = dst.createGraphics()
    try {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.drawImage(src, 0, 0, w, h, null)
    } finally {
        g.dispose()
    }
    return dst
}

const val SCREENSHOT_TMP_DIR_NAME = "ide-introspector-screenshots"
val SCREENSHOT_TTL: Duration = Duration.ofMinutes(30)

private val TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")

fun screenshotTempDir(): Path =
    Path.of(System.getProperty("java.io.tmpdir"), SCREENSHOT_TMP_DIR_NAME)

/**
 * Writes [image] as PNG into [screenshotTempDir] and returns the resulting path.
 * The file is registered with [java.io.File.deleteOnExit] so the JVM cleans up on
 * graceful shutdown; callers should invoke [pruneOldScreenshots] before writing to
 * also evict files left over from previous (possibly crashed) sessions.
 */
fun writePngToTempFile(image: BufferedImage, prefix: String = "screenshot"): Path {
    val dir = Files.createDirectories(screenshotTempDir())
    val stamp = LocalDateTime.now().format(TIMESTAMP_FORMAT)
    val suffix = UUID.randomUUID().toString().take(8)
    val file = dir.resolve("$prefix-$stamp-$suffix.png")
    Files.newOutputStream(file).use { ImageIO.write(image, "png", it) }
    file.toFile().deleteOnExit()
    return file
}

/**
 * Removes screenshot files older than [ttl] from [screenshotTempDir]. Best-effort —
 * any IO error on an individual entry is swallowed so a single locked/permission-denied
 * file never blocks new captures.
 */
fun pruneOldScreenshots(ttl: Duration = SCREENSHOT_TTL) {
    val dir = screenshotTempDir()
    if (!Files.isDirectory(dir)) return
    val cutoff = FileTime.from(Instant.now().minus(ttl))
    Files.newDirectoryStream(dir, "*.png").use { stream ->
        for (entry in stream) {
            try {
                if (Files.getLastModifiedTime(entry) < cutoff) Files.deleteIfExists(entry)
            } catch (_: Exception) {
            }
        }
    }
}


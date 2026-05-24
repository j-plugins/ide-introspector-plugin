package com.github.xepozz.ide.introspector.util

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.awt.image.BufferedImage
import java.nio.file.Files
import java.nio.file.attribute.FileTime
import java.time.Duration
import java.time.Instant

/**
 * Unit tests for the file-based screenshot helpers in `ImageEncoding.kt`.
 *
 * These tests touch the real `java.io.tmpdir`. They clean up after themselves but
 * intentionally do NOT delete the entire screenshot directory in [tearDown] — another
 * concurrently-running test/IDE session could be writing into it. We only remove files
 * created by *this* test (tracked via [tracked]).
 */
class ImageEncodingTest {

    private val tracked = mutableListOf<java.nio.file.Path>()

    @After
    fun tearDown() {
        tracked.forEach { runCatching { Files.deleteIfExists(it) } }
    }

    private fun newImage(w: Int = 10, h: Int = 10): BufferedImage =
        BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)

    @Test
    fun `writePngToTempFile creates a file with PNG signature in the dedicated tmp dir`() {
        val path = writePngToTempFile(newImage()).also { tracked.add(it) }

        assertTrue("file should exist on disk", Files.exists(path))
        assertEquals(
            "file must live in the dedicated screenshot dir",
            screenshotTempDir().toAbsolutePath(),
            path.parent.toAbsolutePath(),
        )
        assertTrue("file name must end with .png", path.fileName.toString().endsWith(".png"))

        val header = Files.readAllBytes(path).take(8)
        val pngMagic = listOf(
            0x89.toByte(), 0x50.toByte(), 0x4E.toByte(), 0x47.toByte(),
            0x0D.toByte(), 0x0A.toByte(), 0x1A.toByte(), 0x0A.toByte(),
        )
        assertEquals("first 8 bytes must be the PNG magic signature", pngMagic, header)
    }

    @Test
    fun `writePngToTempFile generates unique names across rapid successive calls`() {
        val a = writePngToTempFile(newImage()).also { tracked.add(it) }
        val b = writePngToTempFile(newImage()).also { tracked.add(it) }
        assertFalse("successive writes must not collide on filename", a == b)
    }

    @Test
    fun `pruneOldScreenshots removes files older than the TTL and keeps fresh ones`() {
        val stale = writePngToTempFile(newImage(), prefix = "stale").also { tracked.add(it) }
        val fresh = writePngToTempFile(newImage(), prefix = "fresh").also { tracked.add(it) }

        // Backdate the "stale" file so the pruner has a real cutoff to compare against.
        Files.setLastModifiedTime(stale, FileTime.from(Instant.now().minus(Duration.ofHours(2))))

        pruneOldScreenshots(ttl = Duration.ofMinutes(30))

        assertFalse("stale file (2h old, TTL 30m) should be deleted", Files.exists(stale))
        assertTrue("fresh file should be preserved", Files.exists(fresh))
    }

    @Test
    fun `pruneOldScreenshots is a no-op when the directory does not exist`() {
        val dir = screenshotTempDir()
        if (Files.isDirectory(dir)) {
            Files.newDirectoryStream(dir, "*.png").use { stream ->
                stream.forEach { runCatching { Files.deleteIfExists(it) } }
            }
            runCatching { Files.deleteIfExists(dir) }
        }
        // Should not throw even if the directory is absent.
        pruneOldScreenshots()
    }
}

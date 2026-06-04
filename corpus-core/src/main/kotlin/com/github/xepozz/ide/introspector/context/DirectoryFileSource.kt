package com.github.xepozz.ide.introspector.context

import java.io.File

class DirectoryFileSource(private val root: File) : FileSource {
    override fun readAll(): List<RawFile> {
        if (!root.isDirectory) return emptyList()
        return root.walkTopDown()
            .filter { it.isFile && it.extension == "md" }
            .map { RawFile(relativePathOf(it), it.readText(Charsets.UTF_8)) }
            .sortedBy { it.relativePath }
            .toList()
    }

    private fun relativePathOf(file: File): String =
        file.relativeTo(root).path.replace(File.separatorChar, '/')
}

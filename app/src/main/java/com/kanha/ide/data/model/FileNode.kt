package com.kanha.ide.data.model

import java.io.File

data class FileNode(
    val file: File,
    val isDirectory: Boolean = file.isDirectory,
    val name: String = file.name,
    var isExpanded: Boolean = false,
    val depth: Int = 0,
    var children: List<FileNode>? = null // null means not loaded yet (lazy loading)
) {
    val path: String
        get() = file.absolutePath
}

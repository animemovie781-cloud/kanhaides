package com.kanha.ide.data.repository

import com.kanha.ide.data.model.FileNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class FileRepository(private val projectRoot: String) {

    /**
     * Lists children of a given directory.
     * Enforces canonical path checks to prevent escaping project root.
     */
    suspend fun listChildren(parentFile: File, depth: Int): List<FileNode> = withContext(Dispatchers.IO) {
        if (!isSafePath(parentFile)) return@withContext emptyList()
        
        val files = parentFile.listFiles()?.toList() ?: emptyList()
        
        // Sort folders first, then files alphabetically
        files.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
            .map { file ->
                FileNode(
                    file = file,
                    depth = depth
                )
            }
    }

    /**
     * Security check: Ensure the requested path is within the project root.
     */
    private fun isSafePath(file: File): Boolean {
        val canonicalRoot = File(projectRoot).canonicalPath
        val canonicalTarget = file.canonicalPath
        return canonicalTarget.startsWith(canonicalRoot)
    }

    suspend fun createFile(parent: File, name: String): Boolean = withContext(Dispatchers.IO) {
        val newFile = File(parent, name)
        if (!isSafePath(newFile)) return@withContext false
        try {
            newFile.createNewFile()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun createDirectory(parent: File, name: String): Boolean = withContext(Dispatchers.IO) {
        val newDir = File(parent, name)
        if (!isSafePath(newDir)) return@withContext false
        newDir.mkdirs()
    }

    suspend fun rename(file: File, newName: String): Boolean = withContext(Dispatchers.IO) {
        if (!isSafePath(file)) return@withContext false
        val newFile = File(file.parentFile, newName)
        if (!isSafePath(newFile)) return@withContext false
        file.renameTo(newFile)
    }

    suspend fun delete(file: File): Boolean = withContext(Dispatchers.IO) {
        if (!isSafePath(file)) return@withContext false
        if (file.isDirectory) {
            file.deleteRecursively()
        } else {
            file.delete()
        }
    }
}
